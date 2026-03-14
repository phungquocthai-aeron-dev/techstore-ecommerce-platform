export interface ProvinceData {
  ProvinceID: number;
  ProvinceName: string;
  Code: string;
  Status: number;
}

export interface DistrictData {
  DistrictID: number;
  DistrictName: string;
  ProvinceId: number;
  Status: number;
}

export interface WardData {
  WardCode: string;
  WardName: string;
  DistrictId: number;
}