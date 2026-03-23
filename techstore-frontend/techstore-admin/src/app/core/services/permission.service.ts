import { Injectable } from '@angular/core';
import { StaffService } from '../../features/staff/staff.service';

export type AppRole = 'ADMIN' | 'SALES_STAFF' | 'WAREHOUSE_STAFF';

/**
 * Danh sách ACTION được phép theo role.
 * Convention: "<module>:<action>"
 *
 * Các action hiện tại:
 *   product:create        — Thêm sản phẩm mới
 *   product:edit          — Sửa thông tin sản phẩm
 *   product:change-status — Đổi trạng thái sản phẩm
 *   product:disable       — Vô hiệu hóa sản phẩm
 *   product:export        — Xuất Excel
 *   variant:add           — Thêm biến thể
 *   variant:edit          — Sửa biến thể
 *   variant:delete        — Xóa biến thể
 *   variant:image         — Cập nhật ảnh biến thể
 *
 * Thêm action mới → chỉ cần bổ sung vào đây, không cần sửa component.
 */
const ROLE_ACTIONS: Record<AppRole, string[] | '*'> = {
  ADMIN: '*', // Tất cả quyền

  SALES_STAFF: [
    // Sản phẩm: chỉ xem + xuất, KHÔNG tạo/sửa/xóa/đổi trạng thái
    'product:export',

    // Biến thể: không có quyền gì
  ],

  WAREHOUSE_STAFF: [
    // // Sản phẩm: toàn quyền (quản lý kho)
    // 'product:create',
    // 'product:edit',
    // 'product:change-status',
    // 'product:disable',
    // 'product:export',

    // // Biến thể: toàn quyền
    // 'variant:add',
    // 'variant:edit',
    // 'variant:delete',
    // 'variant:image',

    'product:export',

  ],
};

// ─── Route permissions ────────────────────────────────────────────────────────

const ROLE_ALLOWED_ROUTES: Record<AppRole, string[]> = {
  ADMIN: ['*'],
  SALES_STAFF: [
    'dashboard',
    'home',
    'categories',
    'brands',
    'orders',
    'coupons',
    'customers',
    'reviews'
  ],
  WAREHOUSE_STAFF: [
    'home',
    'warehouses',
    'suppliers',
  ],
};

// ─────────────────────────────────────────────────────────────────────────────

@Injectable({
  providedIn: 'root'
})
export class PermissionService {

  constructor(private staffService: StaffService) {}

  // ── Helpers ──────────────────────────────────────────────────────

  parseRoles(rolesStr: string | null | undefined): AppRole[] {
    if (!rolesStr?.trim()) return [];
    return rolesStr.trim().split(/\s+/).filter(r => r.length > 0) as AppRole[];
  }

  getCurrentRoles(): AppRole[] {
    return this.parseRoles(this.staffService.currentStaff?.roles);
  }

  // ── Route permissions ─────────────────────────────────────────────

  canAccess(routePath: string): boolean {
    const roles = this.getCurrentRoles();
    if (roles.length === 0) return false;

    return roles.some(role => {
      const allowed = ROLE_ALLOWED_ROUTES[role];
      if (!allowed) return false;
      if (allowed.includes('*')) return true;
      return allowed.some(p => routePath === p || routePath.startsWith(p + '/'));
    });
  }

  // ── Action permissions ────────────────────────────────────────────

  /**
   * Kiểm tra user có được thực hiện action không.
   * Chỉ cần 1 role trong danh sách có quyền là được.
   *
   * @example
   * canDo('product:create')  // true với ADMIN & WAREHOUSE_STAFF
   * canDo('product:edit')    // false với SALES_STAFF
   */
  canDo(action: string): boolean {
    const roles = this.getCurrentRoles();
    if (roles.length === 0) return false;

    return roles.some(role => {
      const actions = ROLE_ACTIONS[role];
      if (!actions) return false;
      if (actions === '*') return true;          // ADMIN
      return actions.includes(action);
    });
  }

  // ── Shorthand helpers (dùng trong template cho gọn) ───────────────

  /** Có thể tạo/sửa/xóa bất kỳ thứ gì (ADMIN hoặc WAREHOUSE_STAFF) */
  get canWrite(): boolean {
    return this.hasAnyRole('ADMIN', 'WAREHOUSE_STAFF');
  }

  // ── Role helpers ──────────────────────────────────────────────────

  hasRole(role: AppRole): boolean {
    return this.getCurrentRoles().includes(role);
  }

  hasAnyRole(...roles: AppRole[]): boolean {
    const current = this.getCurrentRoles();
    return roles.some(r => current.includes(r));
  }

  getDefaultRoute(): string {
    if (this.hasAnyRole('ADMIN', 'SALES_STAFF')) return '/dashboard';
    if (this.hasRole('WAREHOUSE_STAFF'))          return '/home';
    return '/auth';
  }

  // ── Display helpers ───────────────────────────────────────────────

  getRoleLabel(rolesStr: string): string {
    const roles = this.parseRoles(rolesStr);
    if (roles.includes('ADMIN'))           return 'Quản trị viên';
    if (roles.includes('SALES_STAFF'))     return 'Nhân viên bán hàng';
    if (roles.includes('WAREHOUSE_STAFF')) return 'Nhân viên kho';
    return rolesStr || 'Nhân viên';
  }

  getRoleBadgeClass(rolesStr: string): string {
    const roles = this.parseRoles(rolesStr);
    if (roles.includes('ADMIN'))           return 'role-admin';
    if (roles.includes('SALES_STAFF'))     return 'role-sales';
    if (roles.includes('WAREHOUSE_STAFF')) return 'role-warehouse';
    return 'role-default';
  }
}