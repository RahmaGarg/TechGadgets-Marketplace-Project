import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService, PasswordResetConfirm } from '../../services/auth.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  token: string = '';
  newPassword: string = '';
  confirmPassword: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;
  isValidatingToken: boolean = true;
  isTokenValid: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Récupérer le token depuis l'URL
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      
      if (!this.token) {
        this.errorMessage = 'Invalid or missing reset token';
        this.isValidatingToken = false;
        return;
      }

      // Valider le token
      this.validateToken();
    });
  }

  validateToken(): void {
    this.isValidatingToken = true;
    
    this.authService.validateResetToken(this.token).subscribe({
      next: (response) => {
        this.isValidatingToken = false;
        this.isTokenValid = true;
        console.log('Token is valid:', response);
      },
      error: (error) => {
        this.isValidatingToken = false;
        this.isTokenValid = false;
        console.error('Token validation error:', error);
        
        if (error.status === 400 || error.status === 404) {
          this.errorMessage = 'Invalid or expired reset token. Please request a new password reset.';
        } else {
          this.errorMessage = 'Unable to validate reset token. Please try again.';
        }
      }
    });
  }

  onSubmit(): void {
    // Réinitialiser les messages
    this.errorMessage = '';
    this.successMessage = '';

    // Validation des champs
    if (!this.newPassword || !this.confirmPassword) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    // Validation de la longueur du mot de passe
    if (this.newPassword.length < 8) {
      this.errorMessage = 'Password must be at least 8 characters long';
      return;
    }

    // Vérifier que les mots de passe correspondent
    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return;
    }

    this.isLoading = true;

    const resetRequest: PasswordResetConfirm = {
      token: this.token,
      newPassword: this.newPassword,
      confirmPassword: this.confirmPassword
    };

    this.authService.resetPassword(resetRequest).subscribe({
      next: (response) => {
        this.isLoading = false;
        console.log('Password reset successful:', response);
        
        this.successMessage = 'Password reset successfully! Redirecting to login...';
        
        // Rediriger vers login après 2 secondes
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Password reset error:', error);
        
        // Gérer les différents types d'erreurs
        if (error.status === 400) {
          this.errorMessage = error.error || 'Invalid token or passwords do not match';
        } else if (error.status === 404) {
          this.errorMessage = 'Reset token not found or has expired';
        } else if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please try again later.';
        } else {
          this.errorMessage = error.error || 'An error occurred. Please try again.';
        }
      }
    });
  }

  // Méthode pour vérifier la force du mot de passe
  getPasswordStrength(): string {
    if (!this.newPassword) return '';
    
    if (this.newPassword.length < 6) return 'weak';
    if (this.newPassword.length < 10) return 'medium';
    
    const hasUpperCase = /[A-Z]/.test(this.newPassword);
    const hasLowerCase = /[a-z]/.test(this.newPassword);
    const hasNumbers = /\d/.test(this.newPassword);
    const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(this.newPassword);
    
    const strength = [hasUpperCase, hasLowerCase, hasNumbers, hasSpecialChar]
      .filter(Boolean).length;
    
    if (strength >= 3) return 'strong';
    return 'medium';
  }

  // Vérifier si les mots de passe correspondent
  passwordsMatch(): boolean {
    return this.newPassword === this.confirmPassword && this.confirmPassword.length > 0;
  }
}