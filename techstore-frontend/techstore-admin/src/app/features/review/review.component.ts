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
  content: [], page: 0, size: 10,
  totalElements: 0, totalPages: 0, last: true,
};

@Component({
  selector: 'app-review',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './review.component.html',
  styleUrls: ['./review.component.css']
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

  // stats (loaded separately for global counts)
  stats = { active: 0, hidden: 0, spam: 0, toxic: 0 };

  // toolbar
  filterStatus = '';
  searchForm: ReviewSearchRequest = {
    sortBy:  'createdAt',
    sortDir: 'DESC',
    page:    0,
    size:    10,
  };

  // status quick-actions
  readonly statusOptions: StatusOption[] = [
    { value: 'ACTIVE', label: 'Hoạt động',  icon: 'bi-check-circle' },
    { value: 'HIDDEN', label: 'Ẩn',          icon: 'bi-eye-slash' },
    { value: 'SPAM',   label: 'Spam',        icon: 'bi-exclamation-triangle' },
    { value: 'TOXIC',  label: 'Độc hại',     icon: 'bi-shield-exclamation' },
  ];

  // modal: reply
  showReplyModal  = false;
  selectedReview: ReviewResponse | null = null;
  editingReplyId: number | null = null;
  replyContent = '';

  // modal: delete review
  showDeleteReviewModal = false;
  deletingReview: ReviewResponse | null = null;

  // modal: delete reply
  showDeleteReplyModal = false;
  deletingReplyReview: ReviewResponse | null = null;

  constructor(private reviewService: ReviewService) {}

  ngOnInit(): void {
    this.loadStats();
    this.doSearch();
  }

  // ─── Load stats (counts per status) ──────────────────────────────

  private loadStats(): void {
    const statuses: Array<keyof typeof this.stats> = ['active', 'hidden', 'spam', 'toxic'];
    statuses.forEach(s => {
      this.reviewService.searchReviews({ status: s.toUpperCase(), size: 1 }).subscribe({
        next: res => { this.stats[s] = res.result?.totalElements ?? 0; }
      });
    });
  }

  // ─── Search / Filter ─────────────────────────────────────────────

  doSearch(resetPage = true): void {
    if (resetPage) this.currentPage = 0;
    this.isLoading = true;

    const req: ReviewSearchRequest = {
      ...this.searchForm,
      status:  this.filterStatus || undefined,
      page:    this.currentPage,
      size:    this.pageSize,
    };

    this.reviewService.searchReviews(req).subscribe({
      next: res => {
        this.page    = res.result ?? { ...DEFAULT_PAGE };
        this.reviews = this.page.content;
        this.isLoading = false;
      },
      error: () => {
        this.showAlert('Không thể tải danh sách bình luận.', 'error');
        this.isLoading = false;
      }
    });
  }

  quickFilter(status: string): void {
    this.filterStatus = status;
    this.doSearch();
  }

  resetSearch(): void {
    this.filterStatus = '';
    this.searchForm   = { sortBy: 'createdAt', sortDir: 'DESC', page: 0, size: this.pageSize };
    this.doSearch();
  }

  // ─── Pagination ───────────────────────────────────────────────────

  goPage(p: number): void {
    this.currentPage = p;
    this.doSearch(false);
  }

  pageNumbers(): number[] {
    const total = this.page.totalPages;
    const cur   = this.currentPage;
    const delta = 2;
    const start = Math.max(0, cur - delta);
    const end   = Math.min(total - 1, cur + delta);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  // ─── Status change (bằng updateReview với status mới) ────────────

  changeStatus(review: ReviewResponse, newStatus: string): void {
    // Gọi updateReview với nội dung giữ nguyên, chỉ đổi status
    // (Tuỳ theo backend — nếu BE có endpoint riêng /status thì thay ở đây)
    this.reviewService.updateReview(review.id, {
      content: review.content,
      rating:  review.rating,
      // status field nếu BE hỗ trợ trong UpdateReviewRequest
      ...(newStatus && { status: newStatus } as any)
    }).subscribe({
      next: res => {
        if (res.result) {
          const idx = this.reviews.findIndex(r => r.id === review.id);
          if (idx !== -1) this.reviews[idx] = res.result!;
        }
        this.showAlert(`Đã chuyển sang: ${this.statusLabelOf(newStatus)}`);
        this.loadStats();
      },
      error: () => this.showAlert('Cập nhật trạng thái thất bại.', 'error')
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
        this.page.totalElements--;
        this.loadStats();
        this.showAlert('Đã xoá bình luận.');
      },
      error: () => { this.isSaving = false; this.showAlert('Xoá thất bại.', 'error'); }
    });
  }

  // ─── Reply modal ──────────────────────────────────────────────────

  openReplyModal(r: ReviewResponse): void {
    this.selectedReview  = r;
    this.editingReplyId  = null;
    this.replyContent    = '';
    this.showReplyModal  = true;
  }

  openEditReply(r: ReviewResponse): void {
    if (!r.reply) return;
    this.selectedReview  = r;
    this.editingReplyId  = r.reply.id;
    this.replyContent    = r.reply.content;
    this.showReplyModal  = true;
  }

  closeReplyModal(): void {
    this.showReplyModal = false;
  }

  saveReply(): void {
    if (!this.replyContent.trim()) {
      this.showAlert('Nội dung phản hồi không được để trống.', 'error'); return;
    }
    if (!this.selectedReview) return;
    this.isSaving = true;

    const obs = this.editingReplyId
      ? this.reviewService.updateReply(this.editingReplyId, { content: this.replyContent })
      : this.reviewService.createReply(this.selectedReview.id, { content: this.replyContent });

    obs.subscribe({
      next: res => {
        this.isSaving = false;
        this.closeReplyModal();
        // cập nhật reply trong card
        const idx = this.reviews.findIndex(r => r.id === this.selectedReview!.id);
        if (idx !== -1 && res.result) this.reviews[idx].reply = res.result;
        this.showAlert(this.editingReplyId ? 'Cập nhật phản hồi thành công.' : 'Phản hồi đã được gửi.');
      },
      error: () => { this.isSaving = false; this.showAlert('Gửi phản hồi thất bại.', 'error'); }
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
        if (idx !== -1) this.reviews[idx] = { ...this.reviews[idx], reply: undefined };
        this.showAlert('Đã xoá phản hồi.');
      },
      error: () => { this.isSaving = false; this.showAlert('Xoá phản hồi thất bại.', 'error'); }
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
      ACTIVE: 'Hoạt động', HIDDEN: 'Đã ẩn', SPAM: 'Spam', TOXIC: 'Độc hại'
    };
    return map[s] ?? s;
  }

  // ─── Alert ────────────────────────────────────────────────────────

  showAlert(msg: string, type: 'success' | 'error' = 'success'): void {
    this.alertMsg  = msg;
    this.alertType = type;
    setTimeout(() => (this.alertMsg = ''), 3500);
  }
}