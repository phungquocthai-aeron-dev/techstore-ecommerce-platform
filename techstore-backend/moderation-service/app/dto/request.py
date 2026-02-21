# -*- coding: utf-8 -*-
from pydantic import BaseModel, Field


class PredictRequest(BaseModel):
    content: str = Field(..., min_length=1, max_length=5000, description="Nội dung cần kiểm tra")

    class Config:
        json_schema_extra = {
            "example": {
                "content": "Mua ngay kẻo hết! Giảm giá 90% chỉ hôm nay!!!"
            }
        }


class PredictResult(BaseModel):
    label: str = Field(..., description="Nhãn dự đoán: Spam | Toxic | Valid")
    confidence: float = Field(..., description="Độ tin cậy cao nhất (0.0 - 1.0)")
    probabilities: dict[str, float] = Field(..., description="Xác suất từng nhãn")
    processed_text: str = Field(..., description="Văn bản sau tiền xử lý")
