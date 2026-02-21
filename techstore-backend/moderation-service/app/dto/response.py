# -*- coding: utf-8 -*-
"""
DTO Response - Chuẩn hoá response giống hệ thống Spring Boot.

Tương tự:
  - ApiResponse<T>
  - ErrorCode enum
"""

from enum import Enum
from typing import Generic, Optional, TypeVar

from pydantic import BaseModel

T = TypeVar("T")


# ── ErrorCode (giống enum ErrorCode trong Spring Boot) ──
class ErrorCode(Enum):
    UNAUTHENTICATED = (1001, "Unauthenticated")
    UNAUTHORIZED = (1007, "You do not have permission")
    INVALID_REQUEST = (1002, "Invalid request")
    INTERNAL_ERROR = (9999, "Internal server error")

    def __init__(self, code: int, message: str):
        self.code = code
        self.message = message


# ── ApiResponse<T> ──
class ApiResponse(BaseModel, Generic[T]):
    code: int = 1000
    message: str = "Success"
    result: Optional[T] = None

    @classmethod
    def success(cls, result: T = None, message: str = "Success") -> "ApiResponse[T]":
        return cls(code=1000, message=message, result=result)

    @classmethod
    def error(cls, error_code: ErrorCode) -> "ApiResponse":
        return cls(code=error_code.code, message=error_code.message)
