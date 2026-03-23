import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { StaffService } from '../../../features/staff/staff.service';
import { AuthService } from '../../services/auth.service'; 
import { PermissionService } from '../../services/permission.service'; 
import { StaffResponse } from '../../../features/staff/models/staff.model'; 
import { Observable } from 'rxjs';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {

  @Input() isCollapsed = false;
  @Output() toggleCollapse = new EventEmitter<void>();

  currentStaff$: Observable<StaffResponse | null>;

  constructor(
    private staffService: StaffService,
    private authService: AuthService,
    public permService: PermissionService, // public để dùng trong template
    private router: Router
  ) {
    this.currentStaff$ = this.staffService.currentStaff$;
  }

  toggle(): void {
    this.toggleCollapse.emit();
  }

  canAccess(route: string): boolean {
    return this.permService.canAccess(route);
  }

  /** Lấy 2 chữ cái đầu của tên để hiển thị avatar */
  getAvatarText(fullName: string): string {
    if (!fullName) return 'ST';
    const parts = fullName.trim().split(' ');
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }

  logout(): void {
    this.authService.logout().subscribe({
      next:  () => this.router.navigate(['/auth']),
      error: () => this.router.navigate(['/auth'])
    });
  }
}