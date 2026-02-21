# -*- coding: utf-8 -*-
"""
app/ml/model.py
---------------
Load SpamDetector từ file .joblib khi FastAPI khởi động.
File này được import bởi routers → detector phải tồn tại ở cấp module.
"""

import joblib
import os
from pathlib import Path

# ── Đường dẫn tới model.joblib ────────────────────────────────────────────────
# Mặc định: tìm model.joblib ở thư mục gốc của service (ngang hàng với main.py)
# Có thể override bằng biến môi trường MODEL_PATH
_BASE_DIR   = Path(__file__).resolve().parents[2]   # moderation-service/
MODEL_PATH  = Path(os.getenv("MODEL_PATH", _BASE_DIR / "model.joblib"))

# ── Load detector ─────────────────────────────────────────────────────────────
if not MODEL_PATH.exists():
    raise FileNotFoundError(
        f"\n{'='*60}"
        f"\n[LỖII] Không tìm thấy file model: {MODEL_PATH}"
        f"\n→ Hãy chạy script train trước:  python train.py"
        f"\n{'='*60}"
    )

print(f"[model.py] Đang load model từ: {MODEL_PATH}")
detector = joblib.load(MODEL_PATH)
print(f"[model.py] ✓ Load thành công! Nhãn: {list(detector.model.classes_)}")