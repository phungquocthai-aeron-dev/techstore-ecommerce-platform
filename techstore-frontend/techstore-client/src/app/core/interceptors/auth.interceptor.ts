import { HttpInterceptorFn, HttpErrorResponse, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError, BehaviorSubject, take } from 'rxjs';
import { TokenService } from '../services/token.service';
import { AuthService } from '../services/auth.service';

// ─────────────────────────────────────────────────────────────────
// Dùng `undefined` thay `null` → bỏ được filter(), tránh bug treo
//
// Luồng:
//   undefined  = đang refresh (chưa có token mới)
//   string     = refresh thành công, token mới sẵn sàng
//
// Khi refresh THẤT BẠI: emit `undefined` → take(1) complete ngay
// → switchMap nhận undefined → throwError → request chờ nhận lỗi
// ─────────────────────────────────────────────────────────────────
let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | undefined>(undefined);

// ─── Các request đang chờ: nhận giá trị đầu tiên từ subject (dù là gì)
// rồi tự quyết định retry hay throw error
function waitForNewTokenAndRetry(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  return refreshTokenSubject.pipe(
    take(1),                          // không filter → nhận cả undefined
    switchMap(token => {
      if (!token) {
        // Refresh đã thất bại → không retry, trả lỗi để request tự xử lý
        return throwError(() => new Error('Phiên đăng nhập đã hết hạn'));
      }
      return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
    })
  );
}

// ─── Public endpoints không cần auth và không được refresh ───────
const PUBLIC_URLS = ['identity/auth/refresh', 'identity/auth/token', 'identity/auth/register', 'identity/auth/introspect', 'chatbot/session'];

function isPublicUrl(url: string): boolean {
  console.warn(url)
  return PUBLIC_URLS.some(path => url.includes(path));
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router       = inject(Router);
  const tokenService = inject(TokenService);
  const authService  = inject(AuthService);

  // Bỏ qua public endpoints → tránh loop vô hạn và gọi refresh không cần thiết
  if (isPublicUrl(req.url)) {
    return next(req);
  }

  const token = tokenService.getToken();
  console.log("Gửi token " + token)
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
console.log(error)
      // Chỉ xử lý 401 khi đang có token (còn lại trả thẳng)
      if (error.status !== 401 || !tokenService.getToken()) {
        return throwError(() => error);
      }

      // ── Đang có request refresh rồi → chờ subject emit rồi retry hoặc throw ──
      if (isRefreshing) {
        return waitForNewTokenAndRetry(authReq, next);
      }

      // ── Bắt đầu refresh ──
      isRefreshing = true;
      refreshTokenSubject.next(undefined); // báo hiệu "đang refresh"

      return authService.refreshCustomerToken().pipe(
        switchMap(res => {
          isRefreshing = false;
          const newToken = res.result?.token;

          if (!newToken) {
            // Server trả 200 nhưng không có token → coi như thất bại
            refreshTokenSubject.next(undefined); // giải phóng request đang chờ với undefined
            tokenService.removeToken();
            // router.navigate(['/home']);
            return throwError(() => new Error('Refresh token không hợp lệ'));
          }

          // Phát token mới → các request đang chờ tự retry
          refreshTokenSubject.next(newToken);

          // Retry chính request này
          return next(authReq.clone({ setHeaders: { Authorization: `Bearer ${newToken}` } }));
        }),
        catchError(refreshError => {
          isRefreshing = false;

          // ✅ Emit undefined (không phải null) để take(1) trong waitForNewTokenAndRetry
          // complete ngay → switchMap nhận undefined → throwError
          // → các request đang chờ nhận lỗi sạch, không bị treo
          refreshTokenSubject.next(undefined);

          tokenService.removeToken();
          // router.navigate(['/home']);
          return throwError(() => refreshError);
        })
      );
    })
  );
};