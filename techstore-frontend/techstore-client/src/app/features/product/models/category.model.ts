export interface CategoryResponse {
  id: number;
  name: string;
  categoryType: string;
  pcComponentType: string;
  description: string;
}

export interface CategoryCreateRequest {
  name: string;
  categoryType: string;
  pcComponentType?: string;
  description?: string;
}

export interface CategoryUpdateRequest {
  name?: string;
  categoryType?: string;
  pcComponentType?: string;
  description?: string;
}