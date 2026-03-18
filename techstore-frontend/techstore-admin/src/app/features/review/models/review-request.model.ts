export interface CreateReviewRequest {
  orderDetailId: number;
  content: string;
  rating: number; // 1–5
}

export interface UpdateReviewRequest {
  content: string;
  rating: number;
}

export interface CreateReplyRequest {
  content: string;
}

export interface UpdateReplyRequest {
  content: string;
}

export interface ReviewSearchRequest {
  productId?: number;
  customerId?: number;
  rating?: number;       // 1–5
  status?: string;       // 'ACTIVE' | 'DELETED'
  hasReply?: boolean;
  keyword?: string;
  sortBy?: 'createdAt' | 'rating';
  sortDir?: 'ASC' | 'DESC';
  page?: number;
  size?: number;
}

export interface ReplySearchRequest {
  staffId?: number;
  status?: string;       // 'ACTIVE' | 'DELETED'
  keyword?: string;
  sortDir?: 'ASC' | 'DESC';
  page?: number;
  size?: number;
}