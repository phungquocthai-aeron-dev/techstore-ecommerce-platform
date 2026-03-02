import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProductService } from '../product/product.service';
import { ProductListResponse } from '../product/models/product.model';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  // ===== NEWEST =====
  latestProducts: ProductListResponse[] = [];
  loadingLatest = true;

  // ===== CATEGORY =====
  laptops: ProductListResponse[] = [];
  accessories: ProductListResponse[] = [];
  smartphones: ProductListResponse[] = [];

  loadingLaptop = true;
  loadingAccessory = true;
  loadingSmartphone = true;

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.loadLatestProducts();
    this.loadCategoryProducts();
  }

  // ===============================
  // LOAD NEWEST
  // ===============================

  loadLatestProducts(): void {
    this.productService.getLatest(8).subscribe({
      next: (res) => {
        this.latestProducts = res?.result || res?.result || [];
        this.loadingLatest = false;
      },
      error: () => this.loadingLatest = false
    });
  }

  // ===============================
  // LOAD CATEGORY
  // ===============================

  loadCategoryProducts(): void {

    this.productService.getByCategoryType("LAPTOP", 0, 4).subscribe({
      next: (res) => {
        this.laptops = res?.result?.content || [];
        this.loadingLaptop = false;
      },
      error: () => this.loadingLaptop = false
    });

    this.productService.getByCategoryType("ACCESSORY", 0, 4).subscribe({
      next: (res) => {
        this.accessories = res?.result?.content || [];
        this.loadingAccessory = false;
      },
      error: () => this.loadingAccessory = false
    });

    this.productService.getByCategoryType("SMARTPHONE", 0, 4).subscribe({
      next: (res) => {
        this.smartphones = res?.result?.content || [];
        this.loadingSmartphone = false;
      },
      error: () => this.loadingSmartphone = false
    });
  }

  // ===============================
  // IMAGE HANDLE
  // ===============================

  getImageUrl(image: string | null | undefined): string {
    if (!image) {
      return 'images/no-product-image.jpg';
    }

    if (image.startsWith('http')) {
      return image;
    }

    return environment.imageProductUrl + image;
  }

  handleImageError(event: any) {
    event.target.src = 'images/no-product-image.jpg';
  }
}