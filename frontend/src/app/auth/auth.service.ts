import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { LoginRequest, RegisterRequest, AuthResponse, ResetPasswordRequest } from '../shared/models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = environment.apiUrl;
  private tokenSubject = new BehaviorSubject<string | null>(localStorage.getItem('token'));
  private userIdSubject = new BehaviorSubject<string | null>(localStorage.getItem('userId'));

  public token$ = this.tokenSubject.asObservable();
  public userId$ = this.userIdSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    console.log('🔧 AuthService constructor:');
    console.log('💾 Initial localStorage token:', localStorage.getItem('token'));
    console.log('💾 Initial localStorage userId:', localStorage.getItem('userId'));
    console.log('🔑 Initial tokenSubject value:', this.tokenSubject.value);
    console.log('🆔 Initial userIdSubject value:', this.userIdSubject.value);
  }

  register(request: RegisterRequest): Observable<string> {
    return this.http.post(`${this.baseUrl}/auth/register`, request, { responseType: 'text' });
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, request)
      .pipe(
        tap(response => {
          console.log('🔍 Full auth response:', response);
          console.log('🔑 Token:', response.token);
          console.log('🆔 User ID:', response.id);
          console.log('📋 Response type:', typeof response);
          console.log('📋 Response keys:', Object.keys(response));
          
          localStorage.setItem('token', response.token);
          localStorage.setItem('userId', response.id);
          this.tokenSubject.next(response.token);
          this.userIdSubject.next(response.id);
          
          console.log('💾 Stored token:', localStorage.getItem('token'));
          console.log('💾 Stored userId:', localStorage.getItem('userId'));
        })
      );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    this.tokenSubject.next(null);
    this.userIdSubject.next(null);
    this.router.navigate(['/']);
  }

  forgotPassword(email: string): Observable<string> {
    return this.http.post(`${this.baseUrl.replace('/api', '')}/auth/forgot-password`, { email }, { responseType: 'text' });
  }

  resetPassword(request: ResetPasswordRequest): Observable<string> {
    return this.http.post(`${this.baseUrl.replace('/api', '')}/auth/reset-password`, request, { responseType: 'text' });
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  getCurrentUserId(): string | null {
    return this.userIdSubject.value;
  }

  getToken(): string | null {
    return this.tokenSubject.value;
  }
}
