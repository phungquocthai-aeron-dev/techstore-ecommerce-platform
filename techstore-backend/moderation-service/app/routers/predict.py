# # -*- coding: utf-8 -*-
# """
# Predict Router

# Endpoint:
#   POST /predict  → cần JWT (authenticated)
# """

# from fastapi import APIRouter, Depends, HTTPException, status

# from app.core.security import get_current_user
# from app.dto.request import PredictRequest, PredictResult
# from app.dto.response import ApiResponse, ErrorCode
# from app.ml.model import detector

# router = APIRouter()


# @router.post(
#     "/predict",
#     response_model=ApiResponse[PredictResult],
#     summary="Phân loại nội dung Spam / Toxic / Valid",
#     description="Nhận `content`, trả về nhãn và độ tin cậy. **Yêu cầu JWT Bearer Token.**",
# )
# def predict(
#     body: PredictRequest,
#     current_user: dict = Depends(get_current_user),   # ← bắt buộc xác thực
# ):
#     """
#     Tương tự @PreAuthorize trong Spring Boot:
#     - get_current_user() đảm bảo token hợp lệ trước khi vào logic.
#     - current_user chứa payload JWT (sub, roles, exp, ...).
#     """
#     try:
#         label, confidence, probabilities = detector.predict(body.content)
#         from app.ml.model import preprocess_text
#         result = PredictResult(
#             label=label,
#             confidence=confidence,
#             probabilities=probabilities,
#             processed_text=preprocess_text(body.content),
#         )
#         return ApiResponse.success(result=result)

#     except RuntimeError as e:
#         raise HTTPException(
#             status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
#             detail=ApiResponse.error(ErrorCode.INTERNAL_ERROR).model_dump(),
#         )
#     except Exception as e:
#         raise HTTPException(
#             status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
#             detail=ApiResponse.error(ErrorCode.INTERNAL_ERROR).model_dump(),
#         )
# -*- coding: utf-8 -*-
"""
Predict Router

Endpoint:
  POST /predict  → cần JWT (authenticated)
"""

from fastapi import APIRouter, Depends, HTTPException, status

from app.core.security import get_current_user
from app.dto.request import PredictRequest, PredictResult
from app.dto.response import ApiResponse, ErrorCode
from app.ml.model import detector
from spam_detector import preprocess_text               # ✅ FIX 1: import đúng chỗ, đúng cấp module

router = APIRouter()


@router.post(
    "/predict",
    response_model=ApiResponse[PredictResult],
    summary="Phân loại nội dung Spam / Toxic / Valid",
    description="Nhận `content`, trả về nhãn và độ tin cậy. **Yêu cầu JWT Bearer Token.**",
)
def predict(
    body: PredictRequest,
    current_user: dict = Depends(get_current_user),
):
    """
    Tương tự @PreAuthorize trong Spring Boot:
    - get_current_user() đảm bảo token hợp lệ trước khi vào logic.
    - current_user chứa payload JWT (sub, roles, exp, ...).
    """
    try:
        # ✅ FIX 2: predict_single() trả về (label: str, proba: np.ndarray)
        #           KHÔNG phải (label, confidence, probabilities)
        label, proba = detector.predict_single(body.content)

        # ✅ FIX 3: tự build confidence và probabilities từ proba array
        classes       = list(detector.model.classes_)
        label_idx     = detector._class_index.get(label, 0)
        confidence    = round(float(proba[label_idx]), 4)
        probabilities = {c: round(float(p), 4) for c, p in zip(classes, proba)}

        result = PredictResult(
            label          = label,
            confidence     = confidence,
            probabilities  = probabilities,
            processed_text = preprocess_text(body.content),
        )
        return ApiResponse.success(result=result)

    except RuntimeError as e:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=ApiResponse.error(ErrorCode.INTERNAL_ERROR).model_dump(),
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=ApiResponse.error(ErrorCode.INTERNAL_ERROR).model_dump(),
        )