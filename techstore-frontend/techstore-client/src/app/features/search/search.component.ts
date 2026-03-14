import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { ActivatedRoute, RouterModule } from '@angular/router';

import { ProductService } from '../product/product.service';
import { BrandService } from '../product/brand.service';
import { CategoryService } from '../product/category.service';
import { ProductListResponse } from '../product/models/product.model';
import { ProductSearchRequest } from '../product/models/product-request.model';
import { BrandResponse } from '../product/models/brand.model';
import { CategoryResponse } from '../product/models/category.model';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit, OnDestroy {

  // ─── Data ─────────────────────────────────────────────────────
  products: ProductListResponse[] = [];
  brands: BrandResponse[] = [];
  categories: CategoryResponse[] = [];

  // ─── Filter state ─────────────────────────────────────────────
  keyword = '';
  selectedBrands: Set<string> = new Set();
  selectedPriceRange: string | null = null;
  selectedCategoryIds: Set<number> = new Set();

  // Custom price inputs (VND)
  customMinInput: number | null = null;
  customMaxInput: number | null = null;
  useCustomPrice = false;
  priceInputError = '';

  // ─── Sort ─────────────────────────────────────────────────────
  sortOptions = [
    { label: 'Phổ biến nhất',     sortBy: 'id',        dir: 'DESC' },
    { label: 'Giá: Thấp đến cao', sortBy: 'basePrice', dir: 'ASC'  },
    { label: 'Giá: Cao đến thấp', sortBy: 'basePrice', dir: 'DESC' },
    // { label: 'Mới nhất',          sortBy: 'createdAt', dir: 'DESC' },
    { label: 'Tên A-Z',           sortBy: 'name',      dir: 'ASC'  },
  ];
  selectedSort = this.sortOptions[0];

  // ─── Pagination ───────────────────────────────────────────────
  currentPage = 0;
  pageSize = 8;
  totalElements = 0;
  totalPages = 0;

  // ─── UI ───────────────────────────────────────────────────────
  isLoading = false;
  errorMessage = '';

  // ─── Preset price ranges ──────────────────────────────────────
  priceRanges = [
    { label: 'Dưới 5 triệu',  value: 'lt5',    min: 0,          max: 5_000_000  },
    { label: '5 - 10 triệu',  value: '5to10',  min: 5_000_000,  max: 10_000_000 },
    { label: '10 - 20 triệu', value: '10to20', min: 10_000_000, max: 20_000_000 },
    { label: 'Trên 20 triệu', value: 'gt20',   min: 20_000_000, max: undefined  },
  ];

  private destroy$     = new Subject<void>();
  private searchInput$ = new Subject<void>();
  private priceInput$  = new Subject<void>();

  constructor(
    private productService:  ProductService,
    private brandService:    BrandService,
    private categoryService: CategoryService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loadBrands();
    this.loadCategories();

    this.route.queryParams
  .pipe(takeUntil(this.destroy$))
  .subscribe(params => {
    const keyword = params['keyword'] || '';

    this.keyword = keyword;
    this.currentPage = 0;

    this.loadProducts();
  });

    this.searchInput$.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => { this.currentPage = 0; this.loadProducts(); });

    this.priceInput$.pipe(
      debounceTime(700),
      takeUntil(this.destroy$)
    ).subscribe(() => { this.applyCustomPrice(); });

    this.loadProducts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ─── Loaders ──────────────────────────────────────────────────
  loadBrands(): void {
    this.brandService.getBrandsByStatus('ACTIVE', 0, 50, 'name', 'ASC')
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: res => { if (res.result) this.brands = res.result.content ?? []; } });
  }

  loadCategories(): void {
    this.categoryService.getAll({ page: 0, size: 100, sortBy: 'name', sortDirection: 'ASC' })
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: res => { if (res.result) this.categories = res.result.content ?? []; } });
  }

  loadProducts(): void {
    this.isLoading = true;
    this.errorMessage = '';

    let minPrice: number | undefined;
    let maxPrice: number | undefined;

    if (this.useCustomPrice) {
      minPrice = this.customMinInput ?? undefined;
      maxPrice = this.customMaxInput ?? undefined;
    } else {
      const range = this.priceRanges.find(r => r.value === this.selectedPriceRange);
      minPrice = range?.min;
      maxPrice = range?.max;
    }

    const req: ProductSearchRequest = {
      keyword: this.keyword.trim() || undefined,
        
      brandNames: this.selectedBrands.size
        ? Array.from(this.selectedBrands)
        : undefined,
        
      categoryIds: this.selectedCategoryIds.size
        ? Array.from(this.selectedCategoryIds)
        : undefined,
        
      minPrice,
      maxPrice,
        
      page: this.currentPage,
      size: this.pageSize,
        
      sortBy: this.selectedSort.sortBy,
      sortDirection: this.selectedSort.dir,
    };

    this.productService.search(req)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          if (res.result) {
            this.products      = res.result.content       ?? [];
            this.totalElements = res.result.totalElements ?? 0;
            this.totalPages    = res.result.totalPages    ?? 0;
          }
          this.isLoading = false;
        },
        error: () => {
          this.errorMessage = 'Không thể tải sản phẩm. Vui lòng thử lại.';
          this.isLoading = false;
        }
      });
  }

  // ─── Filter handlers ──────────────────────────────────────────
  onKeywordChange(): void { this.searchInput$.next(); }

  onBrandToggle(brandName: string): void {
    this.selectedBrands.has(brandName)
      ? this.selectedBrands.delete(brandName)
      : this.selectedBrands.add(brandName);
    this.currentPage = 0;
    this.loadProducts();
  }

  onCategoryToggle(id: number): void {
    this.selectedCategoryIds.has(id)
      ? this.selectedCategoryIds.delete(id)
      : this.selectedCategoryIds.add(id);
    this.currentPage = 0;
    this.loadProducts();
  }

  onPriceRangeToggle(rangeValue: string): void {
    this.selectedPriceRange = this.selectedPriceRange === rangeValue ? null : rangeValue;
    if (this.selectedPriceRange) {
      this.useCustomPrice  = false;
      this.customMinInput  = null;
      this.customMaxInput  = null;
      this.priceInputError = '';
    }
    this.currentPage = 0;
    this.loadProducts();
  }

  onCustomPriceChange(): void { this.priceInput$.next(); }

  private applyCustomPrice(): void {
    this.priceInputError = '';
    const min = this.customMinInput;
    const max = this.customMaxInput;

    if (min !== null && min < 0) { this.priceInputError = 'Giá tối thiểu không được âm'; return; }
    if (max !== null && max < 0) { this.priceInputError = 'Giá tối đa không được âm'; return; }
    if (min !== null && max !== null && min > max) {
      this.priceInputError = 'Giá tối thiểu phải nhỏ hơn tối đa'; return;
    }

    const hasValue = min !== null || max !== null;
    this.useCustomPrice = hasValue;
    if (hasValue) this.selectedPriceRange = null;
    this.currentPage = 0;
    this.loadProducts();
  }

  onSortChange(option: typeof this.sortOptions[0]): void {
    this.selectedSort = option;
    this.loadProducts();
  }

  clearAllFilters(): void {
    this.keyword = '';
    this.selectedBrands.clear();
    this.selectedCategoryIds.clear();
    this.selectedPriceRange  = null;
    this.useCustomPrice      = false;
    this.customMinInput      = null;
    this.customMaxInput      = null;
    this.priceInputError     = '';
    this.selectedSort        = this.sortOptions[0];
    this.currentPage         = 0;
    this.loadProducts();
  }

  removeFilter(type: 'brand' | 'price' | 'customPrice' | 'category', value?: string | number): void {
    if (type === 'brand'    && typeof value === 'string') this.selectedBrands.delete(value);
    if (type === 'category' && typeof value === 'number') this.selectedCategoryIds.delete(value);
    if (type === 'price') this.selectedPriceRange = null;
    if (type === 'customPrice') {
      this.useCustomPrice  = false;
      this.customMinInput  = null;
      this.customMaxInput  = null;
      this.priceInputError = '';
    }
    this.currentPage = 0;
    this.loadProducts();
  }

  // ─── Pagination ───────────────────────────────────────────────
  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.loadProducts();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  // ─── Helpers ──────────────────────────────────────────────────
  get activeFilterCount(): number {
    return this.selectedBrands.size
      + this.selectedCategoryIds.size
      + (this.selectedPriceRange ? 1 : 0)
      + (this.useCustomPrice ? 1 : 0);
  }

  getPriceRangeLabel(value: string): string {
    return this.priceRanges.find(r => r.value === value)?.label ?? '';
  }

  getCategoryName(id: number): string {
    return this.categories.find(c => c.id === id)?.name ?? `#${id}`;
  }

  get customPriceLabel(): string {
    const min = this.customMinInput != null ? this.formatPrice(this.customMinInput) : '0đ';
    const max = this.customMaxInput != null ? this.formatPrice(this.customMaxInput) : '∞';
    return `${min} – ${max}`;
  }

  formatPrice(price?: number): string {
  return (price ?? 0).toLocaleString('vi-VN') + 'đ';
}

  trackByProductId(_: number, p: ProductListResponse): number { return p.id; }
  trackByCategoryId(_: number, c: CategoryResponse): number   { return c.id; }

  handleImageError(event: any) {
    event.target.src = 'images/no-product-image.jpg';
  }
}