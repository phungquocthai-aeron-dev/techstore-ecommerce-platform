// ─── Response (map StaffResponse.java) ───────────────────────────────────────
export interface StaffResponse {
  id: number;
  fullName: string;
  email: string;
  phone: string;
  status: string;
  roles: string;
}

// ─── Request (map StaffRequest.java) ─────────────────────────────────────────
export interface StaffRequest {
  id?: number;
  fullName?: string;
  email?: string;
  phone?: string;
  status?: string;
  roleNames?: string[];
}

// ─── Role update request (map StaffRoleUpdateRequest.java) ───────────────────
export interface StaffRoleUpdateRequest {
  roleNames: string[];
}