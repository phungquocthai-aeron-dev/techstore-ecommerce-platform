export interface AddressResponse {
  id: number;
  address: string;
  provinceId: number;
  provinceName: string;
  districtId: number;
  districtName: string;
  wardCode: string;
  wardName: string;
  status: boolean;
  customerId: number;
}

export interface AddressRequest {
  address: string;
  provinceId: number;
  districtId: number;
  wardCode: string;
}