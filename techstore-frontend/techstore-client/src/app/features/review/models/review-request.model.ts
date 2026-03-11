export interface CreateReviewRequest {
  orderDetailId: number;
  content?: string;
  rating: number;
}

export interface UpdateReviewRequest {
  content?: string;
  rating?: number;
}

export interface CreateReplyRequest {
  content: string;
}

export interface UpdateReplyRequest {
  content: string;
}