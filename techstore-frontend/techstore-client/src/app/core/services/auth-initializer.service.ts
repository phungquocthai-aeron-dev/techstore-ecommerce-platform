import { Injectable } from "@angular/core";
import { CustomerService } from "../../features/user/customer.service";
import { AuthService } from "./auth.service";
import { TokenService } from "./token.service";

@Injectable({ providedIn: 'root' })
export class AuthInitializerService {

  constructor(
    private tokenService: TokenService,
    private authService: AuthService,
    private customerService: CustomerService
  ) {}

  init() {
    const token = this.tokenService.getToken();
    if (!token) return;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const customerId = Number(payload.sub);

      this.authService.refreshCustomerToken().subscribe({
        next: () => {
          this.customerService.loadCurrentUser(customerId).subscribe();
        },
        error: () => {
          this.tokenService.removeToken();
        }
      });

    } catch {
      this.tokenService.removeToken();
    }
  }
}