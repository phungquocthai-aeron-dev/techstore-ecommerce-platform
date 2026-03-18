export interface WarehouseCreateRequest {
  name: string;
  maxCapacity: string;
  unitCapacity: string;
  addressId: string;
}

export interface WarehouseUpdateRequest {
  name?: string;
  maxCapacity?: string;
  unitCapacity?: string;
  status?: string;
  addressId?: string;
}