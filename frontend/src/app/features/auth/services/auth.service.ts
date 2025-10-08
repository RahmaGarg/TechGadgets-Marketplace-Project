import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { Router } from '@angular/router';

// Interfaces pour les données
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
    // Récupérer l'utilisateur du localStorage au démarrage
    const storedUser = localStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<AuthResponse | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  // Getter pour la valeur actuelle de l'utilisateur
  public get currentUserValue(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  // Vérifier si l'utilisateur est connecté
  public isLoggedIn(): boolean {
    return !!this.currentUserValue && !!this.getToken();
  }

  // Récupérer le token
  public getToken(): string | null {
    return localStorage.getItem('token');
  }

  // Register - Inscription
  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request)
      .pipe(
        tap(response => {
          // Sauvegarder le token et l'utilisateur
          this.saveAuthData(response);
        })
      );
  }

  // Login - Connexion
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => {
          // Sauvegarder le token et l'utilisateur
          this.saveAuthData(response);
        })
      );
  }

  // Forgot Password - Demande de réinitialisation
  forgotPassword(email: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/forgot-password?email=${email}`, {}, {
      responseType: 'text'
    });
  }

  // Validate Reset Token - Valider le token de réinitialisation
  validateResetToken(token: string): Observable<string> {
    return this.http.get(`${this.apiUrl}/reset-password/validate?token=${token}`, {
      responseType: 'text'
    });
  }

  // Reset Password - Réinitialiser le mot de passe
  resetPassword(request: PasswordResetConfirm): Observable<string> {
    return this.http.post(`${this.apiUrl}/reset-password`, request, {
      responseType: 'text'
    });
  }

  // Logout - Déconnexion
  logout(): void {
    // Supprimer les données du localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    
    // Mettre à jour le BehaviorSubject
    this.currentUserSubject.next(null);
    
    // Rediriger vers la page de login
    this.router.navigate(['/login']);
  }

  // Sauvegarder les données d'authentification
  private saveAuthData(response: AuthResponse): void {
    localStorage.setItem('token', response.token);
    localStorage.setItem('currentUser', JSON.stringify(response));
    this.currentUserSubject.next(response);
  }

  // Récupérer le rôle de l'utilisateur
  public getUserRole(): string | null {
    const user = this.currentUserValue;
    return user ? user.role : null;
  }
  // Vérifier si l'utilisateur a un rôle spécifique
  public hasRole(role: string): boolean {
    return this.getUserRole() === role;
  }
}