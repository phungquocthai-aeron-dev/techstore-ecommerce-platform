import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TokenService } from '../services/token.service';
import { AuthService } from '../services/auth.service';
import { catchError, map, of } from 'rxjs';

/**
 * AuthGuard – kiểm tra token còn hợp lệ không.
 * Nếu chưa đăng nhập → redirect về /auth.
 */
export const authGuard: CanActivateFn = () => {
  const tokenService = inject(TokenService);
  const authService  = inject(AuthService);
  const router       = inject(Router);

  if (!tokenService.isLoggedIn()) {
    router.navigate(['/auth']);
    return false;
  }

  // Introspect để xác minh token còn hợp lệ phía server
  return authService.introspect().pipe(
    map(res => {
      if (res.result?.valid) {
        return true;
      }
      tokenService.removeToken();
      router.navigate(['/auth']);
      return false;
    }),
    catchError(() => {
      tokenService.removeToken();
      router.navigate(['/auth']);
      return of(false);
    })
  );
};