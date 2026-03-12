import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  isLightTheme = false;
  isMobileMenuOpen = false;
  searchKeyword = '';

  constructor(private router: Router) {}

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

    goSearch(): void {
    const kw = this.searchKeyword.trim();
    if (kw) {
      this.router.navigate(['/search'], { queryParams: { keyword: kw } });
    } else {
      this.router.navigate(['/search']);
    }
  }
}