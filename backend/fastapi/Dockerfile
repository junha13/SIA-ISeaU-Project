# Dockerfile (캐시 최적화 버전)

# 가벼운 베이스 (Python 3.10-bookworm 유지)
FROM nvidia/cuda:12.1.1-runtime-ubuntu22.04

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    YOLO_CONFIG_DIR=/server/.ultralytics

# 1. 시스템 의존성 설치 (가장 안정적, 자주 바뀌지 않음)
RUN apt-get update && apt-get install -y --no-install-recommends \
    # Python 3.11 설치 및 개발 도구
    python3.11 python3.11-dev python3-pip \
    # FFmpeg 및 OpenCV 관련 기본 의존성
    libgl1 libglib2.0-0 libsm6 libxext6 \
    pkg-config \
    build-essential \
    ffmpeg libavcodec-dev libavformat-dev libavutil-dev \
    && \
    rm -rf /var/lib/apt/lists/*

# 'python' 및 'pip' 명령이 python3.11을 가리키도록 연결 설정
RUN update-alternatives --install /usr/bin/python python /usr/bin/python3.11 1
RUN update-alternatives --install /usr/bin/pip pip /usr/bin/pip3 1

WORKDIR /server

# 3. Pip 업그레이드
RUN python -m pip install -U pip wheel setuptools

# 4. ★변경: 가벼운 라이브러리 먼저 설치 (빠른 레이어)
RUN pip install --no-cache-dir fastapi uvicorn pydantic pydantic-settings websockets python-multipart numpy

# 5. GPU 사용
RUN pip install --no-cache-dir --index-url https://download.pytorch.org/whl/cu121 torch torchvision torchaudio

# 6. 비디오/추적
# ultralytics 8.3.x + opencv 고정, bytetrack 의존 lapx/filterpy 포함
RUN pip install --no-cache-dir \
    "ultralytics==8.3.34" \
    "opencv-python-headless==4.10.0.84" \
    "yt-dlp==2024.10.22" \
    "lapx>=0.5.7" \
    "filterpy>=1.4.5" \
    "httpx==0.27.2"

# 7. 설정 디렉토리 미리 생성 (권한 경고 방지)
RUN mkdir -p ${YOLO_CONFIG_DIR}  

# 8. YOLO 모델 파일 복사 (모델 파일이 바뀌면 이 단계만 다시 실행)
# NOTE: 이 단계는 코드 수정과 독립적으로 캐시됩니다.
RUN mkdir -p /server/app/yolopt
COPY beach_yolo.pt /server/app/yolopt/beach_yolo.pt

# 9. 소스 코드 복사 (★ 코드 수정 시에만 빌드 ★)
# main.py 등 Python 코드를 수정해도, 위에 무거운 설치 단계 캐시는 깨지지 않습니다.
COPY . /server

# FastAPI 서버 기동
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]