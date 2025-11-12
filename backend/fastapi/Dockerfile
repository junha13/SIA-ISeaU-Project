# Dockerfile (캐시 최적화 버전)

# 가벼운 베이스 (Python 3.10-bookworm 유지)
FROM python:3.11

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

# 1. 시스템 의존성 설치 (가장 안정적, 자주 바뀌지 않음)
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 libglib2.0-0 libsm6 libxext6 \
    pkg-config \
    build-essential \
    ffmpeg libavcodec-dev libavformat-dev libavutil-dev \
    && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /server

# 3. Pip 업그레이드
RUN python -m pip install -U pip wheel setuptools

# 4. ★변경: 가벼운 라이브러리 먼저 설치 (빠른 레이어)
RUN pip install --no-cache-dir fastapi uvicorn pydantic pydantic-settings websockets python-multipart numpy

# 5. ★변경: 무거운 라이브러리 나중에 설치 (느린 레이어 분리)
RUN pip install --no-cache-dir ultralytics opencv-python-headless yt-dlp

# 6. YOLO 모델 파일 복사 (모델 파일이 바뀌면 이 단계만 다시 실행)
# NOTE: 이 단계는 코드 수정과 독립적으로 캐시됩니다.
RUN mkdir -p /server/app/yolopt
COPY beach_yolo.pt /server/app/yolopt/beach_yolo.pt

# 7. 소스 코드 복사 (★ 코드 수정 시에만 빌드 ★)
# main.py 등 Python 코드를 수정해도, 위에 무거운 설치 단계 캐시는 깨지지 않습니다.
COPY . /server

# FastAPI 서버 기동
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]