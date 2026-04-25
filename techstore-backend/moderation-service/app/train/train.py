# -*- coding: utf-8 -*-
"""
Script huấn luyện model và lưu ra file .joblib
Chạy 1 lần: python train.py
Sau đó FastAPI service load model từ file, không cần train lại.
"""

import pandas as pd
import joblib
from pathlib import Path
import os
from sklearn.model_selection import train_test_split

# Import từ spam_detector.py (cùng thư mục)
from spam_detector import (
    SpamDetector,
    preprocess_text,
    VALID_LABELS,
    CSV_FILE,
    TEST_SIZE,
    RANDOM_STATE,
)

# Đường dẫn lưu model – FastAPI service sẽ load từ đây
MODEL_PATH = Path(__file__).resolve().parents[1] / "ml" / "model.joblib"

def train_and_save():
    print("\n" + "="*60)
    print("TRAIN & SAVE MODEL")
    print("="*60)

    # ── ĐỌC DỮ LIỆU ──────────────────────────────────────────────
    print(f"\n[1/5] Đọc dữ liệu từ '{CSV_FILE}'...")
    df = pd.read_csv(CSV_FILE)
    df = df.dropna(subset=['text', 'label'])
    df['label'] = df['label'].astype(str).str.strip().str.capitalize()
    df = df[df['label'].isin(VALID_LABELS)]
    print(f"  ✓ {len(df)} dòng hợp lệ")
    for label, count in df['label'].value_counts().items():
        print(f"    - {label}: {count} ({count/len(df)*100:.2f}%)")

    # ── TIỀN XỬ LÝ ────────────────────────────────────────────────
    print("\n[2/5] Tiền xử lý văn bản...")
    df['processed_text'] = df['text'].apply(preprocess_text)
    df = df[df['processed_text'].str.len() > 0]
    print(f"  ✓ {len(df)} văn bản hợp lệ sau tiền xử lý")

    # ── CHIA TRAIN/TEST ────────────────────────────────────────────
    print(f"\n[3/5] Chia train/test ({(1-TEST_SIZE)*100:.0f}/{TEST_SIZE*100:.0f})...")
    # X = df['processed_text'].values
    X = df['text'].values
    y = df['label'].values
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=TEST_SIZE, random_state=RANDOM_STATE, stratify=y
    )
    print(f"  ✓ Train: {len(X_train)} | Test: {len(X_test)}")

    # ── HUẤN LUYỆN ────────────────────────────────────────────────
    print("\n[4/5] Huấn luyện mô hình...")
    detector = SpamDetector()
    detector.train(X_train, y_train)
    detector.evaluate(X_test, y_test)

    # ── LƯU MODEL ─────────────────────────────────────────────────
    print(f"\n[5/5] Lưu model ra '{MODEL_PATH}'...")
    joblib.dump(detector, MODEL_PATH)
    size_kb = os.path.getsize(MODEL_PATH) / 1024
    print(f"  ✓ Lưu thành công! ({size_kb:.1f} KB)")
    print(f"\n  → Chạy FastAPI: uvicorn main:app --reload")
    print("="*60)


if __name__ == "__main__":
    train_and_save()