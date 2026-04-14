import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../check-out/order.service';
import { ReviewService } from '../review/review.service';


import { CreateReviewRequest } from '../review/models/review-request.model';
import { CustomerOrderItemResponse, CustomerOrderResponse } from '../check-out/models/order.model';

type TabStatus = 'ALL' | 'PROCESSING' | 'READY_TO_SHIP' | 'SHIPPING' | 'DELIVERED' | 'CANCELLED' | 'RETURNED';

interface Tab {
  key: TabStatus;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './order.component.html',
  styleUrls: ['./order.component.css']
})
export class OrdersComponent implements OnInit {

  // ─── State ───────────────────────────────────────────
  customerId = 1; // TODO: lấy từ AuthService / localStorage

  activeTab: TabStatus = 'ALL';
  allOrders: CustomerOrderResponse[] = [];
  loading = false;

  // Alert
  alert: { type: 'success' | 'error'; message: string } | null = null;

  // Modal: chi tiết đơn hàng
  showDetailModal = false;
  selectedOrder: CustomerOrderResponse | null = null;

  // Modal: hủy đơn (chỉ PROCESSING)
  showCancelModal = false;
  cancellingOrderId: number | null = null;
  cancelReason = '';
  cancelNote = '';
  cancelling = false;

  // Modal: xác nhận đã nhận hàng (SHIPPING → DELIVERED)
  showConfirmDeliveredModal = false;
  confirmingOrderId: number | null = null;
  confirmingDelivered = false;

  // Modal: trả hàng
  showReturnModal = false;
  returningOrderId: number | null = null;
  returnReason = '';
  returnNote = '';
  returning = false;

  // Modal: đánh giá sản phẩm
  showReviewModal = false;
  reviewingItem: CustomerOrderItemResponse | null = null;
  reviewRating = 5;
  reviewContent = '';
  submittingReview = false;
  hoverRating = 0;

  // ─── Tabs config ─────────────────────────────────────
  tabs: Tab[] = [
    { key: 'ALL',           label: 'Tất cả',        icon: 'bi-grid-3x3-gap'      },
    { key: 'PROCESSING',    label: 'Đang xử lý',    icon: 'bi-gear'              },
    { key: 'READY_TO_SHIP', label: 'Sẵn sàng giao', icon: 'bi-box-seam'          },
    { key: 'SHIPPING',      label: 'Đang giao',      icon: 'bi-truck'             },
    { key: 'DELIVERED',     label: 'Đã giao',        icon: 'bi-bag-check'         },
    { key: 'CANCELLED',     label: 'Đã hủy',         icon: 'bi-x-circle'          },
    { key: 'RETURNED',      label: 'Trả hàng',       icon: 'bi-arrow-return-left' },
  ];

  cancelReasons = [
    'Tôi muốn thay đổi địa chỉ giao hàng',
    'Tôi muốn thay đổi sản phẩm / số lượng',
    'Tìm được giá tốt hơn ở nơi khác',
    'Đặt nhầm sản phẩm',
    'Thanh toán gặp vấn đề',
    'Khác'
  ];

  returnReasons = [
    'Sản phẩm bị lỗi / hư hỏng',
    'Sản phẩm không đúng mô tả',
    'Nhận sai sản phẩm',
    'Sản phẩm không đúng màu / size',
    'Tôi không còn cần sản phẩm này',
    'Khác'
  ];

  ratingLabels: Record<number, string> = {
    1: 'Rất tệ',
    2: 'Không hài lòng',
    3: 'Bình thường',
    4: 'Hài lòng',
    5: 'Xuất sắc'
  };

  constructor(
    private orderService: OrderService,
    private reviewService: ReviewService
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  // ─── Load & filter ────────────────────────────────────

  loadOrders(): void {
    this.loading = true;
    this.orderService.getOrdersByCustomer(this.customerId).subscribe({
      next: res => {
        this.allOrders = (res.result ?? []).sort(
          (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.loading = false;
      },
      error: () => {
        this.showAlert('error', 'Không thể tải danh sách đơn hàng!');
        this.loading = false;
      }
    });
  }

  get filteredOrders(): CustomerOrderResponse[] {
    if (this.activeTab === 'ALL') return this.allOrders;
    return this.allOrders.filter(o => o.status === this.activeTab);
  }

  countByStatus(status: TabStatus): number {
    if (status === 'ALL') return this.allOrders.length;
    return this.allOrders.filter(o => o.status === status).length;
  }

  selectTab(tab: TabStatus): void {
    this.activeTab = tab;
  }

  // ─── Helpers ─────────────────────────────────────────

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      PROCESSING:    'Đang xử lý',
      READY_TO_SHIP: 'Sẵn sàng giao',
      SHIPPING:      'Đang giao hàng',
      DELIVERED:     'Đã giao',
      CANCELLED:     'Đã hủy',
      RETURNED:      'Đã trả hàng',
    };
    return map[status] ?? status;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      PROCESSING:    'status-processing',
      READY_TO_SHIP: 'status-ready',
      SHIPPING:      'status-shipping',
      DELIVERED:     'status-delivered',
      CANCELLED:     'status-cancelled',
      RETURNED:      'status-returned',
    };
    return map[status] ?? '';
  }

  grandTotal(order: CustomerOrderResponse): number {
    return order.totalPrice + order.shippingFee;
  }

  firstItem(order: CustomerOrderResponse): CustomerOrderItemResponse | null {
    return order.items?.[0] ?? null;
  }

  extraItemCount(order: CustomerOrderResponse): number {
    return Math.max(0, (order.items?.length ?? 0) - 1);
  }

  hasUnreviewedItems(order: CustomerOrderResponse): boolean {
    return order.items?.some(i => !i.reviewed) ?? false;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('vi-VN').format(amount) + 'đ';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleDateString('vi-VN', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  // ─── Detail modal ─────────────────────────────────────

  openDetail(order: CustomerOrderResponse): void {
    this.selectedOrder = order;
    this.showDetailModal = true;
  }

  closeDetail(): void {
    this.showDetailModal = false;
    this.selectedOrder = null;
  }

  // ─── Cancel modal (chỉ PROCESSING) ───────────────────

  openCancel(orderId: number): void {
    this.cancellingOrderId = orderId;
    this.cancelReason = '';
    this.cancelNote = '';
    this.showCancelModal = true;
  }

  closeCancel(): void {
    this.showCancelModal = false;
    this.cancellingOrderId = null;
  }

  confirmCancel(): void {
    if (!this.cancelReason) {
      this.showAlert('error', 'Vui lòng chọn lý do hủy đơn!');
      return;
    }
    if (!this.cancellingOrderId) return;

    this.cancelling = true;
    this.orderService.cancelOrder(this.cancellingOrderId, 0).subscribe({
      next: () => {
        this.showAlert('success', 'Hủy đơn hàng thành công!');
        this.loadOrders();
        this.closeCancel();
        this.cancelling = false;
      },
      error: () => {
        this.showAlert('error', 'Hủy đơn thất bại, vui lòng thử lại!');
        this.cancelling = false;
      }
    });
  }

  // ─── Confirm Delivered (SHIPPING → DELIVERED) ─────────

  openConfirmDelivered(orderId: number): void {
    this.confirmingOrderId = orderId;
    this.showConfirmDeliveredModal = true;
  }

  closeConfirmDelivered(): void {
    this.showConfirmDeliveredModal = false;
    this.confirmingOrderId = null;
  }

  confirmDelivered(): void {
    if (!this.confirmingOrderId) return;
    this.confirmingDelivered = true;
    this.orderService.updateStatus(this.confirmingOrderId, 'DELIVERED').subscribe({
      next: () => {
        this.showAlert('success', 'Xác nhận đã nhận hàng thành công!');
        this.loadOrders();
        this.closeConfirmDelivered();
        this.confirmingDelivered = false;
      },
      error: () => {
        this.showAlert('error', 'Xác nhận thất bại, vui lòng thử lại!');
        this.confirmingDelivered = false;
      }
    });
  }

  // ─── Return modal ─────────────────────────────────────

  openReturn(orderId: number): void {
    this.returningOrderId = orderId;
    this.returnReason = '';
    this.returnNote = '';
    this.showReturnModal = true;
  }

  closeReturn(): void {
    this.showReturnModal = false;
    this.returningOrderId = null;
  }

  confirmReturn(): void {
    if (!this.returnReason) {
      this.showAlert('error', 'Vui lòng chọn lý do trả hàng!');
      return;
    }
    if (!this.returningOrderId) return;

    this.returning = true;
    this.orderService.updateStatus(this.returningOrderId, 'RETURNED').subscribe({
      next: () => {
        this.showAlert('success', 'Yêu cầu trả hàng đã được gửi!');
        this.loadOrders();
        this.closeReturn();
        this.returning = false;
      },
      error: () => {
        this.showAlert('error', 'Gửi yêu cầu trả hàng thất bại!');
        this.returning = false;
      }
    });
  }

  // ─── Review modal ─────────────────────────────────────

  /** Mở modal đánh giá — chỉ gọi khi item chưa reviewed */
  openReview(item: CustomerOrderItemResponse): void {
    if (item.reviewed) return;
    this.reviewingItem = item;
    this.reviewRating = 5;
    this.hoverRating = 0;
    this.reviewContent = '';
    this.showReviewModal = true;
  }

  closeReview(): void {
    this.showReviewModal = false;
    this.reviewingItem = null;
  }

  setRating(star: number): void {
    this.reviewRating = star;
  }

  get activeRating(): number {
    return this.hoverRating || this.reviewRating;
  }

  submitReview(): void {
    if (!this.reviewingItem) return;

    const req: CreateReviewRequest = {
      orderDetailId: this.reviewingItem.orderDetailId,
      rating: this.reviewRating,
      content: this.reviewContent.trim() || undefined
    };

    this.submittingReview = true;
    this.reviewService.createReview(req).subscribe({
      next: () => {
        this.showAlert('success', 'Cảm ơn bạn đã đánh giá sản phẩm!');
        // Cập nhật reviewed locally để UI phản ánh ngay lập tức
        if (this.reviewingItem) {
          this.reviewingItem.reviewed = true;
        }
        this.closeReview();
        this.submittingReview = false;
      },
      error: () => {
        this.showAlert('error', 'Gửi đánh giá thất bại, vui lòng thử lại!');
        this.submittingReview = false;
      }
    });
  }

  // ─── Reorder ──────────────────────────────────────────

  reorder(order: CustomerOrderResponse): void {
    // TODO: thêm tất cả items vào giỏ hàng
    this.showAlert('success', 'Đã thêm sản phẩm vào giỏ hàng!');
  }

  // ─── Alert ────────────────────────────────────────────

  showAlert(type: 'success' | 'error', message: string): void {
    this.alert = { type, message };
    setTimeout(() => { this.alert = null; }, 3500);
  }

  handleImgError(event: any) {
    event.target.src = 'images/no-product-image.jpg';
  }
}