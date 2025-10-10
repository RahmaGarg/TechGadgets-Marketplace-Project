import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css'],
  imports: [CommonModule, FormsModule]
})
export class ForgotPasswordComponent  {
  email: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;
  emailSent: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}



  onSubmit(): void {
    // Réinitialiser les messages
    this.errorMessage = '';
    this.successMessage = '';

    // Validation de l'email
    if (!this.email) {
      this.errorMessage = 'Please enter your email address';
      return;
    }

    if (!this.isValidEmail(this.email)) {
      this.errorMessage = 'Please enter a valid email address';
      return;
    }

    this.isLoading = true;

    this.authService.forgotPassword(this.email.trim().toLowerCase()).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.emailSent = true;
        console.log('Password reset email sent:', response);
        
        this.successMessage = 'Password reset link has been sent to your email. Please check your inbox and spam folder.';
        
        // Optionnel : Rediriger vers login après 5 secondes
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 5000);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Forgot password error:', error);
        
        // Gérer les différents types d'erreurs
        if (error.status === 404) {
          this.errorMessage = 'No account found with this email address';
        } else if (error.status === 400) {
          this.errorMessage = 'Invalid email address';
        } else if (error.status === 429) {
          this.errorMessage = 'Too many requests. Please try again later';
        } else if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please try again later.';
        } else {
          this.errorMessage = error.error || 'An error occurred. Please try again.';
        }
      }
    });
  }

  private redirectBasedOnRole(): void {
    const role = this.authService.getUserRole();
    
    switch (role?.toUpperCase()) {
      case 'SELLER':
        this.router.navigate(['/seller/dashboard']);
        break;
      case 'CLIENT':
        this.router.navigate(['/client/dashboard']);
        break;
      case 'ADMIN':
        this.router.navigate(['/admin/dashboard']);
        break;
      default:
        this.router.navigate(['/']);
        break;
    }
  }

  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  // Méthode pour renvoyer l'email (si nécessaire)
  resendEmail(): void {
    this.emailSent = false;
    this.successMessage = '';
    this.onSubmit();
  }
}