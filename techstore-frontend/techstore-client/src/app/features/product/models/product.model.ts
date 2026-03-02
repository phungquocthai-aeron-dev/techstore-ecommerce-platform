export interface ProductResponse {
  id: number;
  name: string;
  description: string;
  basePrice: number;

  performanceScore: number;
  powerConsumption: number;
  status: string;

  brand: {
    id: number;
    name: string;
  };

  category: {
    id: number;
    name: string;
  };

  images: ProductImageResponse[];
  specs: ProductSpecResponse[];
  variants: VariantResponse[];
}

export interface ProductImageResponse {
  id: number;
  url: string;
  isPrimary: boolean;
}

export interface ProductSpecResponse {
  id: number;
  specKey: string;
  specValue: string;
}

export interface VariantResponse {
  id: number;
  productId: number;
  color: string;
  price: number;
  stock: number;
  status: string;
  imageUrl: string;
  weight: number;
  name: string;
}

export interface ProductListResponse {
  id: number;
  name: string;
  basePrice: number;
  status: string;
  brandName: string;
  categoryName: string;
  primaryImage: string;
}