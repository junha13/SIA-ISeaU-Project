# # DB연결 엔진 , Base = declarative_base() 등
# from sqlalchemy import create_engine
# from sqlalchemy.orm import sessionmaker, declarative_base
# from ..config import settings

# engine = create_engine(settings.DB_URL, pool_pre_ping=True)
# SessionLocal = sessionmager(autocommit=False, autoflush=False, bind=engine)
# Base = declarative_base()

# def get_db():
#     db = SessionLocal()
#     try:
#         yield db
#     finally:
#         db.close()