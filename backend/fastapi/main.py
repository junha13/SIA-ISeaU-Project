from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware 
import uvicorn
import asyncio
import cv2
import time
import numpy as np 
import subprocess 
import shlex 
import json
import os
from typing import List, Dict, Any, Tuple, Optional
from ultralytics import YOLO 
from contextlib import asynccontextmanager # ★추가: lifespan 사용을 위한 모듈

# ====================================================================
# ★★★ 0. 핵심 설정 및 상수 ★★★
# ====================================================================

OUT_W = 1024  # FFmpeg의 출력 프레임 너비 (픽셀). 분석 성능과 화질의 균형을 맞춥니다.
OUT_H = 768   # FFmpeg의 출력 프레임 높이 (픽셀).
YOLO_MODEL_PATH = "beach_yolo.pt" # Docker 컨테이너 내부의 YOLO 모델 파일 경로/이름.
YOLO_CONF_THRESHOLD = 0.5   # YOLO 탐지 결과의 최소 신뢰도 임계값. 0.0 ~ 1.0 사이 값.
DET_EVERY_FRAMES = 5 # ★성능 최적화: YOLO 추론을 몇 프레임마다 실행할지 결정합니다. 
FRAME_SIZE = OUT_W * OUT_H * 3 # FFmpeg으로부터 읽어올 RAW BGR (3채널) 프레임의 총 바이트 크기.

YOUTUBE_URL_TO_FETCH = "https://www.youtube.com/watch?v=E9I4A9aXJUY" # Streamlink가 주소를 가져올 유튜브 라이브 스트림 URL.
DEFAULT_STREAM_URL = "http://211.114.96.121:1935/jejusi7/11-30T.stream/playlist.m3u8" # Streamlink 실패 시 대체할 기본 CCTV 스트림 주소.
MIN_MOTION_AREA = 500 # GMM이 움직임으로 간주할 최소 픽셀 영역 크기.
MAX_MOTION_AREA = 5000
# 전역 상태 변수 (AIStreamServer 클래스에서 초기화됨)
VIDEO_SOURCES: List[Tuple[str, str]] = []
yolo_model = None
gmm_models = {}
frame_counters = {}

# yolo, gmm 관심 영역 설정 // 전체 해상도 변경 시 이것도 변경해야 함
ROI_PX = {
    "stream1": [(0, 400), (1024, 400), (1024, 768), (0, 768)],   # 하단 절반 사각형(다각형도 가능)
    "stream2": [(0, 400), (1024, 400), (1024, 768), (0, 768)],   # 오른쪽 띠
}

# ====================================================================
# ★★★ Helper 함수: Streamlink 및 GMM 로직 ★★★
# ====================================================================

def get_ytdlp_url(youtube_url: str) -> Optional[str]:
    """YouTube 페이지에서 FFmpeg이 바로 읽을 m3u8 재생 URL만 추출."""
    try:
        # 1) m3u8 직출 (한 줄 출력)
        cmd = f'yt-dlp -g -f "best[protocol^=m3u8]/b[ext=m3u8]" {youtube_url}'
        out = subprocess.check_output(shlex.split(cmd), text=True, stderr=subprocess.STDOUT, timeout=20)
        url = out.strip().splitlines()[-1] if out.strip() else None
        if url and ".m3u8" in url:
            return url
    except Exception as e:
        print(f"[YTDLP] m3u8 직출 실패: {e}")

    try:
        # 2) 폴백: JSON에서 m3u8 포맷만 골라 추출
        cmd = f'yt-dlp --dump-json --no-warnings -f "bv*+ba/best" {youtube_url}'
        out = subprocess.check_output(shlex.split(cmd), text=True, stderr=subprocess.STDOUT, timeout=25)
        info = json.loads(out)
        for f in (info.get("formats") or []):
            u = f.get("url", "")
            if u and (".m3u8" in u or "m3u8" in (f.get("ext","") + f.get("protocol",""))):
                return u
    except Exception as e:
        print(f"[YTDLP] JSON 폴백 실패: {e}")

    return None

def is_overlap(box1, box2):
    """두 박스가 겹치는지 확인 (GMM 보완 탐지에서 중복 탐지 방지용)"""
    x1_min, y1_min, x1_max, y1_max = box1
    x2_min, y2_min, x2_max, y2_max = box2
    
    if (x1_max < x2_min or x1_min > x2_max or
        y1_max < y2_min or y1_min > y2_max):
        return False
    return True

def build_roi_mask_px(w:int, h:int, pts_px):
    """픽셀 다각형 pts_px로 ROI 마스크 생성 (흰=관심, 검정=무시)"""
    if not pts_px:
        return None
    m = np.zeros((h, w), np.uint8)
    cv2.fillPoly(m, [np.array(pts_px, np.int32)], 255)
    return m

# ====================================================================
# ★★★ 1. AI 스트리밍 서버 클래스 정의 ★★★
# ====================================================================

class AIStreamServer:
    def __init__(self):
        self.yolo_model = None
        self.gmm_models = {}
        self.frame_counters = {}
        self.video_sources: List[Tuple[str, str]] = []
        self.roi_masks = {}  # ★추가: ROI 마스크 캐시

    async def initialize(self):
        """서버 시작 시 Streamlink 호출 및 모든 모델을 안전하게 로드합니다."""
        global yolo_model, gmm_models, frame_counters, VIDEO_SOURCES
        
        print("서버 초기화 로직 실행: Streamlink 및 모델 로드 시작...")

        stream2_youtube_url = get_ytdlp_url(YOUTUBE_URL_TO_FETCH)
        
        self.video_sources = [
            ("stream1", DEFAULT_STREAM_URL), 
            ("stream2", stream2_youtube_url if stream2_youtube_url else DEFAULT_STREAM_URL), 
        ]
        
        try:
            self.yolo_model = YOLO(os.path.join("/server/app/yolopt", YOLO_MODEL_PATH))
            print(f"YOLO model loaded successfully.")
        except Exception as e:
            self.yolo_model = None
            
        self.gmm_models = {
            name: cv2.createBackgroundSubtractorMOG2(history=1200, varThreshold=25, detectShadows=False)
            for name, _ in self.video_sources
        }
        self.frame_counters = {name: 0 for name, _ in self.video_sources}

        # ★추가: ROI 마스크 생성 (해상도 기준 한 번만)
        self.roi_masks = {
            sid: build_roi_mask_px(OUT_W, OUT_H, ROI_PX.get(sid))
            for sid, _ in self.video_sources
        }
        
        # 전역 변수 업데이트 (FastAPI 라우터에서 참조 가능하도록)
        VIDEO_SOURCES = self.video_sources
        yolo_model = self.yolo_model
        gmm_models = self.gmm_models
        frame_counters = self.frame_counters
        print("초기화 완료.")


    async def process_single_stream(self, websocket: WebSocket, stream_id: str, stream_url: str):
        """단일 스트림의 FFmpeg 구동, AI 처리, 웹소켓 전송 파이프라인."""
        
        command = (
            'ffmpeg -analyzeduration 1000000 -probesize 32 -fflags nobuffer -an ' 
            # ★수정: -an (No Audio) 추가로 오디오 스트림을 완전히 무시
            f'-i "{stream_url}" -s {OUT_W}x{OUT_H} -r 15 -map 0:v -f image2pipe -pix_fmt bgr24 -vcodec rawvideo -' 
        )
        
        process = None
        try:
            process = await asyncio.create_subprocess_exec(
                *shlex.split(command),
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            
            last_yolo_boxes = [] 
            
            while True:
                frame_bytes = await process.stdout.readexactly(FRAME_SIZE)
                if not frame_bytes: break
                
                frame_bgr = np.frombuffer(frame_bytes, dtype=np.uint8).reshape(OUT_H, OUT_W, 3)
                frame = frame_bgr.copy() 
                vis = frame.copy() # 시각화용 복사본
                
                people_count = 0
                
                # -----------------------------------------------------------------------
                # 3-4. AI 처리 (YOLO + GMM 보완 로직)
                # -----------------------------------------------------------------------

                # ★추가: ROI 마스크 적용 (fg/YOLO 입력을 관심영역으로 제한)
                roi_mask = self.roi_masks.get(stream_id)
                if roi_mask is not None:
                    frame_for_ai = cv2.bitwise_and(frame, frame, mask=roi_mask)  # ROI 영역만 남긴 프레임
                else:
                    frame_for_ai = frame

                # 1. GMM 전경 마스크 생성 (움직임 포착)
                gmm = self.gmm_models[stream_id]
                fg = gmm.apply(frame_for_ai, learningRate=0.005)
                fg = cv2.morphologyEx(fg, cv2.MORPH_OPEN, np.ones((3,3),np.uint8), iterations=2)
                fg = cv2.dilate(fg, np.ones((10,10),np.uint8), iterations=2) 

                motion_count = int(cv2.countNonZero(fg)) 

                # 2. YOLO 추론 (5프레임마다 실행)
                self.frame_counters[stream_id] += 1
                if self.yolo_model and self.frame_counters[stream_id] % DET_EVERY_FRAMES == 0:
                    results = self.yolo_model.predict(
                        frame_for_ai, conf=YOLO_CONF_THRESHOLD, 
                        verbose=False, classes=[0]
                    )[0]
                    
                    yolo_detected_boxes = []
                    
                    for b, c in zip(results.boxes.xyxy.cpu().numpy(), results.boxes.cls.cpu().numpy()):
                        if int(c) == 0:
                            x1,y1,x2,y2 = map(int, b)
                            yolo_detected_boxes.append((x1, y1, x2, y2))
                            people_count += 1
                            # YOLO 탐지 박스 (초록색)
                            cv2.rectangle(vis, (x1,y1), (x2,y2), (0,255,0), 2)
                    
                    last_yolo_boxes = yolo_detected_boxes 

                # 3. GMM 기반 움직임 보완 탐지 (YOLO가 놓친 움직이는 객체 찾기)
                contours, _ = cv2.findContours(fg, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

                for cnt in contours:
                    if cv2.contourArea(cnt) : continue

                    x, y, w, h = cv2.boundingRect(cnt)
                    motion_box = (x, y, x + w, y + h)

                    is_covered_by_yolo = False
                    for yolo_box in last_yolo_boxes:
                        if is_overlap(motion_box, yolo_box):
                            is_covered_by_yolo = True
                            break
                    
                    if not is_covered_by_yolo:
                        # GMM 보완 탐지 박스 (노란색)
                        cv2.rectangle(vis, (x, y), (x + w, y + h), (0, 255, 255), 2)
                        people_count += 1 

                frame = vis 
                
                # -----------------------------------------------------------------------
                # 3-6. 인코딩 및 전송
                # -----------------------------------------------------------------------
                
                ok, buf = cv2.imencode(".jpg", frame, [cv2.IMWRITE_JPEG_QUALITY, 70])
                if not ok: continue
                
                jpg_chunk = buf.tobytes()
                payload = {"stream_id": stream_id, "timestamp": int(time.time() * 1000), "people": people_count, "motion": motion_count}
                
                await websocket.send_bytes(jpg_chunk)
                await websocket.send_text(json.dumps(payload))

        except Exception as e:
            if process:
                stderr_data = await process.stderr.read()
                print(f"[{stream_id}] Streaming Fatal Error: {e}")
                print(f"[{stream_id}] FFmpeg STDERR: {stderr_data.decode()}")
        finally:
            if process and process.returncode is None:
                process.terminate()
                await process.wait()


# ====================================================================
# ★★★ 4. FastAPI 앱 및 라우터 설정 (메인 실행부) ★★★
# ====================================================================

# 서버 인스턴스 생성
server = AIStreamServer()

# ★수정: lifespan 컨텍스트 관리자 정의 (on_event 대체)
@asynccontextmanager
async def lifespan(app: FastAPI):
    # 1. 서버 시작(Startup) 로직: 여기서 initialize 메서드 호출
    await server.initialize()
    yield # 이 시점에서 서버가 요청 처리를 시작합니다.
    # 2. 서버 종료(Shutdown) 로직: 필요하다면 cleanup 코드를 여기에 작성합니다.


# FastAPI 앱 생성 시 lifespan 인자 전달
app = FastAPI(
    title="Easy AI CCTV Server", 
    version="0.1.0", 
    lifespan=lifespan # ★변경: lifespan 등록
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], 
    allow_credentials=True,
    allow_methods=["*"], 
    allow_headers=["*"],
)

# WebSockets 라우터 (클래스 메서드 호출로 로직 전달)
@app.websocket("/ws/stream") 
async def websocket_video_stream(websocket: WebSocket):
    
    await websocket.accept()
    print("메인 웹소켓 연결 수락됨. 스트림 시작 준비.")
    
    tasks = []
    try:
        for stream_id, stream_url in server.video_sources:
            if stream_url is None: continue

            task = asyncio.create_task(
                server.process_single_stream(websocket, stream_id, stream_url)
            )
            tasks.append(task)
            
        await asyncio.gather(*tasks)

    except Exception as e:
        print(f"메인 핸들러 오류: {e}")
    finally:
        pass

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)