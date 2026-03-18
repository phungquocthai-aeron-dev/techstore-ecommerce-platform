import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { TokenService } from '../services/token.service';
import { AuthService } from '../services/auth.service';
import { map, catchError, of } from 'rxjs';

export const authGuard: CanActivateFn = () => {

  const tokenService = inject(TokenService);
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = tokenService.getToken();

  if (!token) {
    router.navigate(['/auth']);
    return of(false);
  }

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