import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  isLightTheme = false;
  isMobileMenuOpen = false;

  navLinks = [
    { label: 'Trang chủ', path: '/', icon: '⌂' },
    { label: 'Sản phẩm', path: '/products', icon: '◈' },
    { label: 'Dịch vụ', path: '/services', icon: '◆' },
    { label: 'Liên hệ', path: '/contact', icon: '◉' },
  ];

  ngOnInit(): void {
    this.isLightTheme = document.body.classList.contains('light-theme');
  }

  toggleTheme(): void {
    this.isLightTheme = !this.isLightTheme;
    document.body.classList.toggle('light-theme', this.isLightTheme);
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }
}