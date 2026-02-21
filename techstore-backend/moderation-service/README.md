# Spam Detection Microservice

FastAPI service phát hiện **Spam / Toxic / Valid** bằng Multinomial Naive Bayes + TF-IDF.  
Tích hợp hoàn toàn với hệ thống Spring Boot microservice qua **JWT Bearer Token**.

---

## Cấu trúc

```
spam-detection-service/
├── main.py                     # FastAPI app entry point
├── requirements.txt
├── Dockerfile
├── .env.example
└── app/
    ├── core/
    │   ├── config.py           # Settings (env vars)
    │   └── security.py         # JWT decode/verify ← tương tự CustomJwtDecoder
    ├── dto/
    │   ├── request.py          # PredictRequest, PredictResult
    │   └── response.py         # ApiResponse<T>, ErrorCode ← giống Spring Boot
    ├── ml/
    │   ├── model.py            # SpamDetector (train, load, predict)
    │   └── saved_model/        # model.pkl + vectorizer.pkl (auto-generated)
    └── routers/
        ├── health.py           # GET /internal/health  (PUBLIC)
        └── predict.py          # POST /predict         (AUTHENTICATED)
```

---

## So sánh với Spring Boot

| Spring Boot                        | FastAPI Python                      |
|------------------------------------|-------------------------------------|
| `CustomJwtDecoder.decode()`        | `security.decode_token()`           |
| `JwtAuthenticationEntryPoint`      | `_build_401()` → HTTP 401 + ApiResponse |
| `.anyRequest().authenticated()`    | `Depends(get_current_user)`         |
| `PUBLIC_ENDPOINTS = /internal/**`  | Router `/internal/*` không có Depends |
| `ApiResponse<T>`                   | `ApiResponse[T]` (Pydantic Generic) |
| `ErrorCode` enum                   | `ErrorCode` enum                    |

---

## Cài đặt & Chạy

### 1. Cài dependencies
```bash
pip install -r requirements.txt
```

### 2. Cấu hình `.env`
```bash
cp .env.example .env
# Sửa JWT_SECRET_KEY cho khớp với Identity Service
```

### 3. Đặt file dataset
```
spam-detection-service/
└── comments_dataset.csv   # cột: text, label (Spam/Toxic/Valid)
```

### 4. Chạy service
```bash
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

Lần đầu chạy sẽ **tự động train** từ CSV và lưu model.  
Lần sau sẽ **tải model đã lưu** (nhanh hơn).

---

## API

### `POST /predict` *(yêu cầu JWT)*
```http
POST /predict
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "content": "Mua ngay kẻo hết! Giảm giá 90%!!!"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Success",
  "result": {
    "label": "Spam",
    "confidence": 0.93,
    "probabilities": {
      "Spam": 0.93,
      "Toxic": 0.04,
      "Valid": 0.03
    },
    "processed_text": "mua kẻo hết giảm giá"
  }
}
```

### `GET /internal/health` *(public)*
```json
{
  "code": 1000,
  "message": "Success",
  "result": { "status": "UP", "service": "spam-detection-service" }
}
```

### `GET /internal/model/info` *(public)*
```json
{
  "code": 1000,
  "message": "Success",
  "result": {
    "is_trained": true,
    "classes": ["Spam", "Toxic", "Valid"],
    "algorithm": "Multinomial Naive Bayes + TF-IDF"
  }
}
```

---

## Gọi từ Spring Boot service

```java
// Ví dụ dùng WebClient trong Spring Boot
@Service
public class SpamDetectionClient {

    private final WebClient webClient;

    public SpamDetectionClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://spam-detection-service:8000").build();
    }

    public PredictResult checkContent(String content, String jwtToken) {
        return webClient.post()
                .uri("/predict")
                .header("Authorization", "Bearer " + jwtToken)
                .bodyValue(Map.of("content", content))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PredictResult>>() {})
                .map(ApiResponse::getResult)
                .block();
    }
}
```

---

## Docker

```bash
docker build -t spam-detection-service .
docker run -p 8000:8000 \
  -e JWT_SECRET_KEY=your-shared-key \
  -v $(pwd)/comments_dataset.csv:/app/comments_dataset.csv \
  spam-detection-service
```

---

## Lưu ý bảo mật

- `JWT_SECRET_KEY` **phải giống hệt** key dùng để ký JWT trong Identity Service.  
- Nếu Identity Service dùng **RS256** (public/private key), thay `JWT_ALGORITHM=RS256` và cung cấp `JWT_PUBLIC_KEY` trong `security.py`.
- Swagger UI có tại: `http://localhost:8000/docs`
