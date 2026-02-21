# -*- coding: utf-8 -*-
"""
JWT Security - Tương tự CustomJwtDecoder + JwtAuthenticationEntryPoint trong Spring Boot.

Flow:
  1. Client gửi request với header: Authorization: Bearer <token>
  2. get_current_user() extract + verify token (giống CustomJwtDecoder.decode())
  3. Nếu invalid → 401 UNAUTHENTICATED (giống JwtAuthenticationEntryPoint)
  4. Nếu valid   → trả về payload để controller dùng
"""

from datetime import datetime, timezone
from typing import Optional

import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from app.core.config import settings
from app.dto.response import ApiResponse, ErrorCode

# Scheme HTTPBearer tự extract "Bearer <token>" khỏi header Authorization
bearer_scheme = HTTPBearer(auto_error=False)


def _build_401() -> HTTPException:
    """
    Tạo HTTPException 401 với body chuẩn ApiResponse,
    giống JwtAuthenticationEntryPoint của Spring Boot.
    """
    return HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail=ApiResponse(
            code=ErrorCode.UNAUTHENTICATED.code,
            message=ErrorCode.UNAUTHENTICATED.message,
        ).model_dump(),
        headers={"WWW-Authenticate": "Bearer"},
    )


def decode_token(token: str) -> dict:
    """
    Parse và verify JWT token.
    Tương tự CustomJwtDecoder.decode() trong Spring Boot:
      - Parse SignedJWT
      - Kiểm tra chữ ký với secret key
      - Kiểm tra expiration
    """
    try:
        payload = jwt.decode(
            token,
            settings.JWT_SECRET_KEY,
            algorithms=[settings.JWT_ALGORITHM],
            options={"verify_exp": True},
        )
        return payload
    except jwt.ExpiredSignatureError:
        raise _build_401()
    except jwt.InvalidTokenError:
        raise _build_401()

# def decode_token(token: str) -> dict:
#     """
#     Giống CustomJwtDecoder trong Spring:
#     - Parse token
#     - KHÔNG verify signature
#     - Tự kiểm tra expiration
#     """
#     try:
#         # Decode không verify signature
#         payload = jwt.decode(
#             token,
#             options={
#                 "verify_signature": False,
#                 "verify_exp": False,  # mình tự check exp
#             },
#         )

#         # Check expiration giống Spring
#         exp = payload.get("exp")
#         if exp is not None:
#             now = datetime.now(timezone.utc).timestamp()
#             if now > exp:
#                 raise _build_401()

#         return payload

#     except Exception:
#         raise _build_401()

def get_current_user(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(bearer_scheme),
) -> dict:
    """
    Dependency inject vào các endpoint cần xác thực.
    Tương tự .anyRequest().authenticated() trong SecurityConfig.
    Trả về JWT payload (sub, roles, exp, ...).
    """
    if credentials is None:
        raise _build_401()
    return decode_token(credentials.credentials)


def get_optional_user(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(bearer_scheme),
) -> Optional[dict]:
    """
    Dependency cho endpoint public nhưng vẫn muốn đọc thông tin user nếu có token.
    Tương tự .permitAll() nhưng vẫn có thể dùng thông tin auth.
    """
    if credentials is None:
        return None
    try:
        return decode_token(credentials.credentials)
    except HTTPException:
        return None
