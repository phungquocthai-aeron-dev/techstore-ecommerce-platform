# -*- coding: utf-8 -*-
"""
Health Router - Public endpoints (tương tự PUBLIC_ENDPOINTS trong SecurityConfig)

Endpoints:
  GET  /internal/health       → kiểm tra service còn sống không
  GET  /internal/model/info   → thông tin model hiện tại (dùng cho service mesh)
"""

from fastapi import APIRouter

from app.dto.response import ApiResponse
from app.ml.model import detector

router = APIRouter()


@router.get(
    "/health",
    response_model=ApiResponse[dict],
    summary="Health check",
)
def health_check():
    return ApiResponse.success(result={"status": "UP", "service": "spam-detection-service"})


@router.get(
    "/model/info",
    response_model=ApiResponse[dict],
    summary="Thông tin model",
)
def model_info():
    return ApiResponse.success(result={
        "is_trained": detector.is_trained,
        "classes": detector.classes_,
        "algorithm": "Multinomial Naive Bayes + TF-IDF",
    })
