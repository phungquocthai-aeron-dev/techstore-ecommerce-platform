import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { WarehouseService } from './warehouse.service';
import { InventoryService } from './inventory.service';
import { WarehouseTransactionService } from './warehouse-transaction.service';

import { WarehouseResponse } from './models/warehouse.model';
import { InventoryResponse } from './models/inventory.model';
import { InventoryUpdateRequest } from './models/inventory-request.model';
import { WarehouseTransactionResponse } from './models/warehouse-transaction.model';
import { WarehouseUpdateRequest } from './models/warehouse-request.model';

@Component({
  selector: 'app-warehouse-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './warehouse-detail.component.html',
  styleUrls: ['./warehouse.component.css']
})
export class WarehouseDetailComponent implements OnInit {

  warehouseId!: number;
  warehouse: WarehouseResponse | null = null;

  isLoading    = false;
  isLoadingInv = false;
  isLoadingTx  = false;
  isSaving     = false;

  alertMsg  = '';
  alertType: 'success' | 'error' = 'success';

  // tabs
  activeTab: 'inventory' | 'transactions' = 'inventory';

  // inventory
  inventories: InventoryResponse[] = [];
  invSearch = '';

  // transactions
  transactions: WarehouseTransactionResponse[] = [];
  txTypeFilter   = '';
  txStatusFilter = '';

  // modal: edit warehouse
  showEditModal = false;
  editForm: Partial<WarehouseUpdateRequest> & { status?: string } = {};

  // modal: update inventory
  showUpdateInvModal = false;
  editingInv: InventoryResponse | null = null;
  invForm: { stock: number; batchCode?: string; status?: string } = { stock: 0 };

  // modal: inbound (placeholder — extend as needed)
  showInboundModal = false;

  // ─── Computed ─────────────────────────────────────────────────────

  get activeInventoryCount(): number {
    return this.inventories.filter(i => i.status === 'ACTIVE').length;
  }

  get filteredInventories(): InventoryResponse[] {
    if (!this.invSearch.trim()) return this.inventories;
    const q = this.invSearch.toLowerCase();
    return this.inventories.filter(i =>
      i.batchCode?.toLowerCase().includes(q) ||
      i.status?.toLowerCase().includes(q) ||
      i.variantInfo?.color?.toLowerCase().includes(q)
    );
  }

  get filteredTransactions(): WarehouseTransactionResponse[] {
    return this.transactions.filter(tx => {
      const matchType   = !this.txTypeFilter   || tx.transactionType === this.txTypeFilter;
      const matchStatus = !this.txStatusFilter || tx.status === this.txStatusFilter;
      return matchType && matchStatus;
    });
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private warehouseService: WarehouseService,
    private inventoryService: InventoryService,
    private txService: WarehouseTransactionService
  ) {}

  ngOnInit(): void {
    this.warehouseId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadWarehouse();
    this.loadInventories();
    this.loadTransactions();
  }

  // ─── Load ─────────────────────────────────────────────────────────

  loadWarehouse(): void {
    this.isLoading = true;
    this.warehouseService.getById(this.warehouseId).subscribe({
      next: res => { this.warehouse = res.result ?? null; this.isLoading = false; },
      error: ()  => { this.showAlert('Không thể tải thông tin kho.', 'error'); this.isLoading = false; }
    });
  }

  loadInventories(): void {
    this.isLoadingInv = true;
    this.inventoryService.getByWarehouse(this.warehouseId).subscribe({
      next: res => { this.inventories = res.result ?? []; this.isLoadingInv = false; },
      error: ()  => { this.isLoadingInv = false; }
    });
  }

  loadTransactions(): void {
    this.isLoadingTx = true;
    this.txService.getByWarehouse(this.warehouseId).subscribe({
      next: res => { this.transactions = res.result ?? []; this.isLoadingTx = false; },
      error: ()  => { this.isLoadingTx = false; }
    });
  }

  // ─── Navigation ───────────────────────────────────────────────────

  goBack(): void {
    this.router.navigate(['/warehouses']);
  }

  // ─── Tx Filter ────────────────────────────────────────────────────

  onTxFilter(): void { /* filteredTransactions getter handles it */ }

  // ─── Edit warehouse ───────────────────────────────────────────────

  openEditModal(): void {
    if (!this.warehouse) return;
    this.editForm = {
      name: this.warehouse.name,
      maxCapacity: this.warehouse.maxCapacity,
      unitCapacity: this.warehouse.unitCapacity,
      addressId: this.warehouse.addressId,
      status: this.warehouse.status
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
      error: () => { this.isSaving = false; this.showAlert('Cập nhật thất bại.', 'error'); }
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
      batchCode: this.invForm.batchCode
    };
    this.inventoryService.update(this.editingInv.id, req).subscribe({
      next: res => {
        this.isSaving = false;
        this.showUpdateInvModal = false;
        const idx = this.inventories.findIndex(i => i.id === this.editingInv!.id);
        if (idx !== -1 && res.result) this.inventories[idx] = res.result;
        this.showAlert('Cập nhật lô hàng thành công.');
      },
      error: () => { this.isSaving = false; this.showAlert('Cập nhật thất bại.', 'error'); }
    });
  }

  // ─── Inbound (placeholder) ────────────────────────────────────────

  openInboundModal(): void {
    this.showInboundModal = true;
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
      error: () => { this.showAlert('Huỷ giao dịch thất bại.', 'error'); }
    });
  }

  // ─── Alert ────────────────────────────────────────────────────────

  showAlert(msg: string, type: 'success' | 'error' = 'success'): void {
    this.alertMsg  = msg;
    this.alertType = type;
    setTimeout(() => (this.alertMsg = ''), 3500);
  }
}