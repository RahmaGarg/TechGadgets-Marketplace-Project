import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { Router } from '@angular/router';

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  role: string;
  name: string;
  isProfileCompleted: boolean;
  redirectTo: string;
}

export interface PasswordResetConfirm {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private currentUserSubject: BehaviorSubject<AuthResponse | null>;
  public currentUser: Observable<AuthResponse | null>;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    const storedUser = localStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<AuthResponse | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  public isLoggedIn(): boolean {
    return !!this.currentUserValue && !!this.getToken();
  }

  public getToken(): string | null {
    return localStorage.getItem('token');
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request)
      .pipe(
        tap(response => {
          this.saveAuthData(response);
        })
      );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => {
          this.saveAuthData(response);
        })
      );
  }

  forgotPassword(email: string): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/forgot-password?email=${email}`,
      {},
      { responseType: 'text' }
    );
  }

  validateResetToken(token: string): Observable<string> {
    return this.http.get(
      `${this.apiUrl}/reset-password/validate?token=${token}`,
      { responseType: 'text' }
    );
  }

  resetPassword(request: PasswordResetConfirm): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/reset-password`,
      request,
      { responseType: 'text' }
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  private saveAuthData(response: AuthResponse): void {
    localStorage.setItem('token', response.token);
    localStorage.setItem('currentUser', JSON.stringify(response));
    this.currentUserSubject.next(response);
  }

  public getUserRole(): string | null {
    const user = this.currentUserValue;
    return user ? user.role : null;
  }

  public hasRole(role: string): boolean {
    return this.getUserRole() === role;
  }

  public isProfileCompleted(): boolean {
    const user = this.currentUserValue;
    return user ? user.isProfileCompleted : false;
  }

  public getRedirectPath(): string {
    const user = this.currentUserValue;
    return user ? user.redirectTo : '/login';
  }
}