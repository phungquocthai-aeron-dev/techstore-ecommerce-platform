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