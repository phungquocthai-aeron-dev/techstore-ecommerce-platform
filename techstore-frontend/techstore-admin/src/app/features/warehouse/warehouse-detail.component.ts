import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { WarehouseService } from './warehouse.service';
import { InventoryService } from './inventory.service';
import { WarehouseTransactionService } from './warehouse-transaction.service';
import { SupplierService } from '../supplier/supplier.service';
import { VariantService } from '../product/variant.service';

import { WarehouseResponse } from './models/warehouse.model';
import { InventoryResponse } from './models/inventory.model';
import { InventoryUpdateRequest } from './models/inventory-request.model';
import { WarehouseTransactionResponse } from './models/warehouse-transaction.model';
import { WarehouseUpdateRequest } from './models/warehouse-request.model';
import {
  WarehouseTransactionCreateRequest,
  TransactionDetailRequest,
} from './models/warehouse-transaction-request.model';
import { SupplierResponse } from '../supplier/models/supplier.model';
import { VariantResponse } from '../product/models/variant.model';
import { PageResponse } from '../product/models/page-response.model';
import { StockOfPipe } from './stockof.pipe';

// ── Dòng nhập kho ────────────────────────────────────────────────────
interface InboundLine {
  variantId: number | null;
  quantity: number;
  cost: number;           // giá nhập
  batchCode: string;
  variantInfo: VariantResponse | null;
  searching: boolean;
  searchTerm: string;
  showDropdown: boolean;
  dropdownResults: VariantResponse[];

  // Giá variant sau khi nhập kho (weighted average)
  proposedPrice: number;      // giá TB được tính tự động
  finalPrice: number;         // giá người dùng có thể override
  priceEdited: boolean;       // đã tự sửa hay chưa
  currentStock: number;       // tồn kho hiện tại để tính TB
  currentPrice: number;       // giá hiện tại để tính TB
}

// ── Dòng xuất kho ────────────────────────────────────────────────────
interface OutboundLine {
  variantId: number | null;
  quantity: number;
  variantInfo: VariantResponse | null;
  searching: boolean;
  searchTerm: string;
  maxStock: number;
  showDropdown: boolean;
  dropdownResults: VariantResponse[];
}

@Component({
  selector: 'app-warehouse-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, StockOfPipe],
  templateUrl: './warehouse-detail.component.html',
  styleUrls: ['./warehouse-detail.component.css'],
})
export class WarehouseDetailComponent implements OnInit {

  warehouseId!: number;
  warehouse: WarehouseResponse | null = null;

  isLoading = false;
  isLoadingInv = false;
  isLoadingTx = false;
  isSaving = false;

  alertMsg = '';
  alertType: 'success' | 'error' = 'success';

  activeTab: 'inventory' | 'transactions' = 'inventory';

  // ─── Inventory ────────────────────────────────────────────────────
  inventories: InventoryResponse[] = [];
  invSearch = '';
  invStockFilter: '' | 'in_stock' | 'out_of_stock' = '';

  // ─── Transactions ─────────────────────────────────────────────────
  transactions: WarehouseTransactionResponse[] = [];
  txTypeFilter = '';
  txStatusFilter = '';

  // ─── All variants (for search dropdown) ───────────────────────────
  allVariants: VariantResponse[] = [];
  isLoadingVariants = false;
  variantSearchDebounce: any = null;

  // ─── Modal: sửa kho ───────────────────────────────────────────────
  showEditModal = false;
  editForm: Partial<WarehouseUpdateRequest> & { status?: string } = {};

  // ─── Modal: sửa lô hàng ───────────────────────────────────────────
  showUpdateInvModal = false;
  editingInv: InventoryResponse | null = null;
  invForm: { stock: number; batchCode?: string; status?: string } = { stock: 0 };

  // ─── Modal: NHẬP KHO ──────────────────────────────────────────────
  showInboundModal = false;
  suppliers: SupplierResponse[] = [];
  inboundForm = {
    supplierId: null as number | null,
    note: '',
    staffId: 1,
  };
  inboundLines: InboundLine[] = [];

  // ─── Modal: XUẤT KHO ──────────────────────────────────────────────
  showOutboundModal = false;
  outboundForm = {
    note: '',
    staffId: 1,
    referenceType: 'MANUAL',
    orderId: null as number | null,
  };
  outboundLines: OutboundLine[] = [];

  // ─── Computed ─────────────────────────────────────────────────────

  get activeInventoryCount(): number {
    return this.inventories.filter(i => i.status === 'ACTIVE').length;
  }

  get totalStock(): number {
    return this.inventories.reduce((s, i) => s + i.stock, 0);
  }

  get filteredInventories(): InventoryResponse[] {
    let list = this.inventories;

    // Filter by stock status
    if (this.invStockFilter === 'in_stock') {
      list = list.filter(i => i.stock > 0 && i.status === 'ACTIVE');
    } else if (this.invStockFilter === 'out_of_stock') {
      list = list.filter(i => i.stock === 0 || i.status !== 'ACTIVE');
    }

    // Filter by search text
    if (this.invSearch.trim()) {
      const q = this.invSearch.toLowerCase();
      list = list.filter(i =>
        i.batchCode?.toLowerCase().includes(q) ||
        i.status?.toLowerCase().includes(q) ||
        i.variantInfo?.color?.toLowerCase().includes(q) ||
        String(i.variantId).includes(q)
      );
    }

    return list;
  }

  get filteredTransactions(): WarehouseTransactionResponse[] {
    return this.transactions.filter(tx => {
      const matchType = !this.txTypeFilter || tx.transactionType === this.txTypeFilter;
      const matchStatus = !this.txStatusFilter || tx.status === this.txStatusFilter;
      return matchType && matchStatus;
    });
  }

  get inboundTotal(): number {
    return this.inboundLines.reduce((s, l) => s + l.quantity * l.cost, 0);
  }

  get outboundTotal(): number {
    return this.outboundLines.reduce((s, l) => s + l.quantity, 0);
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private warehouseService: WarehouseService,
    private inventoryService: InventoryService,
    private txService: WarehouseTransactionService,
    private supplierService: SupplierService,
    private variantService: VariantService,
  ) { }

  ngOnInit(): void {
    this.warehouseId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadWarehouse();
    this.loadInventories();
    this.loadTransactions();
    this.loadSuppliers();
    this.loadAllVariants();
  }

  // ─── Load ─────────────────────────────────────────────────────────

  loadWarehouse(): void {
    this.isLoading = true;
    this.warehouseService.getById(this.warehouseId).subscribe({
      next: res => { this.warehouse = res.result ?? null; this.isLoading = false; },
      error: () => { this.showAlert('Không thể tải thông tin kho.', 'error'); this.isLoading = false; },
    });
  }

  loadInventories(): void {
    this.isLoadingInv = true;
    this.inventoryService.getByWarehouse(this.warehouseId).subscribe({
      next: res => { this.inventories = res.result ?? []; this.isLoadingInv = false; },
      error: () => { this.isLoadingInv = false; },
    });
  }

  loadTransactions(): void {
    this.isLoadingTx = true;
    this.txService.getByWarehouse(this.warehouseId).subscribe({
      next: res => { this.transactions = res.result ?? []; this.isLoadingTx = false; },
      error: () => { this.isLoadingTx = false; },
    });
  }

  loadSuppliers(): void {
    this.supplierService.getAll().subscribe({
      next: res => { this.suppliers = res.result ?? []; },
      error: () => { },
    });
  }

  loadAllVariants(): void {
    this.isLoadingVariants = true;
    // Tải trang đầu với size lớn để có danh sách cơ bản
    this.variantService.getAll(0, 200, 'id', 'DESC').subscribe({
      next: res => {
        this.allVariants = res.result?.content ?? [];
        this.isLoadingVariants = false;
      },
      error: () => { this.isLoadingVariants = false; },
    });
  }

  // ─── Navigation ───────────────────────────────────────────────────

  goBack(): void {
    this.router.navigate(['/warehouses']);
  }

  onTxFilter(): void { /* getter handles */ }

  // ─── Edit warehouse ───────────────────────────────────────────────

  openEditModal(): void {
    if (!this.warehouse) return;
    this.editForm = {
      name: this.warehouse.name,
      maxCapacity: this.warehouse.maxCapacity,
      unitCapacity: this.warehouse.unitCapacity,
      addressId: this.warehouse.addressId,
      status: this.warehouse.status,
    };
    this.showEditModal = true;
  }

  saveEdit(): void {
    this.isSaving = true;
    this.warehouseService.update(this.warehouseId, this.editForm as WarehouseUpdateRequest).subscribe({
      next: res => {
        this.isSaving = false;
        this.showEditModal = false;
        this.warehouse = res.result ?? this.warehouse;
        this.showAlert('Cập nhật kho thành công.');
      },
      error: () => { this.isSaving = false; this.showAlert('Cập nhật thất bại.', 'error'); },
    });
  }

  // ─── Update inventory ─────────────────────────────────────────────

  openUpdateInvModal(inv: InventoryResponse): void {
    this.editingInv = inv;
    this.invForm = { stock: inv.stock, batchCode: inv.batchCode, status: inv.status };
    this.showUpdateInvModal = true;
  }

  saveInventory(): void {
    if (!this.editingInv) return;
    this.isSaving = true;
    const req: InventoryUpdateRequest = {
      stock: this.invForm.stock,
      status: this.invForm.status,
      batchCode: this.invForm.batchCode,
    };
    this.inventoryService.update(this.editingInv.id, req).subscribe({
      next: res => {
        this.isSaving = false;
        this.showUpdateInvModal = false;
        const idx = this.inventories.findIndex(i => i.id === this.editingInv!.id);
        if (idx !== -1 && res.result) this.inventories[idx] = res.result;
        this.showAlert('Cập nhật lô hàng thành công.');
      },
      error: () => { this.isSaving = false; this.showAlert('Cập nhật thất bại.', 'error'); },
    });
  }

  // ═══════════════════════════════════════════════════════════════════
  //  VARIANT SEARCH (SHARED)
  // ═══════════════════════════════════════════════════════════════════

  searchVariantsLocal(term: string): VariantResponse[] {
    if (!term.trim()) return this.allVariants.slice(0, 10);
    const q = term.toLowerCase();
    return this.allVariants.filter(v =>
      String(v.id).includes(q) ||
      v.color?.toLowerCase().includes(q) ||
      v.name?.toLowerCase().includes(q)
    ).slice(0, 10);
  }

  onInboundSearchInput(line: InboundLine): void {
    clearTimeout(this.variantSearchDebounce);
    this.variantSearchDebounce = setTimeout(() => {
      line.dropdownResults = this.searchVariantsLocal(line.searchTerm);
      line.showDropdown = line.dropdownResults.length > 0 && !line.variantId;
    }, 150);
  }

  onOutboundSearchInput(line: OutboundLine): void {
    clearTimeout(this.variantSearchDebounce);
    this.variantSearchDebounce = setTimeout(() => {
      line.dropdownResults = this.searchVariantsLocal(line.searchTerm);
      line.showDropdown = line.dropdownResults.length > 0 && !line.variantId;
    }, 150);
  }

  // ─── Close dropdowns on outside click ────────────────────────────
  @HostListener('document:click')
  closeAllDropdowns(): void {
    this.inboundLines.forEach(l => l.showDropdown = false);
    this.outboundLines.forEach(l => l.showDropdown = false);
  }

  stopPropagation(event: Event): void {
    event.stopPropagation();
  }

  // ═══════════════════════════════════════════════════════════════════
  //  NHẬP KHO
  // ═══════════════════════════════════════════════════════════════════

  openInboundModal(): void {
    this.inboundForm = { supplierId: null, note: '', staffId: 1 };
    this.inboundLines = [this.newInboundLine()];
    this.showInboundModal = true;
  }

  private newInboundLine(): InboundLine {
    return {
      variantId: null, quantity: 1, cost: 0, batchCode: '',
      variantInfo: null, searching: false, searchTerm: '',
      showDropdown: false, dropdownResults: [],
      proposedPrice: 0, finalPrice: 0, priceEdited: false,
      currentStock: 0, currentPrice: 0,
    };
  }

  addInboundLine(): void {
    this.inboundLines.push(this.newInboundLine());
  }

  removeInboundLine(i: number): void {
    if (this.inboundLines.length === 1) return;
    this.inboundLines.splice(i, 1);
  }

  selectVariantForInbound(line: InboundLine, variant: VariantResponse): void {
    line.variantId = variant.id;
    line.variantInfo = variant;
    line.searchTerm = `${variant.name ?? ''} #${variant.id} - ${variant.color}`;
    line.showDropdown = false;
    line.currentPrice = variant.price ?? 0;

    // Lấy tồn kho hiện tại trong warehouse này
    const inv = this.inventories.find(i => i.variantId === variant.id && i.status === 'ACTIVE');
    line.currentStock = inv?.stock ?? 0;

    // Tính giá TB ban đầu (chưa nhập cost nên dùng giá hiện tại)
    this.recalcProposedPrice(line);
  }

  clearInboundVariant(line: InboundLine): void {
    line.variantId = null;
    line.variantInfo = null;
    line.searchTerm = '';
    line.proposedPrice = 0;
    line.finalPrice = 0;
    line.priceEdited = false;
    line.currentStock = 0;
    line.currentPrice = 0;
    line.showDropdown = false;
  }

  /**
   * Tính giá trung bình có trọng số:
   * newPrice = (currentStock * currentPrice + quantity * cost) / (currentStock + quantity)
   */
  recalcProposedPrice(line: InboundLine): void {
    const totalQty = line.currentStock + line.quantity;
    if (totalQty <= 0) {
      line.proposedPrice = line.cost > 0 ? line.cost : line.currentPrice;
    } else {
      line.proposedPrice = Math.round(
        (line.currentStock * line.currentPrice + line.quantity * line.cost) / totalQty
      );
    }
    // Nếu người dùng chưa tự sửa, tự động cập nhật finalPrice
    if (!line.priceEdited) {
      line.finalPrice = line.proposedPrice;
    }
  }

  onInboundCostChange(line: InboundLine): void {
    this.recalcProposedPrice(line);
  }

  onInboundQtyChange(line: InboundLine): void {
    this.recalcProposedPrice(line);
  }

  onFinalPriceChange(line: InboundLine): void {
    line.priceEdited = (line.finalPrice !== line.proposedPrice);
  }

  resetFinalPrice(line: InboundLine): void {
    line.finalPrice = line.proposedPrice;
    line.priceEdited = false;
  }

  saveInbound(): void {
    if (!this.inboundForm.supplierId) {
      this.showAlert('Vui lòng chọn nhà cung cấp.', 'error'); return;
    }
    const invalidLine = this.inboundLines.find(l => !l.variantId || l.quantity < 1 || l.cost < 0);
    if (invalidLine) {
      this.showAlert('Vui lòng điền đầy đủ thông tin các dòng hàng.', 'error'); return;
    }
    const invalidPrice = this.inboundLines.find(l => !l.finalPrice || l.finalPrice <= 0);
    if (invalidPrice) {
      this.showAlert('Giá variant phải lớn hơn 0.', 'error'); return;
    }

    const details: TransactionDetailRequest[] = this.inboundLines.map(l => ({
      variantId: l.variantId!,
      quantity: l.quantity,
      cost: l.cost,
      batchCode: l.batchCode || undefined,
      updatedPrice: l.finalPrice,   // gửi giá variant mới về backend
    }));

    const req: WarehouseTransactionCreateRequest = {
      note: this.inboundForm.note,
      transactionType: 'INBOUND',
      referenceType: 'PURCHASE',
      supplierId: this.inboundForm.supplierId,
      staffId: this.inboundForm.staffId,
      warehouseId: this.warehouseId,
      details,
    };

    this.isSaving = true;
    this.txService.createInbound(req).subscribe({
      next: res => {
        this.isSaving = false;
        this.showInboundModal = false;
        if (res.result) this.transactions.unshift(res.result);
        this.loadInventories();
        this.loadAllVariants(); // reload giá variant mới
        this.showAlert('Nhập kho thành công.');
      },
      error: () => { this.isSaving = false; this.showAlert('Nhập kho thất bại.', 'error'); },
    });
  }

  // ═══════════════════════════════════════════════════════════════════
  //  XUẤT KHO
  // ═══════════════════════════════════════════════════════════════════

  openOutboundModal(): void {
    this.outboundForm = { note: '', staffId: 1, referenceType: 'MANUAL', orderId: null };
    this.outboundLines = [this.newOutboundLine()];
    this.showOutboundModal = true;
  }

  private newOutboundLine(): OutboundLine {
    return {
      variantId: null, quantity: 1, variantInfo: null,
      searching: false, searchTerm: '', maxStock: 0,
      showDropdown: false, dropdownResults: [],
    };
  }

  addOutboundLine(): void {
    this.outboundLines.push(this.newOutboundLine());
  }

  removeOutboundLine(i: number): void {
    if (this.outboundLines.length === 1) return;
    this.outboundLines.splice(i, 1);
  }

  selectVariantForOutbound(line: OutboundLine, variant: VariantResponse): void {
    line.variantId = variant.id;
    line.variantInfo = variant;
    line.searchTerm = `${variant.name ?? ''} #${variant.id} - ${variant.color}`;
    line.showDropdown = false;

    // Lấy tồn kho trong warehouse này
    const inv = this.inventories.find(i => i.variantId === variant.id && i.status === 'ACTIVE');
    line.maxStock = inv?.stock ?? 0;
  }

  clearOutboundVariant(line: OutboundLine): void {
    line.variantId = null;
    line.variantInfo = null;
    line.searchTerm = '';
    line.maxStock = 0;
    line.showDropdown = false;
  }

  saveOutbound(): void {
    const invalidLine = this.outboundLines.find(l => !l.variantId || l.quantity < 1);
    if (invalidLine) {
      this.showAlert('Vui lòng điền đầy đủ thông tin các dòng hàng.', 'error'); return;
    }
    const overStock = this.outboundLines.find(l => l.maxStock > 0 && l.quantity > l.maxStock);
    if (overStock) {
      this.showAlert(`Số lượng xuất vượt quá tồn kho (tối đa ${overStock.maxStock}).`, 'error'); return;
    }

    const details: TransactionDetailRequest[] = this.outboundLines.map(l => ({
      variantId: l.variantId!,
      quantity: l.quantity,
      cost: 0,
    }));

    const req: WarehouseTransactionCreateRequest = {
      note: this.outboundForm.note,
      transactionType: 'OUTBOUND',
      referenceType: this.outboundForm.referenceType,
      orderId: this.outboundForm.orderId ?? undefined,
      staffId: this.outboundForm.staffId,
      warehouseId: this.warehouseId,
      details,
    };

    this.isSaving = true;
    this.txService.createOutbound(req).subscribe({
      next: res => {
        this.isSaving = false;
        this.showOutboundModal = false;
        if (res.result) this.transactions.unshift(res.result);
        this.loadInventories();
        this.showAlert('Xuất kho thành công.');
      },
      error: () => { this.isSaving = false; this.showAlert('Xuất kho thất bại.', 'error'); },
    });
  }

  // ─── Cancel transaction ───────────────────────────────────────────

  cancelTransaction(id: number): void {
    if (!confirm('Huỷ giao dịch này?')) return;
    this.txService.cancel(id).subscribe({
      next: res => {
        const idx = this.transactions.findIndex(t => t.id === id);
        if (idx !== -1 && res.result) this.transactions[idx] = res.result;
        this.showAlert('Đã huỷ giao dịch.');
      },
      error: () => { this.showAlert('Huỷ giao dịch thất bại.', 'error'); },
    });
  }

  // ─── Alert ────────────────────────────────────────────────────────

  showAlert(msg: string, type: 'success' | 'error' = 'success'): void {
    this.alertMsg = msg;
    this.alertType = type;
    setTimeout(() => (this.alertMsg = ''), 3500);
  }

  formatCurrency(v: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);
  }
}