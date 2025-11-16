# app/core/config.py

from pydantic_settings import BaseSettings
from typing import Literal

class Settings(BaseSettings):
    # --- 비디오 소스 및 인코딩 설정 ---
    VIDEO_SRC: str = "0"  # 웹캠 기본값
    OUT_W: int = 1280     # 리사이즈 너비
    OUT_H: int = 1080     # 리사이즈 높이
    
    # --- YOLO/AI 설정 (Passthrough 모드에서도 충돌 방지용으로 유지) ---
    YOLO_MODEL: str = "yolov8n.pt" 
    YOLO_CONF: float = 0.40       
    YOLO_DEVICE: Literal["cpu", "cuda"] = "cpu"
    
    # ... (나머지 DET_EVERY, ROI 등 변수들도 필요하면 여기에 포함해야 합니다) ...
    # 현재는 최소한의 충돌 방지용 변수만 포함합니다.

    class Config:
        env_file = ".env"
        env_file_encoding = 'utf-8'

settings = Settings()