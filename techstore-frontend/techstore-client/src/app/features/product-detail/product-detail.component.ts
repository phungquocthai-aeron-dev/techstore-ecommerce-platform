import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { ProductService } from '../product/product.service';
import { CartService } from '../cart/cart.service';
import { ReviewService } from '../review/review.service';

import { ProductResponse, VariantResponse } from '../product//models/product.model';
import { ReviewResponse } from '../review/models/review.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { OrderItem } from '../check-out/check-out.component';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent implements OnInit, OnDestroy {

  // ─── State ───────────────────────────────────────────────
  product: ProductResponse | null = null;
  selectedVariant: VariantResponse | null = null;
  selectedMainImage = '';
  activeTab: 'specs' | 'description' | 'warranty' = 'specs';

  quantity = 1;

  reviews: ReviewResponse[] = [];
  reviewPage = 0;
  reviewSize = 5;
  reviewTotal = 0;
  reviewFilter: number | undefined = undefined;
  reviewLoading = false;

  loading = false;
  cartLoading = false;
  error = '';
  cartMessage = '';

  modalImageSrc = '';
  showModal = false;

  private destroy$ = new Subject<void>();

  // ─── Constructor ─────────────────────────────────────────
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    private reviewService: ReviewService
  ) {}

  // ─── Lifecycle ───────────────────────────────────────────
  ngOnInit(): void {
    this.route.paramMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const id = Number(params.get('id'));
        if (id) {
          this.loadProduct(id);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ─── Load Product ─────────────────────────────────────────
  loadProduct(id: number): void {
    this.loading = true;
    this.error = '';

    this.productService.getById(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.product = res.result ?? null;
          if (this.product?.variants?.length) {
            this.selectVariant(this.product.variants[0]);
          }
          const primaryImg = this.product?.images?.find(img => img.isPrimary);
          this.selectedMainImage = primaryImg?.url || this.product?.images?.[0]?.url || '';
          this.loading = false;
          this.loadReviews();
        },
        error: err => {
          this.error = 'Không thể tải thông tin sản phẩm.';
          this.loading = false;
          console.error(err);
        }
      });
  }

  // ─── Variant ──────────────────────────────────────────────
  selectVariant(variant: VariantResponse): void {
    this.selectedVariant = variant;
    if (variant.imageUrl) {
      this.selectedMainImage = variant.imageUrl;
    }
    this.quantity = 1;
  }

  isVariantOutOfStock(variant: VariantResponse): boolean {
    return variant.stock === 0 || variant.status !== 'ACTIVE';
  }

  // ─── Images ───────────────────────────────────────────────
  selectMainImage(url: string): void {
    this.selectedMainImage = url;
  }

  openModal(url: string): void {
    this.modalImageSrc = url;
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.modalImageSrc = '';
  }

  // ─── Quantity ─────────────────────────────────────────────
  get maxStock(): number {
    return this.selectedVariant?.stock ?? 1;
  }

  decreaseQty(): void {
    if (this.quantity > 1) this.quantity--;
  }

  increaseQty(): void {
    if (this.quantity < this.maxStock) this.quantity++;
  }

  // ─── Stock Display ────────────────────────────────────────
  get stockClass(): string {
    const stock = this.selectedVariant?.stock ?? 0;
    if (stock === 0) return 'out-stock';
    if (stock < 5) return 'low-stock';
    return '';
  }

  get stockLabel(): string {
    const stock = this.selectedVariant?.stock ?? 0;
    if (stock === 0) return 'Hết hàng';
    if (stock < 5) return `Chỉ còn ${stock} sản phẩm`;
    return `Còn ${stock} sản phẩm`;
  }

  // ─── Price ───────────────────────────────────────────────
  get currentPrice(): number {
    return this.selectedVariant?.price ?? this.product?.basePrice ?? 0;
  }

  get oldPrice(): number {
    return Math.round(this.currentPrice * 1.18);
  }

  formatPrice(val: number): string {
    return new Intl.NumberFormat('vi-VN').format(val) + 'đ';
  }

  // ─── Tabs ────────────────────────────────────────────────
  switchTab(tab: 'specs' | 'description' | 'warranty'): void {
    this.activeTab = tab;
  }

  // ─── Specs helper ────────────────────────────────────────
  getSpec(key: string): string {
    return this.product?.specs?.find(s => s.specKey === key)?.specValue ?? '—';
  }

  // ─── Cart ────────────────────────────────────────────────
  addToCart(): void {
    if (!this.selectedVariant || this.selectedVariant.stock === 0) return;

    this.cartLoading = true;
    this.cartMessage = '';

    this.cartService.addItem({
      variantId: this.selectedVariant.id,
      quantity: this.quantity
    }).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.cartMessage = 'Đã thêm vào giỏ hàng!';
          this.cartLoading = false;
          setTimeout(() => this.cartMessage = '', 3000);
        },
        error: err => {
          this.cartMessage = 'Thêm vào giỏ thất bại.';
          this.cartLoading = false;
          console.error(err);
        }
      });
  }

  /**
   * Mua ngay — không qua giỏ hàng, navigate thẳng đến /checkout
   * với item hiện tại được truyền qua router state.
   */
  buyNow(): void {
    if (!this.selectedVariant || !this.product) return;

    const item: OrderItem = {
      productId:   this.product.id,
      variantId:   this.selectedVariant.id,
      productName: this.product.name,
      variantName: `${this.selectedVariant.name} — ${this.selectedVariant.color}`,
      imageUrl:    this.selectedVariant.imageUrl || this.selectedMainImage,
      quantity:    this.quantity,
      price:       this.currentPrice,
      weight:      this.selectedVariant.weight ?? 300
    };

    this.router.navigate(['/checkout'], {
      state: { items: [item] }
    });
  }

  // ─── Reviews ─────────────────────────────────────────────
  loadReviews(reset = true): void {
    if (!this.product) return;

    this.reviewLoading = true;
    if (reset) {
      this.reviewPage = 0;
      this.reviews = [];
    }

    this.reviewService.getReviews(
      this.product.id,
      this.reviewFilter,
      this.reviewPage,
      this.reviewSize
    ).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          const content = res.result?.content ?? [];
          this.reviews = reset ? content : [...this.reviews, ...content];
          this.reviewTotal = res.result?.totalElements ?? 0;
          this.reviewLoading = false;
        },
        error: err => {
          console.error(err);
          this.reviewLoading = false;
        }
      });
  }

  filterReviews(rating: number | undefined): void {
    this.reviewFilter = rating;
    this.loadReviews(true);
  }

  loadMoreReviews(): void {
    this.reviewPage++;
    this.loadReviews(false);
  }

  get hasMoreReviews(): boolean {
    return this.reviews.length < this.reviewTotal;
  }

  get averageRating(): number {
    if (!this.reviews.length) return 0;
    const sum = this.reviews.reduce((acc, r) => acc + r.rating, 0);
    return Math.round((sum / this.reviews.length) * 10) / 10;
  }

  getStarArray(rating: number): boolean[] {
    return Array.from({ length: 5 }, (_, i) => i < rating);
  }

  trackByReview(_: number, r: ReviewResponse): number {
    return r.id;
  }

  trackByVariant(_: number, v: VariantResponse): number {
    return v.id;
  }

  round(value: number): number {
    return Math.round(value);
  }

  handleImageError(event: any) {
    event.target.src = 'images/no-product-image.jpg';
  }
}