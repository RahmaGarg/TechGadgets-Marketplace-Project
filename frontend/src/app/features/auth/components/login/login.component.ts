import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService, LoginRequest } from '../../services/auth.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  imports: [FormsModule, CommonModule]
})
export class LoginComponent implements OnInit {
  email: string = '';
  password: string = '';
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Si l'utilisateur est déjà connecté, rediriger selon son rôle
    if (this.authService.isLoggedIn()) {
      this.redirectBasedOnRole();
    }
  }

  onLogin(): void {
    // Réinitialiser le message d'erreur
    this.errorMessage = '';

    // Validation basique
    if (!this.email || !this.password) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    // Validation de l'email
    if (!this.isValidEmail(this.email)) {
      this.errorMessage = 'Please enter a valid email address';
      return;
    }

    this.isLoading = true;

    const loginRequest: LoginRequest = {
      email: this.email.trim(),
      password: this.password
    };

    this.authService.login(loginRequest).subscribe({
      next: (response) => {
        this.isLoading = false;
        console.log('Login successful:', response);
        
        // Rediriger selon le rôle de l'utilisateur
        this.redirectBasedOnRole();
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Login error:', error);
        
        // Gérer les différents types d'erreurs
        if (error.status === 401) {
          this.errorMessage = 'Invalid email or password';
        } else if (error.status === 403) {
          this.errorMessage = 'Account is not active or verified';
        } else if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please try again later.';
        } else {
          this.errorMessage = error.error?.message || 'An error occurred during login. Please try again.';
        }
      }
    });
  }

  private redirectBasedOnRole(): void {
    const role = this.authService.getUserRole();
  switch (role) {
    case 'ADMIN':
      this.router.navigate(['/admin/home']);
      break;
    case 'FREELANCER':
      this.router.navigate(['/freelancer/home']);
      break;
    case 'CLIENT':
      this.router.navigate(['/client/home']);
      break;
    default:
      this.router.navigate(['/login']); // cas où le rôle n'est pas reconnu
      break;
  }
  }

  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }
}