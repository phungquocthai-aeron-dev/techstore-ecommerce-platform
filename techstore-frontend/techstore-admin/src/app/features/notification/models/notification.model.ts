// admin-notification.model.ts

export interface NotificationResponse {
  id: string;
  userId: string;
  title: string;
  content: string;
  createdDate: string;
  modifiedDate: string;
  isRead: boolean;
}

export interface NotificationPageResponse {
  currentPage: number;
  totalPages: number;
  pageSize: number;
  totalElements: number;
  data: NotificationResponse[];
}

export interface NotificationRequest {
  title: string;
  content: string;
  userId?: number; // 0 = broadcast to all
}