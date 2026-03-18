import { Injectable } from "@angular/core";
import { AuthService } from "./auth.service";
import { TokenService } from "./token.service";
import { StaffService } from "../../features/staff/staff.service";

@Injectable({ providedIn: 'root' })
export class AuthInitializerService {

  constructor(
    private tokenService: TokenService,
    private authService: AuthService,
    private staffService: StaffService
  ) {}

  init() {
    const token = this.tokenService.getToken();
    if (!token) return;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const staffId = Number(payload.sub);

      this.authService.refreshToken().subscribe({
        next: () => {
          this.staffService.loadCurrentStaff(staffId).subscribe();
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