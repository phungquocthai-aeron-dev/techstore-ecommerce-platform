import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  /** Nhận trạng thái collapsed từ layout cha */
  @Input() isCollapsed = false;

  /** Phát event khi user click toggle (nếu cần xử lý ở cha) */
  @Output() toggleCollapse = new EventEmitter<void>();

  toggle(): void {
    this.toggleCollapse.emit();
  }
}