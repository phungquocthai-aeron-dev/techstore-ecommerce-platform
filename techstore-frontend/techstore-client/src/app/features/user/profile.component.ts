import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomerService } from './customer.service';
import { AddressService } from './address.service';
import { ShippingService } from '../check-out/shipping.service';
import { CustomerResponse, CustomerUpdateRequest } from './models/customer.model';
import { AddressResponse, AddressRequest } from './models/address.model';
import { ProvinceData, DistrictData, WardData } from '../check-out/models/shipping.model';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  // ─── State ───────────────────────────────────────────
  customerId = 1; // TODO: lấy từ AuthService / localStorage

  customer: CustomerResponse | null = null;
  addresses: AddressResponse[] = [];

  // Form: thông tin cá nhân
  profileForm = { fullName: '', phone: '', dob: '' };

  // Form: đổi mật khẩu
  passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' };

  // Form: địa chỉ (thêm/sửa)
  addressForm: AddressRequest = { address: '', provinceId: 0, districtId: 0, wardCode: '' };
  editingAddressId: number | null = null;
  showAddressModal = false;
  addressModalTitle = 'Thêm Địa Chỉ Mới';

  // Shipping dropdowns
  readonly SHIPPING_TYPE = 'ghn';
  provinces: ProvinceData[] = [];
  districts: DistrictData[] = [];
  wards: WardData[] = [];
  loadingProvinces = false;
  loadingDistricts = false;
  loadingWards = false;

  // Alert
  alert: { type: 'success' | 'error'; message: string } | null = null;

  // Avatar preview
  avatarUrl = '';
  imageBaseUrl = environment.imageAvatarUrl;

  // Loading
  loading = false;
  savingProfile = false;
  savingPassword = false;
  savingAddress = false;

  constructor(
    private customerService: CustomerService,
    private addressService: AddressService,
    private shippingService: ShippingService
  ) {}

  ngOnInit(): void {
    this.loadCustomer();
    this.loadAddresses();
  }

  // ─── Load data ────────────────────────────────────────

  loadCustomer(): void {
    this.loading = true;
    this.customerService.getById(this.customerId).subscribe({
      next: res => {
        this.customer = res.result ?? null;
        this.avatarUrl = res.result?.avatarUrl ? res.result.avatarUrl : 'images/avatar_men.png';
        console.log(this.avatarUrl)
        this.profileForm = {
          fullName: res.result?.fullName ?? '',
          phone: res.result?.phone ?? '',
          dob: res.result?.dob ?? ''
        };
        this.loading = false;
      },
      error: () => {
        this.showAlert('error', 'Không thể tải thông tin cá nhân!');
        this.loading = false;
      }
    });
  }

  loadAddresses(): void {
    this.addressService.getByCustomerId(this.customerId).subscribe({
      next: res => { this.addresses = res.result ?? []; },
      error: () => { this.showAlert('error', 'Không thể tải danh sách địa chỉ!'); }
    });
  }

  // ─── Shipping dropdowns ───────────────────────────────

  loadProvinces(): void {
    this.loadingProvinces = true;
    this.shippingService.getProvinces(this.SHIPPING_TYPE).subscribe({
      next: res => {
        this.provinces = res.result ?? [];
        console.log(res.result)
        this.loadingProvinces = false;
      },
      error: () => {
        this.showAlert('error', 'Không thể tải danh sách tỉnh/thành!');
        this.loadingProvinces = false;
      }
    });
  }

  onProvinceChange(): void {
    // Reset district & ward
    this.addressForm.districtId = 0;
    this.addressForm.wardCode = '';
    this.districts = [];
    this.wards = [];

    if (!this.addressForm.provinceId) return;

    this.loadingDistricts = true;
    this.shippingService.getDistricts(this.SHIPPING_TYPE, this.addressForm.provinceId).subscribe({
      next: res => {
        this.districts = res.result ?? [];
        this.loadingDistricts = false;
      },
      error: () => {
        this.showAlert('error', 'Không thể tải danh sách quận/huyện!');
        this.loadingDistricts = false;
      }
    });
  }

  onDistrictChange(): void {
    // Reset ward
    this.addressForm.wardCode = '';
    this.wards = [];

    if (!this.addressForm.districtId) return;

    this.loadingWards = true;
    this.shippingService.getWards(this.SHIPPING_TYPE, this.addressForm.districtId).subscribe({
      next: res => {
        this.wards = res.result ?? [];
        this.loadingWards = false;
      },
      error: () => {
        this.showAlert('error', 'Không thể tải danh sách phường/xã!');
        this.loadingWards = false;
      }
    });
  }

  // ─── Profile ──────────────────────────────────────────

  saveProfile(): void {
    if (!this.profileForm.fullName || !this.profileForm.phone) {
      this.showAlert('error', 'Vui lòng điền đầy đủ thông tin!');
      return;
    }

    const req: CustomerUpdateRequest = {
      fullName: this.profileForm.fullName,
      phone: this.profileForm.phone,
      dob: this.profileForm.dob
    };

    this.savingProfile = true;
    this.customerService.updateInfo(this.customerId, req).subscribe({
      next: res => {
        this.customer = res.result ?? null;
        this.showAlert('success', 'Lưu thông tin cá nhân thành công!');
        this.savingProfile = false;
      },
      error: () => {
        this.showAlert('error', 'Cập nhật thất bại, vui lòng thử lại!');
        this.savingProfile = false;
      }
    });
  }

  resetProfile(): void {
    if (this.customer) {
      this.profileForm = {
        fullName: this.customer.fullName,
        phone: this.customer.phone,
        dob: this.customer.dob
      };
    }
  }

  // ─── Avatar ───────────────────────────────────────────

  triggerAvatarInput(): void {
    const input = document.getElementById('avatarInput') as HTMLInputElement;
    input?.click();
  }

  onAvatarChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = e => { this.avatarUrl = e.target?.result as string; };
    reader.readAsDataURL(file);

    this.customerService.uploadAvatar(this.customerId, file).subscribe({
      next: () => { this.showAlert('success', 'Cập nhật ảnh đại diện thành công!'); },
      error: () => { this.showAlert('error', 'Upload ảnh thất bại!'); }
    });
  }

  // ─── Password ─────────────────────────────────────────

  savePassword(): void {
    if (!this.passwordForm.oldPassword || !this.passwordForm.newPassword) {
      this.showAlert('error', 'Vui lòng điền đầy đủ thông tin!');
      return;
    }
    if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
      this.showAlert('error', 'Mật khẩu xác nhận không khớp!');
      return;
    }
    if (this.passwordForm.newPassword.length < 6) {
      this.showAlert('error', 'Mật khẩu mới phải có ít nhất 6 ký tự!');
      return;
    }

    this.savingPassword = true;
    this.customerService.updatePassword(
      this.customerId,
      this.passwordForm.oldPassword,
      this.passwordForm.newPassword,
      this.passwordForm.confirmPassword
    ).subscribe({
      next: () => {
        this.showAlert('success', 'Đổi mật khẩu thành công!');
        this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' };
        this.savingPassword = false;
      },
      error: () => {
        this.showAlert('error', 'Mật khẩu hiện tại không đúng!');
        this.savingPassword = false;
      }
    });
  }

  // ─── Address ──────────────────────────────────────────

  openAddAddress(): void {
    this.editingAddressId = null;
    this.addressForm = { address: '', provinceId: 0, districtId: 0, wardCode: '' };
    this.districts = [];
    this.wards = [];
    this.addressModalTitle = 'Thêm Địa Chỉ Mới';
    this.showAddressModal = true;
    // Load provinces khi mở modal
    if (this.provinces.length === 0) {
      this.loadProvinces();
    }
  }

  openEditAddress(addr: AddressResponse): void {
    this.editingAddressId = addr.id;
    this.addressForm = {
      address: addr.address,
      provinceId: addr.provinceId,
      districtId: addr.districtId,
      wardCode: addr.wardCode
    };
    this.addressModalTitle = 'Sửa Địa Chỉ';
    this.showAddressModal = true;

    // Load provinces, rồi cascade load districts & wards theo giá trị hiện tại
    if (this.provinces.length === 0) {
      this.loadingProvinces = true;
      this.shippingService.getProvinces(this.SHIPPING_TYPE).subscribe({
        next: res => {
          this.provinces = res.result ?? [];
          this.loadingProvinces = false;
          this.preloadDistrictsAndWards(addr.provinceId, addr.districtId);
        },
        error: () => { this.loadingProvinces = false; }
      });
    } else {
      this.preloadDistrictsAndWards(addr.provinceId, addr.districtId);
    }
  }

  /** Dùng khi mở edit: load districts theo province, rồi wards theo district */
  private preloadDistrictsAndWards(provinceId: number, districtId: number): void {
    this.loadingDistricts = true;
    this.shippingService.getDistricts(this.SHIPPING_TYPE, provinceId).subscribe({
      next: res => {
        this.districts = res.result ?? [];
        this.loadingDistricts = false;
        if (districtId) {
          this.loadingWards = true;
          this.shippingService.getWards(this.SHIPPING_TYPE, districtId).subscribe({
            next: wRes => {
              this.wards = wRes.result ?? [];
              this.loadingWards = false;
            },
            error: () => { this.loadingWards = false; }
          });
        }
      },
      error: () => { this.loadingDistricts = false; }
    });
  }

  closeAddressModal(): void {
    this.showAddressModal = false;
  }

  saveAddress(): void {
    if (!this.addressForm.address || !this.addressForm.provinceId || !this.addressForm.districtId) {
      this.showAlert('error', 'Vui lòng điền đầy đủ thông tin địa chỉ!');
      return;
    }

    this.savingAddress = true;

    if (this.editingAddressId) {
      this.addressService.update(this.editingAddressId, this.addressForm).subscribe({
        next: () => {
          this.showAlert('success', 'Cập nhật địa chỉ thành công!');
          this.loadAddresses();
          this.closeAddressModal();
          this.savingAddress = false;
        },
        error: () => {
          this.showAlert('error', 'Cập nhật địa chỉ thất bại!');
          this.savingAddress = false;
        }
      });
    } else {
      this.addressService.create(this.customerId, this.addressForm).subscribe({
        next: () => {
          this.showAlert('success', 'Thêm địa chỉ mới thành công!');
          this.loadAddresses();
          this.closeAddressModal();
          this.savingAddress = false;
        },
        error: () => {
          this.showAlert('error', 'Thêm địa chỉ thất bại!');
          this.savingAddress = false;
        }
      });
    }
  }

  deleteAddress(id: number): void {
    if (!confirm('Bạn có chắc muốn xóa địa chỉ này?')) return;

    this.addressService.delete(id).subscribe({
      next: () => {
        this.showAlert('success', 'Xóa địa chỉ thành công!');
        this.loadAddresses();
      },
      error: () => { this.showAlert('error', 'Xóa địa chỉ thất bại!'); }
    });
  }

  // ─── Alert ────────────────────────────────────────────

  showAlert(type: 'success' | 'error', message: string): void {
    this.alert = { type, message };
    setTimeout(() => { this.alert = null; }, 3500);
  }

  handleImageError(event: any) {
    event.target.src = 'images/avatar_men.png';
  }
}