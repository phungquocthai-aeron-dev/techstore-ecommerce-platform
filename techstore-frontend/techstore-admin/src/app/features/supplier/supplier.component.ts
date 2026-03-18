import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { SupplierService } from './supplier.service';
import { SupplierResponse } from './models/supplier.model';
import { SupplierCreateRequest, SupplierUpdateRequest } from './models/supplier-request.model';

@Component({
  selector: 'app-supplier',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './supplier.component.html',
  styleUrls: ['./supplier.component.css']
})
export class SupplierComponent implements OnInit {

  suppliers: SupplierResponse[] = [];
  isLoading = false;
  isSaving  = false;

  // alert
  alertMsg  = '';
  alertType: 'success' | 'error' = 'success';

  // toolbar
  searchName   = '';
  filterStatus = '';

  // modal create/edit
  showModal = false;
  editingId: number | null = null;
  form: { name: string; phone: string; status?: string } = { name: '', phone: '' };

  // modal delete
  showDeleteModal    = false;
  deletingSupplier: SupplierResponse | null = null;

  constructor(private supplierService: SupplierService) {}

  ngOnInit(): void {
    this.loadAll();
  }

  // ─── Load ─────────────────────────────────────────────────────────

  loadAll(): void {
    this.isLoading = true;
    this.supplierService.getAll().subscribe({
      next: res => { this.suppliers = res.result ?? []; this.isLoading = false; },
      error: ()  => { this.showAlert('Không thể tải danh sách nhà cung cấp.', 'error'); this.isLoading = false; }
    });
  }

  // ─── Search / Filter ──────────────────────────────────────────────

  onSearch(): void {
    if (!this.searchName.trim()) { this.loadAll(); return; }
    this.isLoading = true;
    this.supplierService.searchByName(this.searchName.trim()).subscribe({
      next: res => { this.suppliers = res.result ?? []; this.isLoading = false; },
      error: ()  => { this.isLoading = false; }
    });
  }

  onFilterStatus(): void {
    if (!this.filterStatus) { this.loadAll(); return; }
    this.isLoading = true;
    this.supplierService.getByStatus(this.filterStatus).subscribe({
      next: res => { this.suppliers = res.result ?? []; this.isLoading = false; },
      error: ()  => { this.isLoading = false; }
    });
  }

  // ─── Modal ────────────────────────────────────────────────────────

  openCreateModal(): void {
    this.editingId = null;
    this.form = { name: '', phone: '' };
    this.showModal = true;
  }

  openEditModal(s: SupplierResponse): void {
    this.editingId = s.id;
    this.form = { name: s.name, phone: s.phone, status: s.status };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  // ─── Save ─────────────────────────────────────────────────────────

  saveSupplier(): void {
    if (!this.form.name.trim() || !this.form.phone.trim()) {
      this.showAlert('Vui lòng điền đầy đủ thông tin.', 'error'); return;
    }
    this.isSaving = true;

    if (this.editingId) {
      const req: SupplierUpdateRequest = { name: this.form.name, phone: this.form.phone, status: this.form.status };
      this.supplierService.update(this.editingId, req).subscribe({
        next: () => { this.isSaving = false; this.closeModal(); this.loadAll(); this.showAlert('Cập nhật thành công.'); },
        error: () => { this.isSaving = false; this.showAlert('Cập nhật thất bại.', 'error'); }
      });
    } else {
      const req: SupplierCreateRequest = { name: this.form.name, phone: this.form.phone };
      this.supplierService.create(req).subscribe({
        next: () => { this.isSaving = false; this.closeModal(); this.loadAll(); this.showAlert('Tạo nhà cung cấp thành công.'); },
        error: () => { this.isSaving = false; this.showAlert('Tạo thất bại.', 'error'); }
      });
    }
  }

  // ─── Status toggle ────────────────────────────────────────────────

  toggleStatus(s: SupplierResponse): void {
    const next = s.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.supplierService.updateStatus(s.id, next).subscribe({
      next: () => { s.status = next; this.showAlert('Cập nhật trạng thái thành công.'); },
      error: () => { this.showAlert('Cập nhật trạng thái thất bại.', 'error'); }
    });
  }

  // ─── Delete ───────────────────────────────────────────────────────

  confirmDelete(s: SupplierResponse): void {
    this.deletingSupplier = s;
    this.showDeleteModal  = true;
  }

  deleteSupplier(): void {
    if (!this.deletingSupplier) return;
    this.isSaving = true;
    this.supplierService.delete(this.deletingSupplier.id).subscribe({
      next: () => {
        this.isSaving = false;
        this.showDeleteModal = false;
        this.suppliers = this.suppliers.filter(s => s.id !== this.deletingSupplier!.id);
        this.showAlert('Đã xoá nhà cung cấp.');
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