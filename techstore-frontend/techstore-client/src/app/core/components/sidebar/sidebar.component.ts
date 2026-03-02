import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface SidebarFilter {
  priceRange: [number, number];
  selectedCategories: string[];
  sortBy: string;
  onlyInStock: boolean;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  @Input() isOpen = true;
  @Output() filterChange = new EventEmitter<SidebarFilter>();
  @Output() closed = new EventEmitter<void>();

  categories = [
    { id: 'tech', label: 'Công nghệ', count: 42 },
    { id: 'design', label: 'Thiết kế', count: 28 },
    { id: 'business', label: 'Kinh doanh', count: 19 },
    { id: 'ai', label: 'Trí tuệ nhân tạo', count: 35 },
    { id: 'security', label: 'Bảo mật', count: 14 },
  ];

  sortOptions = [
    { value: 'newest', label: 'Mới nhất' },
    { value: 'popular', label: 'Phổ biến nhất' },
    { value: 'price_asc', label: 'Giá tăng dần' },
    { value: 'price_desc', label: 'Giá giảm dần' },
  ];

  priceMin = 0;
  priceMax = 5000000;
  selectedCategories: string[] = [];
  sortBy = 'newest';
  onlyInStock = false;

  toggleCategory(id: string): void {
    const idx = this.selectedCategories.indexOf(id);
    if (idx > -1) {
      this.selectedCategories.splice(idx, 1);
    } else {
      this.selectedCategories.push(id);
    }
    this.emitFilter();
  }

  isCategorySelected(id: string): boolean {
    return this.selectedCategories.includes(id);
  }

  emitFilter(): void {
    this.filterChange.emit({
      priceRange: [this.priceMin, this.priceMax],
      selectedCategories: [...this.selectedCategories],
      sortBy: this.sortBy,
      onlyInStock: this.onlyInStock
    });
  }

  resetFilters(): void {
    this.priceMin = 0;
    this.priceMax = 5000000;
    this.selectedCategories = [];
    this.sortBy = 'newest';
    this.onlyInStock = false;
    this.emitFilter();
  }

  formatPrice(value: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
  }

  close(): void {
    this.closed.emit();
  }
}