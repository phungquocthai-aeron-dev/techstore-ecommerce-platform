import { Injectable } from '@angular/core';
import { jwtDecode } from 'jwt-decode';
import { BehaviorSubject } from 'rxjs';

const TOKEN_KEY = 'techstore_access_token';

@Injectable({
  providedIn: 'root'
})
export class TokenService {

  private loggedIn$ = new BehaviorSubject<boolean>(this.hasToken());

  constructor() {}

  // ─── Token ─────────────────────────────────────────

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
    this.loggedIn$.next(true);
  }

  removeToken(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.loggedIn$.next(false);
  }

  private hasToken(): boolean {
    return !!localStorage.getItem(TOKEN_KEY);
  }

  // ─── Reactive ─────────────────────────────────────

  getLoggedIn() {
    return this.loggedIn$.asObservable();
  }

  isLoggedIn(): boolean {
    return this.loggedIn$.value;
  }

  // ─── User info ────────────────────────────────────

  getUserId(): string {
    const token = this.getToken();
    if (!token) return '';
    const decoded: any = jwtDecode(token);
    return decoded.sub;
  }
}