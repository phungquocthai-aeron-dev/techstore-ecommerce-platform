import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { TokenService } from '../services/token.service';

export const guestGuard: CanActivateFn = () => {

  const tokenService = inject(TokenService);
  const router = inject(Router);

  const token = tokenService.getToken();

  // Nếu đã đăng nhập thì không cho vào login/register
  if (token) {
    router.navigate(['/home']);
    return false;
  }

  return true;
};