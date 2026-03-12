// customer.model.ts
export interface CustomerResponse {
  id: number;
  email: string;
  fullName: string;
  phone: string;
  dob: string;
  avatarUrl: string;
  status: string;
  createdAt: string;
}

export interface CustomerRegisterRequest {
  email: string;
  password: string;
  passwordConfirm: string;
  fullName: string;
  phone: string;
  dob: string;
}

export interface CustomerUpdateRequest {
  fullName?: string;
  phone?: string;
  dob?: string;
}