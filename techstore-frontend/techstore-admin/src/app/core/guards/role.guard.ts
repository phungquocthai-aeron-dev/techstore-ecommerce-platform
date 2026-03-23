import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { PermissionService } from '../services/permission.service';
import { StaffService } from '../../features/staff/staff.service';
import { TokenService } from '../services/token.service';
import { catchError, map, of} from 'rxjs';
import { jwtDecode } from 'jwt-decode';

/**
 * RoleGuard – kiểm tra role có quyền truy cập route không.
 * Nếu staff chưa được load (refresh trang) → load lại từ token rồi kiểm tra.
 */
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const permService  = inject(PermissionService);
  const staffService = inject(StaffService);
  const tokenService = inject(TokenService);
  const router       = inject(Router);

  // Lấy path từ route
  const routePath = route.routeConfig?.path ?? '';

  // Nếu staff đã load sẵn trong state → kiểm tra ngay
  if (staffService.currentStaff) {
    return checkPermission(routePath, permService, router);
  }

  // Chưa có state (refresh trang) → decode token lấy id rồi load
  const token = tokenService.getToken();
  if (!token) {
    router.navigate(['/auth']);
    return of(false);
  }

  try {
    const decoded: any = jwtDecode(token);
    const staffId: number = decoded?.sub ? parseInt(decoded.sub) : decoded?.id;

    if (!staffId) {
      router.navigate(['/auth']);
      return of(false);
    }

    return staffService.loadCurrentStaff(staffId).pipe(
      map(() => checkPermission(routePath, permService, router)),
      catchError(() => {
        router.navigate(['/auth']);
        return of(false);
      })
    );
  } catch {
    router.navigate(['/auth']);
    return of(false);
  }
};

function checkPermission(
  routePath: string,
  permService: PermissionService,
  router: Router
): boolean {
  if (permService.canAccess(routePath)) {
    return true;
  }
  // Redirect về route mặc định của role
  router.navigate([permService.getDefaultRoute()]);
  return false;
}