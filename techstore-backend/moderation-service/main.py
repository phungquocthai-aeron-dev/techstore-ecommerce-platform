# -*- coding: utf-8 -*-
"""
Spam Detection Microservice - FastAPI
Tương thích với hệ thống Spring Boot microservice
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from app.routers import predict, health
from app.ml.model import detector
from app.core.config import settings


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load ML model khi khởi động service"""
    print("Đang khởi động Spam Detection Service...")
    print("Model sẵn sàng!")
    yield
    print("Spam Detection Service đã dừng.")


app = FastAPI(
    title="Spam Detection Service",
    description="Microservice phát hiện Spam / Toxic / Valid dùng Multinomial Naive Bayes",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health.router, prefix="/internal", tags=["Internal"])
app.include_router(
    predict.router,
    prefix="/moderation",
    tags=["Moderation"],
)