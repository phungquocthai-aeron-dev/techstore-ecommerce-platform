import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { WarehouseService } from './warehouse.service';
import { WarehouseResponse } from './models/warehouse.model';
import { WarehouseCreateRequest, WarehouseUpdateRequest } from './models/warehouse-request.model';

@Component({
  selector: 'app-warehouse',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './warehouse.component.html',
  styleUrls: ['./warehouse.component.css']
})
export class WarehouseComponent implements OnInit {

  warehouses: WarehouseResponse[] = [];
  isLoading = false;
  isSaving  = false;

  alertMsg  = '';
  alertType: 'success' | 'error' = 'success';

  filterStatus = '';
  viewMode: 'table' | 'grid' = 'table';

  showModal = false;
  editingId: number | null = null;
  form: {
    name: string; maxCapacity: string; unitCapacity: string;
    addressId: string; status?: string;
  } = { name: '', maxCapacity: '', unitCapacity: '', addressId: '' };

  showDeleteModal      = false;
  deletingWarehouse: WarehouseResponse | null = null;

  get activeCount(): number {
    return this.warehouses.filter(w => w.status === 'ACTIVE').length;
  }

  constructor(
    private warehouseService: WarehouseService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  // ─── Load ─────────────────────────────────────────────────────────

  loadAll(): void {
    this.isLoading = true;
    this.warehouseService.getAll().subscribe({
      next: res => { this.warehouses = res.result ?? []; this.isLoading = false; },
      error: ()  => { this.showAlert('Không thể tải danh sách kho.', 'error'); this.isLoading = false; }
    });
  }

  onFilterStatus(): void {
    if (!this.filterStatus) { this.loadAll(); return; }
    this.isLoading = true;
    this.warehouseService.getByStatus(this.filterStatus).subscribe({
      next: res => { this.warehouses = res.result ?? []; this.isLoading = false; },
      error: ()  => { this.isLoading = false; }
    });
  }

  // ─── Navigate ─────────────────────────────────────────────────────

  goToDetail(id: number): void {
    this.router.navigate(['/warehouses', id]);
  }

  // ─── Modal ────────────────────────────────────────────────────────

  openCreateModal(): void {
    this.editingId = null;
    this.form = { name: '', maxCapacity: '', unitCapacity: '', addressId: '' };
    this.showModal = true;
  }

  openEditModal(w: WarehouseResponse): void {
    this.editingId = w.id;
    this.form = {
      name: w.name, maxCapacity: w.maxCapacity,
      unitCapacity: w.unitCapacity, addressId: w.addressId,
      status: w.status
    };
    this.showModal = true;
  }

  closeModal(): void { this.showModal = false; }

  // ─── Save ─────────────────────────────────────────────────────────

  saveWarehouse(): void {
    if (!this.form.name || !this.form.maxCapacity || !this.form.unitCapacity || !this.form.addressId) {
      this.showAlert('Vui lòng điền đầy đủ thông tin.', 'error'); return;
    }
    this.isSaving = true;

    if (this.editingId) {
      const req: WarehouseUpdateRequest = { ...this.form };
      this.warehouseService.update(this.editingId, req).subscribe({
        next: () => { this.isSaving = false; this.closeModal(); this.loadAll(); this.showAlert('Cập nhật kho thành công.'); },
        error: () => { this.isSaving = false; this.showAlert('Cập nhật thất bại.', 'error'); }
      });
    } else {
      const req: WarehouseCreateRequest = {
        name: this.form.name, maxCapacity: this.form.maxCapacity,
        unitCapacity: this.form.unitCapacity, addressId: this.form.addressId
      };
      this.warehouseService.create(req).subscribe({
        next: () => { this.isSaving = false; this.closeModal(); this.loadAll(); this.showAlert('Tạo kho thành công.'); },
        error: () => { this.isSaving = false; this.showAlert('Tạo kho thất bại.', 'error'); }
      });
    }
  }

  // ─── Toggle status ────────────────────────────────────────────────

  toggleStatus(w: WarehouseResponse): void {
    const next = w.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.warehouseService.updateStatus(w.id, next).subscribe({
      next: () => { w.status = next; this.showAlert('Cập nhật trạng thái thành công.'); },
      error: () => { this.showAlert('Cập nhật trạng thái thất bại.', 'error'); }
    });
  }

  // ─── Delete ───────────────────────────────────────────────────────

  confirmDelete(w: WarehouseResponse): void {
    this.deletingWarehouse = w;
    this.showDeleteModal   = true;
  }

  deleteWarehouse(): void {
    if (!this.deletingWarehouse) return;
    this.isSaving = true;
    this.warehouseService.delete(this.deletingWarehouse.id).subscribe({
      next: () => {
        this.isSaving = false;
        this.showDeleteModal = false;
        this.warehouses = this.warehouses.filter(w => w.id !== this.deletingWarehouse!.id);
        this.showAlert('Đã xoá kho.');
      },
      error: () => { this.isSaving = false; this.showAlert('Xoá thất bại.', 'error'); }
    });
  }

  // ─── Alert ────────────────────────────────────────────────────────

  showAlert(msg: string, type: 'success' | 'error' = 'success'): void {
    this.alertMsg  = msg;
    this.alertType = type;
    setTimeout(() => (this.alertMsg = ''), 3500);
  }
}