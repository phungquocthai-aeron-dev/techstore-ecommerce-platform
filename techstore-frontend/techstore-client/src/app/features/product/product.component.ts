import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../product/product.service';
import { CategoryService } from './category.service';
import { BrandService } from './brand.service';
import { ProductListResponse } from '../product/models/product.model';
import { CategoryResponse } from './models/category.model';
import { BrandResponse } from './models/brand.model'; 
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.css']
})
export class ProductsComponent implements OnInit {

  // ── State ──
  products: ProductListResponse[] = [];
  categories: CategoryResponse[] = [];
  brands: BrandResponse[] = [];
  loading = false;

  // ── Filters ──
  selectedCategoryType = '';
  selectedCategoryId: number | null = null;
  selectedBrandIds: number[] = [];
  minPrice: number | null = null;
  maxPrice: number | null = null;
  sortBy = 'id';
  sortDirection = 'DESC';
  keyword = '';

  // ── Pagination ──
  page = 0;
  size = 12;
  totalPages = 0;
  totalElements = 0;

  readonly TYPE_META: Record<string, { label: string; icon: string }> = {
    LAPTOP:       { label: 'Laptop',       icon: 'bi-laptop' },
    SMARTPHONE:   { label: 'Điện thoại',   icon: 'bi-phone' },
    PC_COMPONENT: { label: 'Linh kiện PC', icon: 'bi-cpu' },
    ACCESSORY:    { label: 'Phụ kiện',     icon: 'bi-headphones' },
  };

  readonly categoryTypes = Object.entries(this.TYPE_META).map(([type, meta]) => ({
    type, ...meta
  }));

  readonly sortOptions = [
    { value: 'id|DESC',       label: 'Mới nhất' },
    { value: 'basePrice|ASC', label: 'Giá tăng dần' },
    { value: 'basePrice|DESC',label: 'Giá giảm dần' },
    { value: 'name|ASC',      label: 'Tên A-Z' },
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private categoryService: CategoryService,
    private brandService: BrandService,
  ) {}

  ngOnInit(): void {
    this.loadBrands();
    this.route.queryParams.subscribe(params => {
      this.selectedCategoryType = params['categoryType'] || '';
      this.selectedCategoryId   = params['categoryId'] ? +params['categoryId'] : null;
      this.keyword              = params['keyword'] || '';
      this.page = 0;
      this.loadCategories();
      this.loadProducts();
    });
  }

  loadBrands(): void {
    this.brandService.getAllBrands(0, 100).subscribe({
      next: res => this.brands = res.result?.content || []
    });
  }

  loadCategories(): void {
    if (this.selectedCategoryType) {
      this.categoryService.getByType(this.selectedCategoryType, { size: 50 }).subscribe({
        next: res => this.categories = res.result?.content || []
      });
    } else {
      this.categoryService.getAll({ size: 100 }).subscribe({
        next: res => this.categories = res.result?.content || []
      });
    }
  }

  loadProducts(): void {
    this.loading = true;
    const [sortBy, sortDirection] = this.sortBy.includes('|')
      ? this.sortBy.split('|')
      : [this.sortBy, this.sortDirection];

    if (this.keyword) {
      this.productService.search({
        keyword: this.keyword,
        categoryIds: this.selectedCategoryId ? [this.selectedCategoryId] : undefined,
        page: this.page, size: this.size, sortBy, sortDirection
      }).subscribe({ next: res => this.handleResult(res), error: () => this.loading = false });
      return;
    }

    if (this.selectedCategoryId) {
      this.productService.getByCategoryId(
        this.selectedCategoryId, this.page, this.size, sortBy, sortDirection,
        this.selectedBrandIds.length ? this.selectedBrandIds : undefined,
        this.minPrice ?? undefined, this.maxPrice ?? undefined
      ).subscribe({ next: res => this.handleResult(res), error: () => this.loading = false });
      return;
    }

    if (this.selectedCategoryType) {
      this.productService.getByCategoryType(
        this.selectedCategoryType, this.page, this.size, sortBy, sortDirection
      ).subscribe({ next: res => this.handleResult(res), error: () => this.loading = false });
      return;
    }

    this.productService.getAll(this.page, this.size, sortBy, sortDirection)
      .subscribe({ next: res => this.handleResult(res), error: () => this.loading = false });
  }

  private handleResult(res: any): void {
    const data = res?.result;
    this.products      = data?.content || [];
    this.totalPages    = data?.totalPages || 0;
    this.totalElements = data?.totalElements || 0;
    this.loading = false;
  }

  // ── Filter actions ──
  selectType(type: string): void {
    this.selectedCategoryType = this.selectedCategoryType === type ? '' : type;
    this.selectedCategoryId = null;
    this.page = 0;
    this.loadCategories();
    this.loadProducts();
  }

  selectCategory(id: number): void {
    this.selectedCategoryId = this.selectedCategoryId === id ? null : id;
    this.page = 0;
    this.loadProducts();
  }

  toggleBrand(id: number): void {
    const idx = this.selectedBrandIds.indexOf(id);
    if (idx > -1) this.selectedBrandIds.splice(idx, 1);
    else this.selectedBrandIds.push(id);
    this.page = 0;
    this.loadProducts();
  }

  applyPrice(): void {
    this.page = 0;
    this.loadProducts();
  }

  onSortChange(): void {
    this.page = 0;
    this.loadProducts();
  }

  resetFilters(): void {
    this.selectedCategoryType = '';
    this.selectedCategoryId = null;
    this.selectedBrandIds = [];
    this.minPrice = null;
    this.maxPrice = null;
    this.keyword = '';
    this.sortBy = 'id|DESC';
    this.page = 0;
    this.loadCategories();
    this.loadProducts();
  }

  goPage(p: number): void {
    if (p < 0 || p >= this.totalPages) return;
    this.page = p;
    this.loadProducts();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  get pageNumbers(): number[] {
    const total = this.totalPages;
    const cur = this.page;
    const delta = 2;
    const range: number[] = [];
    for (let i = Math.max(0, cur - delta); i <= Math.min(total - 1, cur + delta); i++) {
      range.push(i);
    }
    return range;
  }

  get filteredCategories(): CategoryResponse[] {
    if (!this.selectedCategoryType) return this.categories;
    return this.categories.filter(c => c.categoryType === this.selectedCategoryType);
  }

  get activeFilterCount(): number {
    let count = 0;
    if (this.selectedCategoryType) count++;
    if (this.selectedCategoryId) count++;
    if (this.selectedBrandIds.length) count++;
    if (this.minPrice || this.maxPrice) count++;
    return count;
  }

  getImageUrl(image: string | null | undefined): string {
    if (!image) return 'images/no-product-image.jpg';
    if (image.startsWith('http')) return image;
    return environment.imageProductUrl + image;
  }

  handleImageError(event: any): void {
    event.target.src = 'images/no-product-image.jpg';
  }

  isBrandSelected(id: number): boolean {
    return this.selectedBrandIds.includes(id);
  }

  getTypeMeta(type: string) {
    return this.TYPE_META[type] || { label: type, icon: 'bi-box' };
  }
}