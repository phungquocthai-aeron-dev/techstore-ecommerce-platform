import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ReviewService } from './review.service';
import { ReviewResponse } from './models/review.model';
import { ReviewSearchRequest } from './models/review-request.model';
import { PageResponse } from '../../shared/models/page-response.model';

interface StatusOption {
  value: string;
  label: string;
  icon: string;
}

const DEFAULT_PAGE: PageResponse<ReviewResponse> = {
  content: [],
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  last: true,
};

@Component({
  selector: 'app-review',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './review.component.html',
  styleUrls: ['./review.component.css'],
})
export class ReviewComponent implements OnInit {

  reviews: ReviewResponse[] = [];
  page: PageResponse<ReviewResponse> = { ...DEFAULT_PAGE };
  currentPage = 0;
  pageSize    = 10;

  isLoading = false;
  isSaving  = false;

  alertMsg  = '';
  alertType: 'success' | 'error' = 'success';

  stats = { active: 0, hidden: 0, spam: 0, toxic: 0 };

  filterStatus = '';
  isFiltering  = false;

  // form tìm kiếm — productId thêm mới
  searchForm: ReviewSearchRequest = {
    productId: undefined,
    sortBy:    'createdAt',
    sortDir:   'DESC',
    page:      0,
    size:      10,
  };

  readonly statusOptions: StatusOption[] = [
    { value: 'ACTIVE', label: 'Hoạt động', icon: 'bi-check-circle' },
    { value: 'HIDDEN', label: 'Ẩn',        icon: 'bi-eye-slash' },
    { value: 'SPAM',   label: 'Spam',      icon: 'bi-exclamation-triangle' },
    { value: 'TOXIC',  label: 'Độc hại',   icon: 'bi-shield-exclamation' },
  ];

  showReplyModal  = false;
  selectedReview: ReviewResponse | null = null;
  editingReplyId: number | null = null;
  replyContent = '';

  showDeleteReviewModal = false;
  deletingReview: ReviewResponse | null = null;

  showDeleteReplyModal = false;
  deletingReplyReview: ReviewResponse | null = null;

  constructor(private reviewService: ReviewService) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadReviews(); // ban đầu lấy tất cả
  }

  trackById(_index: number, item: ReviewResponse): number {
    return item.id;
  }

  // ─── Kiểm tra có đang filter thực sự không ───────────────────────

  private hasActiveFilter(): boolean {
    return !!(
      this.filterStatus ||
      this.searchForm.keyword ||
      this.searchForm.rating != null ||
      this.searchForm.hasReply != null ||
      this.searchForm.productId != null
    );
  }

  // ─── Load ban đầu / reset (getAllReviews) ─────────────────────────

  loadReviews(resetPage = true): void {
    if (resetPage) this.currentPage = 0;
    this.isFiltering = false;
    this.isLoading   = true;

    this.reviewService.getAllReviews(this.currentPage, this.pageSize).subscribe({
      next: res => {
        this.page    = res.result ?? { ...DEFAULT_PAGE };
        this.reviews = this.page.content ?? [];
        this.isLoading = false;
      },
      error: () => {
        this.showAlert('Không thể tải danh sách bình luận.', 'error');
        this.isLoading = false;
      },
    });
  }

  // ─── Search / Filter (searchReviews) ─────────────────────────────

  doSearch(resetPage = true): void {
    // Nếu không có filter nào → về getAllReviews
    if (!this.hasActiveFilter()) {
      this.loadReviews(resetPage);
      return;
    }

    if (resetPage) this.currentPage = 0;
    this.isFiltering = true;
    this.isLoading   = true;

    const req: ReviewSearchRequest = {
      ...this.searchForm,
      status: this.filterStatus || undefined,
      page:   this.currentPage,
      size:   this.pageSize,
    };

    this.reviewService.searchReviews(req).subscribe({
      next: res => {
        this.page    = res.result ?? { ...DEFAULT_PAGE };
        this.reviews = this.page.content ?? [];
        this.isLoading = false;
      },
      error: () => {
        this.showAlert('Không thể tải danh sách bình luận.', 'error');
        this.isLoading = false;
      },
    });
  }

  // card stat: status rỗng → reset về tất cả, có status → filter
  quickFilter(status: string): void {
    this.filterStatus = status;
    if (!status && !this.hasActiveFilter()) {
      this.loadReviews();
    } else {
      this.doSearch();
    }
  }

  resetSearch(): void {
    this.filterStatus = '';
    this.searchForm   = {
      productId: undefined,
      sortBy:    'createdAt',
      sortDir:   'DESC',
      page:      0,
      size:      this.pageSize,
    };
    this.loadReviews(); // reset về getAllReviews
  }

  // ─── Pagination ───────────────────────────────────────────────────

  goPage(p: number): void {
    this.currentPage = p;
    this.isFiltering ? this.doSearch(false) : this.loadReviews(false);
  }

  pageNumbers(): number[] {
    const total = this.page.totalPages;
    const cur   = this.currentPage;
    const delta = 2;
    const start = Math.max(0, cur - delta);
    const end   = Math.min(total - 1, cur + delta);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  // ─── Stats ────────────────────────────────────────────────────────

  private loadStats(): void {
    const statuses: Array<keyof typeof this.stats> = ['active', 'hidden', 'spam', 'toxic'];
    statuses.forEach(s => {
      this.reviewService
        .searchReviews({ status: s.toUpperCase(), page: 0, size: 1 })
        .subscribe({
          next: res => { this.stats[s] = res.result?.totalElements ?? 0; },
          error: () => {},
        });
    });
  }

  // ─── Status change ────────────────────────────────────────────────

  changeStatus(review: ReviewResponse, newStatus: string): void {
    this.reviewService.updateReviewStatus(review.id, newStatus).subscribe({
      next: res => {
        const idx = this.reviews.findIndex(r => r.id === review.id);
        if (idx !== -1) {
          this.reviews[idx] = res.result
            ? { ...this.reviews[idx], ...res.result }
            : { ...this.reviews[idx], status: newStatus };
        }
        this.showAlert(`Đã chuyển sang: ${this.statusLabelOf(newStatus)}`);
        this.loadStats();
      },
      error: () => this.showAlert('Cập nhật trạng thái thất bại.', 'error'),
    });
  }

  // ─── Delete review ────────────────────────────────────────────────

  confirmDeleteReview(r: ReviewResponse): void {
    this.deletingReview       = r;
    this.showDeleteReviewModal = true;
  }

  deleteReview(): void {
    if (!this.deletingReview) return;
    this.isSaving = true;

    this.reviewService.deleteReview(this.deletingReview.id).subscribe({
      next: () => {
        this.isSaving = false;
        this.showDeleteReviewModal = false;
        this.reviews = this.reviews.filter(r => r.id !== this.deletingReview!.id);
        this.page = { ...this.page, totalElements: Math.max(0, this.page.totalElements - 1) };
        this.loadStats();
        this.showAlert('Đã xoá bình luận.');
      },
      error: () => {
        this.isSaving = false;
        this.showAlert('Xoá thất bại.', 'error');
      },
    });
  }

  // ─── Reply modal ──────────────────────────────────────────────────

  openReplyModal(r: ReviewResponse): void {
    this.selectedReview = r;
    this.editingReplyId = null;
    this.replyContent   = '';
    this.showReplyModal = true;
  }

  openEditReply(r: ReviewResponse): void {
    if (!r.reply) return;
    this.selectedReview = r;
    this.editingReplyId = r.reply.id;
    this.replyContent   = r.reply.content;
    this.showReplyModal = true;
  }

  closeReplyModal(): void {
    this.showReplyModal = false;
    this.selectedReview = null;
    this.editingReplyId = null;
    this.replyContent   = '';
  }

  saveReply(): void {
    if (!this.replyContent.trim()) {
      this.showAlert('Nội dung phản hồi không được để trống.', 'error');
      return;
    }
    if (!this.selectedReview) return;
    this.isSaving = true;

    const obs = this.editingReplyId
      ? this.reviewService.updateReply(this.editingReplyId, { content: this.replyContent })
      : this.reviewService.createReply(this.selectedReview.id, { content: this.replyContent });

    const reviewId = this.selectedReview.id;

    obs.subscribe({
      next: res => {
        this.isSaving = false;
        const idx = this.reviews.findIndex(r => r.id === reviewId);
        if (idx !== -1 && res.result) {
          this.reviews[idx] = { ...this.reviews[idx], reply: res.result };
        }
        this.showAlert(this.editingReplyId ? 'Cập nhật phản hồi thành công.' : 'Phản hồi đã được gửi.');
        this.closeReplyModal();
      },
      error: () => {
        this.isSaving = false;
        this.showAlert('Gửi phản hồi thất bại.', 'error');
      },
    });
  }

  // ─── Delete reply ─────────────────────────────────────────────────

  confirmDeleteReply(r: ReviewResponse): void {
    this.deletingReplyReview  = r;
    this.showDeleteReplyModal = true;
  }

  deleteReply(): void {
    const r = this.deletingReplyReview;
    if (!r?.reply) return;
    this.isSaving = true;

    this.reviewService.deleteReply(r.reply.id).subscribe({
      next: () => {
        this.isSaving = false;
        this.showDeleteReplyModal = false;
        const idx = this.reviews.findIndex(x => x.id === r.id);
        if (idx !== -1) {
          this.reviews[idx] = {
            ...this.reviews[idx],
            reply: { ...this.reviews[idx].reply!, status: 'DELETED' },
          };
        }
        this.showAlert('Đã xoá phản hồi.');
      },
      error: () => {
        this.isSaving = false;
        this.showAlert('Xoá phản hồi thất bại.', 'error');
      },
    });
  }

  // ─── Helpers ──────────────────────────────────────────────────────

  starsArray(rating: number): string[] {
    return Array.from({ length: 5 }, (_, i) => i < rating ? 'full' : 'empty');
  }

  getInitial(name?: string): string {
    if (!name) return '?';
    return name.trim().charAt(0).toUpperCase();
  }

  statusLabel(s: string): string {
    return this.statusLabelOf(s);
  }

  private statusLabelOf(s: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'Hoạt động',
      HIDDEN: 'Đã ẩn',
      SPAM:   'Spam',
      TOXIC:  'Độc hại',
    };
    return map[s] ?? s;
  }

  showAlert(msg: string, type: 'success' | 'error' = 'success'): void {
    this.alertMsg  = msg;
    this.alertType = type;
    setTimeout(() => (this.alertMsg = ''), 3500);
  }
}