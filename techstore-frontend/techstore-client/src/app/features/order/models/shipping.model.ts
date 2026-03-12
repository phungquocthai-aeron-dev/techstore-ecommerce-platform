export interface ProvinceData {
  provinceId: number;
  provinceName: string;
  code: string;
  status: number;
}

export interface DistrictData {
  districtId: number;
  districtName: string;
  provinceId: number;
  status: number;
}

export interface WardData {
  wardCode: string;
  wardName: string;
  districtId: number;
}