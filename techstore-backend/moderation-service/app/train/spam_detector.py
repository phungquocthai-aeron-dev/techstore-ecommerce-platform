# -*- coding: utf-8 -*-
"""
Spam Detection System using Multinomial Naive Bayes
Hệ thống phát hiện spam/toxic sử dụng Multinomial Naive Bayes
"""

import pandas as pd
import numpy as np
import re
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.metrics import (accuracy_score, precision_score, recall_score,
                             f1_score, classification_report, confusion_matrix)
import warnings
warnings.filterwarnings('ignore')

# ========================= CẤU HÌNH =========================
CSV_FILE       = '../data/comments_dataset.csv'
TEST_SIZE      = 0.2
RANDOM_STATE   = 42

# Nhãn hợp lệ
VALID_LABELS   = {'Spam', 'Valid', 'Toxic'}

VIETNAMESE_STOPWORDS = [
    'và', 'của', 'có', 'được', 'trong', 'là', 'với', 'để', 'các', 'một',
    'cho', 'này', 'đã', 'không', 'từ', 'theo', 'đến', 'khi', 'về', 'như',
    'những', 'hoặc', 'nếu', 'nhưng', 'vì', 'đang', 'bởi', 'thì', 'sẽ',
    'bị', 'do', 'nên', 'mà', 'còn', 'bằng', 'rằng', 'ở', 'lại', 'trên',
    'sau', 'đó', 'ra', 'thế', 'nào', 'nữa', 'hay', 'đây', 'vào', 'cũng', 'nó'
]

# Bad words – bổ sung tùy ý
BAD_WORDS = {
    "vcl", "vl", "cc", "dm", "cmm", "đm", "đmm",
    "clm", "cặc", "lồn", "đéo", "địt", "mẹ kiếp", "bố mày",
}

# Toxic threshold – nếu xác suất Toxic vượt ngưỡng này thì override
TOXIC_THRESHOLD = 0.35


# ========================= HÀM KIỂM TRA BAD WORDS =========================
def normalize_for_blacklist(text: str) -> str:
    """
    Chuẩn hóa text để bắt các biến thể evasion:
      v.c.l  →  vcl
      v c l  →  vcl
      vcllll →  vcll  (repeated char normalization)
      đmmmm  →  đmm
    """
    text = text.lower()
    # Bước 1: xóa các ký tự ngăn cách giữa các chữ cái (dấu chấm, gạch, space lẻ)
    # v.c.l → vcl | v-c-l → vcl | v c l → vcl
    text = re.sub(r'(?<=[a-záàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ])[^a-z0-9áàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ]+(?=[a-záàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ])', '', text)
    # Bước 2: repeated character normalization – giữ tối đa 2 ký tự liên tiếp
    # vcllllll → vcll | đmmmmmm → đmm
    text = re.sub(r'(.)\1{2,}', r'\1\1', text)
    return text


def contains_bad_word(text: str) -> bool:
    """
    Kiểm tra xem text có chứa bad word không.
    Kiểm tra trên cả text gốc lẫn text đã normalize để bắt evasion tricks.
    """
    viet_char = r'a-záàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ'

    # Kiểm tra trên 2 phiên bản: gốc (lower) và đã normalize
    texts_to_check = [text.lower(), normalize_for_blacklist(text)]

    for check_text in texts_to_check:
        for bw in BAD_WORDS:
            if ' ' in bw:
                # Cụm từ: tìm thẳng
                if bw in check_text:
                    return True
            else:
                # Từ đơn: dùng word boundary tiếng Việt
                pattern = (
                    r'(?<![' + viet_char + r'])' +
                    re.escape(bw) +
                    r'(?![' + viet_char + r'])'
                )
                if re.search(pattern, check_text):
                    return True
    return False


# ========================= HÀM TIỀN XỬ LÝ VĂN BẢN =========================
def preprocess_text(text: str) -> str:
    if not isinstance(text, str):
        return ""
    text = text.lower()
    text = re.sub(
        r'[^a-záàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ0-9\s]',
        ' ', text
    )
    words = text.split()
    words = [w for w in words if w not in VIETNAMESE_STOPWORDS and len(w) > 1]
    text = ' '.join(words)
    text = re.sub(r'\s+', ' ', text).strip()
    return text


# ========================= LỚP MÔ HÌNH =========================
class SpamDetector:
    """Phát hiện Spam / Toxic / Valid dùng Multinomial Naive Bayes và TF-IDF"""

    LABEL_CONFIG = {
        'Spam':  {'icon': '🚫', 'msg': 'Đây là SPAM!'},
        'Toxic': {'icon': '☠️',  'msg': 'Đây là nội dung ĐỘC HẠI (TOXIC)!'},
        'Valid': {'icon': '✅',  'msg': 'Đây là bình luận hợp lệ (VALID).'},
    }

    def __init__(self):
        self.vectorizer = TfidfVectorizer(
            max_features=5000,
            ngram_range=(1, 2),
            min_df=1,
            max_df=0.95
        )
        self.model       = MultinomialNB(alpha=1.0)
        self.is_trained  = False
        self._class_index = {}   # {"Spam": 0, "Toxic": 1, "Valid": 2}

    # ---------- TRAIN ----------
    def train(self, X_train, y_train):
        print("\n" + "="*60)
        print("BẮT ĐẦU HUẤN LUYỆN MÔ HÌNH")
        print("="*60)

        print("\n[1/3] Chuyển đổi văn bản thành TF-IDF features...")
        X_tfidf = self.vectorizer.fit_transform(X_train)
        print(f"  - Số mẫu train : {X_tfidf.shape[0]}")
        print(f"  - Số đặc trưng : {X_tfidf.shape[1]}")

        print("\n[2/3] Huấn luyện Multinomial Naive Bayes...")
        self.model.fit(X_tfidf, y_train)
        self._class_index = {c: i for i, c in enumerate(self.model.classes_)}

        print("\n[3/3] Hoàn thành huấn luyện!")
        self.is_trained = True
        print(f"  - Các nhãn đã học: {list(self.model.classes_)}")
        print("="*60)

    # ---------- PREDICT (batch) ----------
    def predict(self, X_test):
        if not self.is_trained:
            raise Exception("Mô hình chưa được huấn luyện!")
        return self.model.predict(self.vectorizer.transform(X_test))

    # ---------- PREDICT (single) ----------
    def predict_single(self, text: str):
        """
        Trả về (label: str, proba: np.ndarray) với 3 lớp bảo vệ:

        Layer 1 – Hard blacklist  : bad word → Toxic ngay lập tức
        Layer 2 – Threshold rule  : xác suất Toxic > TOXIC_THRESHOLD → Toxic
        Layer 3 – Model prediction: kết quả mô hình thuần
        """
        if not self.is_trained:
            raise Exception("Mô hình chưa được huấn luyện!")

        n_classes = len(self.model.classes_)

        # ── LAYER 1: Hard blacklist ──────────────────────────────────────
        if contains_bad_word(text):
            proba = np.full(n_classes, 0.01 / (n_classes - 1))
            toxic_idx = self._class_index.get("Toxic", 1)
            proba[toxic_idx] = 0.99
            print("[Layer 1] Bad word detected → Toxic (hard rule)")
            return "Toxic", proba

        # ── Model inference ─────────────────────────────────────────────
        processed = preprocess_text(text)
        tfidf     = self.vectorizer.transform([processed])
        prediction = self.model.predict(tfidf)[0]
        proba      = self.model.predict_proba(tfidf)[0]

        # ── LAYER 2: Threshold override ──────────────────────────────────
        toxic_idx = self._class_index.get("Toxic")
        if toxic_idx is not None and proba[toxic_idx] > TOXIC_THRESHOLD:
            print(f"[Layer 2] Toxic prob={proba[toxic_idx]:.3f} > {TOXIC_THRESHOLD} → override to Toxic")
            return "Toxic", proba

        # ── LAYER 3: Model result ────────────────────────────────────────
        return prediction, proba

    # ---------- EVALUATE ----------
    def evaluate(self, X_test, y_test):
        print("\n" + "="*60)
        print("ĐÁNH GIÁ MÔ HÌNH TRÊN TẬP TEST")
        print("="*60)

        y_pred = self.predict(X_test)
        labels = list(self.model.classes_)

        accuracy  = accuracy_score(y_test, y_pred)
        precision = precision_score(y_test, y_pred, average='macro', zero_division=0)
        recall    = recall_score(y_test, y_pred, average='macro', zero_division=0)
        f1        = f1_score(y_test, y_pred, average='macro', zero_division=0)

        print(f"\nKẾT QUẢ ĐÁNH GIÁ (macro avg):")
        print(f"  • Accuracy  : {accuracy:.4f}  ({accuracy*100:.2f}%)")
        print(f"  • Precision : {precision:.4f}  ({precision*100:.2f}%)")
        print(f"  • Recall    : {recall:.4f}  ({recall*100:.2f}%)")
        print(f"  • F1-Score  : {f1:.4f}  ({f1*100:.2f}%)")

        print("\n" + "-"*60)
        print("CLASSIFICATION REPORT:")
        print("-"*60)
        print(classification_report(y_test, y_pred, target_names=labels, zero_division=0))

        print("-"*60)
        print("CONFUSION MATRIX:")
        print("-"*60)
        cm    = confusion_matrix(y_test, y_pred, labels=labels)
        col_w = 10
        header = " " * 16 + "".join(f"{l:^{col_w}}" for l in labels)
        print(header)
        print(" " * 16 + "-" * (col_w * len(labels)))
        for i, row_label in enumerate(labels):
            row_str = f"Actual {row_label:<9}" + "".join(f"{cm[i][j]:^{col_w}}" for j in range(len(labels)))
            print(row_str)
        print("="*60)

        return accuracy, precision, recall, f1


# ========================= HÀM CHÍNH =========================
def main():
    print("\n" + "="*60)
    print("HỆ THỐNG PHÁT HIỆN SPAM / TOXIC")
    print("Sử dụng Multinomial Naive Bayes và TF-IDF")
    print("="*60)

    # ── BƯỚC 1: ĐỌC DỮ LIỆU ──
    print("\n[BƯỚC 1] Đọc dữ liệu từ file CSV...")
    try:
        df = pd.read_csv(CSV_FILE)
        print(f"  ✓ Đọc thành công {len(df)} dòng từ '{CSV_FILE}'")
        print(f"  ✓ Các cột: {list(df.columns)}")
    except FileNotFoundError:
        print(f"  ✗ KHÔNG TÌM THẤY FILE: {CSV_FILE}")
        print(f"  → File cần có 2 cột: 'text' và 'label'")
        print(f"  → Nhãn hợp lệ: {VALID_LABELS}")
        return
    except Exception as e:
        print(f"  ✗ LỖI KHI ĐỌC FILE: {e}")
        return

    if 'text' not in df.columns or 'label' not in df.columns:
        print("  ✗ LỖI: File CSV phải có 2 cột 'text' và 'label'")
        return

    df = df.dropna(subset=['text', 'label'])
    df['label'] = df['label'].astype(str).str.strip().str.capitalize()

    before  = len(df)
    df      = df[df['label'].isin(VALID_LABELS)]
    removed = before - len(df)
    if removed:
        print(f"  ⚠ Đã loại {removed} dòng có nhãn không hợp lệ.")

    print(f"  ✓ Sau làm sạch: {len(df)} dòng")
    print(f"\n  PHÂN BỐ NHÃN:")
    for label, count in df['label'].value_counts().items():
        print(f"    - {label}: {count} ({count/len(df)*100:.2f}%)")

    found_labels = set(df['label'].unique())
    missing = VALID_LABELS - found_labels
    if missing:
        print(f"\n  ⚠ Chú ý: Nhãn {missing} chưa có trong dataset.")

    # ── BƯỚC 2: TIỀN XỬ LÝ ──
    print("\n[BƯỚC 2] Tiền xử lý văn bản...")
    df['processed_text'] = df['text'].apply(preprocess_text)
    df = df[df['processed_text'].str.len() > 0]
    print(f"  ✓ Hoàn thành: {len(df)} văn bản hợp lệ")

    # ── BƯỚC 3: CHIA TRAIN/TEST ──
    print(f"\n[BƯỚC 3] Chia tập train/test ({(1-TEST_SIZE)*100:.0f}/{TEST_SIZE*100:.0f})...")
    X = df['processed_text'].values
    y = df['label'].values
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=TEST_SIZE, random_state=RANDOM_STATE, stratify=y
    )
    print(f"  ✓ Tập train: {len(X_train)} mẫu")
    print(f"  ✓ Tập test : {len(X_test)} mẫu")

    # ── BƯỚC 4: HUẤN LUYỆN ──
    detector = SpamDetector()
    detector.train(X_train, y_train)

    # ── BƯỚC 5: ĐÁNH GIÁ ──
    detector.evaluate(X_test, y_test)

    # ── BƯỚC 6: DỰ ĐOÁN VĂN BẢN MỚI ──
    print("\n" + "="*60)
    print("DỰ ĐOÁN VĂN BẢN MỚI")
    print("="*60)
    print("Nhập văn bản để kiểm tra (nhập 'quit' để thoát)")
    print("-"*60)

    classes = list(detector.model.classes_)

    while True:
        print("\n➤ Nhập văn bản: ", end="")
        user_input = input().strip()

        if user_input.lower() in ['quit', 'exit', 'q', 'thoát']:
            print("\n👋 Tạm biệt! Cảm ơn bạn đã sử dụng hệ thống.")
            break

        if not user_input:
            print("  ⚠ Văn bản không được để trống!")
            continue

        try:
            prediction, proba = detector.predict_single(user_input)
            cfg = SpamDetector.LABEL_CONFIG.get(prediction, {'icon': '❓', 'msg': prediction})

            print("\n" + "-"*60)
            print("📊 KẾT QUẢ DỰ ĐOÁN:")
            print("-"*60)
            print(f"  Văn bản  : \"{user_input}\"")
            print(f"  Kết quả  : {cfg['icon']} {prediction}")
            print(f"  Độ tin cậy:")
            for label, prob in zip(classes, proba):
                bar = "█" * int(prob * 20)
                print(f"    • {label:<6}: {prob*100:6.2f}%  {bar}")
            print(f"\n  → {cfg['msg']}")
            print("-"*60)

        except Exception as e:
            print(f"  ✗ LỖI KHI DỰ ĐOÁN: {e}")


# ========================= CHẠY =========================
if __name__ == "__main__":
    main()