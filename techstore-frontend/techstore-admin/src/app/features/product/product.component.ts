import {
  Component,
  OnInit,
  OnDestroy
} from '@angular/core';

import { Subject } from 'rxjs';
import {
  takeUntil,
  debounceTime,
  distinctUntilChanged
} from 'rxjs/operators';

import { ProductService }  from './product.service';
import { BrandService }    from './brand.service';
import { CategoryService } from './category.service';
import { VariantService }  from './variant.service';

import { ProductListResponse, ProductResponse } from './models/product.model';
import { BrandResponse }    from './models/brand.model';
import { CategoryResponse } from './models/category.model';
import { VariantResponse }  from './models/variant.model';

import {
  ProductCreateRequest,
  ProductUpdateRequest,
} from './models/product-request.model';

import {
  VariantCreateRequest,
  VariantUpdateRequest
} from './models/variant-request.model';

import { DecimalPipe, NgClass, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';

// ─── Extended interfaces for UI state ──────────────────────────────────────
interface ProductListUI extends ProductListResponse {
  _checked?:        boolean;
  _variantOpen?:    boolean;
  _variantLoading?: boolean;
  _variants?:       VariantResponse[];
  _variantCount?:   number;
  _perfPct?:        number;
  _perfColor?:      string;
}

// ─── Toast ──────────────────────────────────────────────────────────────────
interface ToastState {
  show:    boolean;
  message: string;
  type:    'success' | 'error';
}

@Component({
  selector:    'app-product',
  templateUrl: './product.component.html',
  styleUrls:   ['./product.component.css'],
  standalone:  true,
  imports:     [DecimalPipe, FormsModule, NgIf, NgFor, NgClass],
})
export class ProductManagementComponent implements OnInit, OnDestroy {

  private destroy$     = new Subject<void>();
  private searchSubject = new Subject<string>();
  private toastTimer?: ReturnType<typeof setTimeout>;

  // ─── List state ────────────────────────────────────────────────────────
  products:      ProductListUI[] = [];
  loading        = false;
  viewMode: 'list' | 'grid' = 'list';

  // ─── Filter / sort ─────────────────────────────────────────────────────
  searchKeyword  = '';
  filterStatus   = '';
  sortBy         = 'id';
  sortDirection  = 'DESC';

  // ─── Pagination ────────────────────────────────────────────────────────
  currentPage    = 0;
  pageSize       = 10;
  totalPages     = 0;
  totalElements  = 0;
  pageNumbers:   number[] = [];

  // ─── Stat counts ────────────────────────────────────────────────────────
  totalCount        = 0;
  activeCount       = 0;
  inactiveCount     = 0;
  discontinuedCount = 0;
  activeStatFilter  = 'all';

  // ─── Lookup data ────────────────────────────────────────────────────────
  brands:     BrandResponse[]    = [];
  categories: CategoryResponse[] = [];

  // ─── Status options ─────────────────────────────────────────────────────
  statusOptions = [
    { value: 'ACTIVE',       label: 'Đang bán'         },
    { value: 'INACTIVE',     label: 'Ngừng bán'        },
    { value: 'DISCONTINUED', label: 'Ngừng kinh doanh' }
  ];

  specPresets = [
    'CPU', 'RAM', 'Ổ cứng', 'Màn hình', 'Pin', 'Kết nối',
    'Hệ điều hành', 'Trọng lượng', 'Bảo hành', 'GPU', 'Cổng giao tiếp'
  ];

  allChecked = false;

  // ─── Toast ──────────────────────────────────────────────────────────────
  toast: ToastState = { show: false, message: '', type: 'success' };

  // ─── Product modal ──────────────────────────────────────────────────────
  showProductModal   = false;
  isEditMode         = false;
  saving             = false;
  modalTab: 'basic' | 'specs' | 'images' = 'basic';
  editingProductId: number | null = null;
  currentProductImages: any[] = [];

  productForm: ProductCreateRequest = this.emptyProductForm();

  // ─── Detail modal ───────────────────────────────────────────────────────
  showDetailModal  = false;
  selectedProduct: ProductResponse | null = null;
  detailMainImage: string | null = null;

  // ─── Variant modal ──────────────────────────────────────────────────────
  showVariantModal    = false;
  isEditVariantMode   = false;
  variantParentProduct: ProductListUI | null = null;
  editingVariantId: number | null = null;

  variantForm: VariantCreateRequest = this.emptyVariantForm();

  // ─── Variant image modal ─────────────────────────────────────────────────
  showVariantImageModal    = false;
  selectedVariantForImage: VariantResponse | null = null;
  variantImageParent: ProductListUI | null = null;
  selectedVariantImageFile: File | null = null;
  variantImagePreview: string | null = null;

  // ─── Status modal ────────────────────────────────────────────────────────
  showStatusModal   = false;
  productForStatus: ProductListUI | null = null;
  newStatus         = '';

  // ─── Delete modal ────────────────────────────────────────────────────────
  showDeleteModal  = false;
  productToDelete: ProductListUI | null = null;
  variantToDelete: VariantResponse | null = null;
  deleteParent:    ProductListUI | null = null;

  constructor(
    private productService:  ProductService,
    private brandService:    BrandService,
    private categoryService: CategoryService,
    private variantService:  VariantService
  ) {}

  // ══════════════════════════════════════════════════════════════════════════
  // LIFECYCLE
  // ══════════════════════════════════════════════════════════════════════════

  ngOnInit(): void {
    this.initSearchDebounce();
    this.loadLookupData();
    this.loadProducts();
    this.loadStatCounts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.toastTimer) clearTimeout(this.toastTimer);
  }

  // ══════════════════════════════════════════════════════════════════════════
  // DATA LOADING
  // ══════════════════════════════════════════════════════════════════════════

  private initSearchDebounce(): void {
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.currentPage = 0;
      this.loadProducts();
    });
  }

  loadProducts(): void {
    this.loading = true;
    const hasSearch = this.searchKeyword.trim() || this.filterStatus;

    const obs = hasSearch
      ? this.productService.search({
          keyword:       this.searchKeyword.trim() || undefined,
          page:          this.currentPage,
          size:          this.pageSize,
          sortBy:        this.sortBy,
          sortDirection: this.sortDirection
        })
      : this.productService.getAll(
          this.currentPage,
          this.pageSize,
          this.sortBy,
          this.sortDirection
        );

    obs.pipe(takeUntil(this.destroy$)).subscribe({
      next: res => {
        const page     = res.result;
        this.products  = (page?.content ?? []).map(p => this.enrichProduct(p));
        console.log(this.products)
        this.totalElements = page?.totalElements ?? 0;
        this.totalPages    = page?.totalPages    ?? 0;
        this.buildPageNumbers();
        this.loading   = false;
      },
      error: () => {
        this.showToast('Không thể tải danh sách sản phẩm', 'error');
        this.loading = false;
      }
    });
  }

  private enrichProduct(p: ProductListResponse): ProductListUI {
    return {
      ...p,
      _checked:         false,
      _variantOpen:     false,
      _variantLoading:  false,
      _variants:        undefined,
      _variantCount:    undefined,
      _perfPct:         0,
      _perfColor:       '#5a67f2'
    };
  }

  loadStatCounts(): void {
    this.productService.getAll(0, 1)
      .pipe(takeUntil(this.destroy$))
      .subscribe(r => this.totalCount = r.result?.totalElements ?? 0);

    this.productService.search({ page: 0, size: 1 })
      .pipe(takeUntil(this.destroy$))
      .subscribe(r => this.activeCount = r.result?.totalElements ?? 0);
  }

  loadLookupData(): void {
    this.brandService.getAllBrands(0, 100)
      .pipe(takeUntil(this.destroy$))
      .subscribe(r => this.brands = r.result?.content ?? []);

    this.categoryService.getAll({ page: 0, size: 100 })
      .pipe(takeUntil(this.destroy$))
      .subscribe(r => this.categories = r.result?.content ?? []);
  }

  // ══════════════════════════════════════════════════════════════════════════
  // FILTERS & SEARCH
  // ══════════════════════════════════════════════════════════════════════════

  onSearchChange(): void {
    this.searchSubject.next(this.searchKeyword);
  }

  clearSearch(): void {
    this.searchKeyword = '';
    this.currentPage   = 0;
    this.loadProducts();
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadProducts();
  }

  onSortChange(): void {
    this.currentPage = 0;
    this.loadProducts();
  }

  filterByStat(status: string): void {
    this.activeStatFilter = status;
    this.filterStatus     = status === 'all' ? '' : status;
    this.currentPage      = 0;
    this.loadProducts();
  }

  // ══════════════════════════════════════════════════════════════════════════
  // PAGINATION
  // ══════════════════════════════════════════════════════════════════════════

  goPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.currentPage) return;
    this.currentPage = page;
    this.loadProducts();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  private buildPageNumbers(): void {
    const pages: number[] = [];
    const total = this.totalPages;
    const cur   = this.currentPage;

    if (total <= 7) {
      for (let i = 0; i < total; i++) pages.push(i);
    } else {
      pages.push(0);
      if (cur > 3)  pages.push(-1);
      for (let i = Math.max(1, cur - 1); i <= Math.min(total - 2, cur + 1); i++) pages.push(i);
      if (cur < total - 4) pages.push(-1);
      pages.push(total - 1);
    }
    this.pageNumbers = pages;
  }

  // ══════════════════════════════════════════════════════════════════════════
  // CHECKBOX
  // ══════════════════════════════════════════════════════════════════════════

  toggleAll(): void {
    this.products.forEach(p => p._checked = this.allChecked);
  }

  // ══════════════════════════════════════════════════════════════════════════
  // VARIANTS
  // ══════════════════════════════════════════════════════════════════════════

  toggleVariants(product: ProductListUI): void {
    product._variantOpen = !product._variantOpen;

    if (product._variantOpen && !product._variants) {
      product._variantLoading = true;
      this.variantService.getByProduct(product.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: res => {
            product._variants      = res.result ?? [];
            product._variantCount  = product._variants.length;
            product._variantLoading = false;
          },
          error: () => {
            product._variantLoading = false;
            this.showToast('Không thể tải biến thể', 'error');
          }
        });
    } else if (product._variantOpen) {
      product._variantCount = product._variants?.length ?? 0;
    }
  }

  // ══════════════════════════════════════════════════════════════════════════
  // VIEW DETAIL
  // ══════════════════════════════════════════════════════════════════════════

  viewProduct(p: ProductListUI): void {
    this.productService.getById(p.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.selectedProduct = res.result ?? null;
          if (this.selectedProduct) {
            const primary = this.selectedProduct.images?.find(i => i.isPrimary);
            this.detailMainImage = primary?.url ?? this.selectedProduct.images?.[0]?.url ?? null;
          }
          this.showDetailModal = true;
        },
        error: () => this.showToast('Không thể tải chi tiết sản phẩm', 'error')
      });
  }

  // ══════════════════════════════════════════════════════════════════════════
  // CREATE PRODUCT
  // ══════════════════════════════════════════════════════════════════════════

  openCreateModal(): void {
    this.isEditMode       = false;
    this.editingProductId = null;
    this.productForm      = this.emptyProductForm();
    this.currentProductImages = [];
    this.modalTab         = 'basic';
    this.showProductModal = true;
  }

  // ══════════════════════════════════════════════════════════════════════════
  // EDIT PRODUCT
  // ══════════════════════════════════════════════════════════════════════════

  editProduct(p: ProductListUI | ProductResponse): void {
    this.isEditMode       = true;
    this.editingProductId = p.id;
    this.modalTab         = 'basic';
    this.showProductModal = true;
    this.showDetailModal  = false;

    this.productService.getById(p.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          const full = res.result;
          if (!full) return;
          this.productForm = {
            name:             full.name,
            description:      full.description,
            performanceScore: full.performanceScore,
            powerConsumption: full.powerConsumption,
            status:           full.status,
            brandId:          full.brand?.id,
            categoryId:       full.category?.id,
            specs:            full.specs?.map(s => ({ specKey: s.specKey, specValue: s.specValue })) ?? []
          };
          this.currentProductImages = full.images ?? [];
        },
        error: () => this.showToast('Không thể tải dữ liệu sản phẩm', 'error')
      });
  }

  closeProductModal(): void {
    this.showProductModal = false;
  }

  saveProduct(): void {
    if (!this.productForm.name?.trim()) {
      this.showToast('Vui lòng nhập tên sản phẩm', 'error');
      this.modalTab = 'basic';
      return;
    }
    if (!this.productForm.categoryId) {
      this.showToast('Vui lòng chọn danh mục', 'error');
      this.modalTab = 'basic';
      return;
    }
    if (!this.productForm.brandId) {
      this.showToast('Vui lòng chọn thương hiệu', 'error');
      this.modalTab = 'basic';
      return;
    }

    this.saving = true;

    const obs = this.isEditMode && this.editingProductId
      ? this.productService.updateProduct(this.editingProductId, this.productForm as ProductUpdateRequest)
      : this.productService.createProduct(this.productForm);

    obs.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.saving = false;
        this.showProductModal = false;
        this.showToast(
          this.isEditMode ? 'Cập nhật sản phẩm thành công!' : 'Thêm sản phẩm thành công!',
          'success'
        );
        this.loadProducts();
        this.loadStatCounts();
      },
      error: err => {
        this.saving = false;
        this.showToast(err?.error?.message ?? 'Đã xảy ra lỗi, vui lòng thử lại', 'error');
      }
    });
  }

  // ══════════════════════════════════════════════════════════════════════════
  // STATUS
  // ══════════════════════════════════════════════════════════════════════════

  openStatusModal(p: ProductListUI): void {
    this.productForStatus = p;
    this.newStatus        = p.status;
    this.showStatusModal  = true;
  }

  saveStatus(): void {
    if (!this.productForStatus) return;
    if (this.newStatus === this.productForStatus.status) {
      this.showStatusModal = false;
      return;
    }
    this.saving = true;

    this.productService.updateStatus(this.productForStatus.id, this.newStatus)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.saving = false;
          this.showStatusModal = false;
          this.productForStatus!.status = this.newStatus;
          this.showToast('Cập nhật trạng thái thành công!', 'success');
          this.loadStatCounts();
        },
        error: err => {
          this.saving = false;
          this.showToast(err?.error?.message ?? 'Lỗi cập nhật trạng thái', 'error');
        }
      });
  }

  // ══════════════════════════════════════════════════════════════════════════
  // DELETE PRODUCT
  // ══════════════════════════════════════════════════════════════════════════

  confirmDelete(p: ProductListUI): void {
    this.productToDelete = p;
    this.variantToDelete = null;
    this.deleteParent    = null;
    this.showDeleteModal = true;
  }

  executeDelete(): void {
    if (this.variantToDelete && this.deleteParent) {
      this.deleteVariantConfirmed();
    } else if (this.productToDelete) {
      this.deleteProductConfirmed();
    }
  }

  private deleteProductConfirmed(): void {
    this.saving = true;
    this.productService.updateStatus(this.productToDelete!.id, 'DISABLE')
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.saving = false;
          this.showDeleteModal = false;
          const p = this.products.find(x => x.id === this.productToDelete!.id);
          if (p) p.status = 'DISABLE';
          this.showToast('Đã vô hiệu hóa sản phẩm!', 'success');
          this.loadProducts();
          this.loadStatCounts();
        },
        error: err => {
          this.saving = false;
          this.showToast(err?.error?.message ?? 'Không thể vô hiệu hóa sản phẩm', 'error');
        }
      });
  }

  // ══════════════════════════════════════════════════════════════════════════
  // IMAGES (Product)
  // ══════════════════════════════════════════════════════════════════════════

  onImagesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || !this.editingProductId) return;
    const files = Array.from(input.files);
    this.saving = true;

    this.productService.updateImages(this.editingProductId, files)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.saving = false;
          this.currentProductImages = res.result?.images ?? [];
          this.showToast('Cập nhật hình ảnh thành công!', 'success');
          // Reset input
          input.value = '';
        },
        error: () => {
          this.saving = false;
          this.showToast('Lỗi tải ảnh lên', 'error');
        }
      });
  }

  onDropImages(event: DragEvent): void {
    event.preventDefault();
    if (!event.dataTransfer?.files || !this.editingProductId) return;
    const files = Array.from(event.dataTransfer.files).filter(f => f.type.startsWith('image/'));
    if (!files.length) return;

    this.productService.updateImages(this.editingProductId, files)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.currentProductImages = res.result?.images ?? [];
          this.showToast('Tải ảnh thành công!', 'success');
        },
        error: () => this.showToast('Lỗi tải ảnh lên', 'error')
      });
  }

  setPrimaryImage(img: any): void {
    this.currentProductImages.forEach(i => i.isPrimary = false);
    img.isPrimary = true;
  }

  // ══════════════════════════════════════════════════════════════════════════
  // ADD VARIANT
  // ══════════════════════════════════════════════════════════════════════════

  openAddVariantModal(product: ProductListUI): void {
    this.variantParentProduct = product;
    this.isEditVariantMode    = false;
    this.editingVariantId     = null;
    this.variantForm          = this.emptyVariantForm();
    this.showVariantModal     = true;
  }

  editVariant(product: ProductListUI, variant: VariantResponse): void {
    this.variantParentProduct = product;
    this.isEditVariantMode    = true;
    this.editingVariantId     = variant.id;
    this.variantForm = {
      color:  variant.color,
      price:  variant.price,
      status: variant.status,
      weight: variant.weight
    };
    this.showVariantModal = true;
  }

  closeVariantModal(): void {
    this.showVariantModal = false;
  }

  saveVariant(): void {
    if (!this.variantForm.color?.trim()) {
      this.showToast('Vui lòng nhập màu sắc', 'error');
      return;
    }
    if (!this.variantForm.price || this.variantForm.price <= 0) {
      this.showToast('Vui lòng nhập giá hợp lệ', 'error');
      return;
    }

    this.saving = true;

    const obs = this.isEditVariantMode && this.editingVariantId
      ? this.variantService.updateVariant(this.editingVariantId, this.variantForm as VariantUpdateRequest)
      : this.variantService.createVariant(this.variantParentProduct!.id, this.variantForm);

    obs.pipe(takeUntil(this.destroy$)).subscribe({
      next: res => {
        this.saving = false;
        this.showVariantModal = false;

        const parent = this.variantParentProduct!;
        if (this.isEditVariantMode && parent._variants) {
          const idx = parent._variants.findIndex(v => v.id === this.editingVariantId);
          if (idx >= 0 && res.result) parent._variants[idx] = res.result;
        } else if (res.result && parent._variants) {
          parent._variants.push(res.result);
          parent._variantCount = parent._variants.length;
        } else {
          parent._variants    = undefined;
          parent._variantOpen = false;
        }

        this.showToast(
          this.isEditVariantMode ? 'Cập nhật biến thể thành công!' : 'Thêm biến thể thành công!',
          'success'
        );
      },
      error: err => {
        this.saving = false;
        this.showToast(err?.error?.message ?? 'Lỗi lưu biến thể', 'error');
      }
    });
  }

  // ══════════════════════════════════════════════════════════════════════════
  // VARIANT IMAGE
  // ══════════════════════════════════════════════════════════════════════════

  openVariantImageModal(product: ProductListUI, variant: VariantResponse): void {
    this.variantImageParent       = product;
    this.selectedVariantForImage  = variant;
    this.selectedVariantImageFile = null;
    this.variantImagePreview      = null;
    this.showVariantImageModal    = true;
  }

  onVariantImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.selectedVariantImageFile = input.files[0];

    const reader = new FileReader();
    reader.onload = e => this.variantImagePreview = e.target?.result as string;
    reader.readAsDataURL(this.selectedVariantImageFile);
  }

  saveVariantImage(): void {
    if (!this.selectedVariantForImage || !this.selectedVariantImageFile) return;
    this.saving = true;

    this.variantService.updateVariantImage(
      this.selectedVariantForImage.id,
      this.selectedVariantImageFile
    ).pipe(takeUntil(this.destroy$)).subscribe({
      next: res => {
        this.saving = false;
        this.showVariantImageModal = false;
        if (res.result && this.variantImageParent?._variants) {
          const idx = this.variantImageParent._variants.findIndex(v => v.id === this.selectedVariantForImage!.id);
          if (idx >= 0) this.variantImageParent._variants[idx] = res.result;
        }
        this.showToast('Cập nhật ảnh biến thể thành công!', 'success');
      },
      error: err => {
        this.saving = false;
        this.showToast(err?.error?.message ?? 'Lỗi cập nhật ảnh', 'error');
      }
    });
  }

  // ══════════════════════════════════════════════════════════════════════════
  // DELETE VARIANT
  // ══════════════════════════════════════════════════════════════════════════

  confirmDeleteVariant(product: ProductListUI, variant: VariantResponse): void {
    this.variantToDelete = variant;
    this.deleteParent    = product;
    this.productToDelete = null;
    this.showDeleteModal = true;
  }

  private deleteVariantConfirmed(): void {
    this.saving = true;
    this.variantService.deleteVariant(this.variantToDelete!.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.saving = false;
          this.showDeleteModal = false;
          if (this.deleteParent?._variants) {
            this.deleteParent._variants = this.deleteParent._variants.filter(v => v.id !== this.variantToDelete!.id);
            this.deleteParent._variantCount = this.deleteParent._variants.length;
          }
          this.showToast('Đã xóa biến thể!', 'success');
        },
        error: err => {
          this.saving = false;
          this.showToast(err?.error?.message ?? 'Không thể xóa biến thể', 'error');
        }
      });
  }

  // ══════════════════════════════════════════════════════════════════════════
  // SPECS HELPERS
  // ══════════════════════════════════════════════════════════════════════════

  addSpec(): void {
    this.productForm.specs.push({ specKey: '', specValue: '' });
  }

  removeSpec(index: number): void {
    this.productForm.specs.splice(index, 1);
  }

  addPresetSpec(key: string): void {
    const exists = this.productForm.specs.some(s => s.specKey === key);
    if (!exists) {
      this.productForm.specs.push({ specKey: key, specValue: '' });
    } else {
      this.showToast(`Thông số "${key}" đã tồn tại`, 'error');
    }
  }

  // ══════════════════════════════════════════════════════════════════════════
  // EXPORT
  // ══════════════════════════════════════════════════════════════════════════

  exportExcel(): void {
    this.showToast('Chức năng xuất Excel đang được phát triển', 'success');
  }

  // ══════════════════════════════════════════════════════════════════════════
  // HELPERS / UTILS
  // ══════════════════════════════════════════════════════════════════════════

  getStatusLabel(status: string): string {
    return this.statusOptions.find(s => s.value === status)?.label ?? status;
  }

  getColorHex(colorName: string): string {
    const map: Record<string, string> = {
      'đen': '#1a1a1a', 'den': '#1a1a1a', 'black': '#1a1a1a',
      'trắng': '#f0f0f0', 'trang': '#f0f0f0', 'white': '#f0f0f0',
      'xanh': '#3b82f6', 'blue': '#3b82f6', 'xanh dương': '#3b82f6',
      'đỏ': '#ef4444', 'do': '#ef4444', 'red': '#ef4444',
      'vàng': '#f59e0b', 'vang': '#f59e0b', 'yellow': '#f59e0b',
      'xám': '#9ca3af', 'xam': '#9ca3af', 'gray': '#9ca3af', 'grey': '#9ca3af',
      'bạc': '#c0c0c0', 'bac': '#c0c0c0', 'silver': '#c0c0c0',
      'vàng đồng': '#b5a642', 'gold': '#b5a642',
      'xanh lá': '#10b981', 'green': '#10b981', 'xanh la': '#10b981',
      'tím': '#8b5cf6', 'tim': '#8b5cf6', 'purple': '#8b5cf6',
      'hồng': '#ec4899', 'hong': '#ec4899', 'pink': '#ec4899',
      'cam': '#f97316', 'orange': '#f97316',
      'navy': '#1e3a5f', 'xanh navy': '#1e3a5f',
      'xanh rêu': '#4a7c59', 'olive': '#6b8e23',
    };
    const lower = colorName?.toLowerCase().trim() ?? '';
    return map[lower] ?? '#94a3b8';
  }

  private emptyProductForm(): ProductCreateRequest {
    return {
      name:             '',
      description:      '',
      performanceScore: 0,
      powerConsumption: 0,
      status:           'ACTIVE',
      brandId:          0,
      categoryId:       0,
      specs:            []
    };
  }

  private emptyVariantForm(): VariantCreateRequest {
    return {
      color:  '',
      price:  0,
      status: 'ACTIVE',
      weight: 0
    };
  }

  private showToast(message: string, type: 'success' | 'error'): void {
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toast = { show: true, message, type };
    this.toastTimer = setTimeout(() => this.toast.show = false, 3200);
  }
}