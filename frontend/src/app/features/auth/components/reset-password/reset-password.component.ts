import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService, PasswordResetConfirm } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css'],
  imports: [CommonModule, FormsModule]
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
  
  // Add these missing properties for password visibility toggle
  showPassword: boolean = false;
  showConfirmPassword: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Get token from URL query parameters
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      
      if (!this.token) {
        this.errorMessage = 'Invalid or missing reset token';
        this.isValidatingToken = false;
        return;
      }

      // Validate the token
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

  // Changed method name from onSubmit to onResetPassword to match HTML
  onResetPassword(): void {
    // Reset messages
    this.errorMessage = '';
    this.successMessage = '';

    // Validate fields
    if (!this.newPassword || !this.confirmPassword) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    // Validate password length
    if (this.newPassword.length < 8) {
      this.errorMessage = 'Password must be at least 8 characters long';
      return;
    }

    // Validate password requirements (aligned with backend)
    if (!this.isPasswordValid()) {
      this.errorMessage = 'Password must contain at least: 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character (@$!%*?&#)';
      return;
    }

    // Check if passwords match
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
        
        // Redirect to login after 2 seconds
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Password reset error:', error);
        
        // Extract error message from response
        let errorMsg = 'An error occurred. Please try again.';
        
        if (error.error) {
          // If error.error is a string, use it directly
          if (typeof error.error === 'string') {
            errorMsg = error.error;
          } 
          // If error.error is an object with a message property
          else if (error.error.message) {
            errorMsg = error.error.message;
          }
          // If error.error is an object with an error property
          else if (error.error.error) {
            errorMsg = error.error.error;
          }
        }
        
        // Handle different error types with extracted message
        if (error.status === 400) {
          this.errorMessage = errorMsg;
        } else if (error.status === 403) {
          this.errorMessage = errorMsg;
        } else if (error.status === 404) {
          this.errorMessage = 'Reset token not found or has expired';
        } else if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please try again later.';
        } else {
          this.errorMessage = errorMsg;
        }
      }
    });
  }

  // Add password visibility toggle methods
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  // Validate password requirements (aligned with backend)
  isPasswordValid(): boolean {
    const hasUpperCase = /[A-Z]/.test(this.newPassword);
    const hasLowerCase = /[a-z]/.test(this.newPassword);
    const hasDigit = /\d/.test(this.newPassword);
    const hasSpecialChar = /[@$!%*?&#]/.test(this.newPassword);
    return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
  }

  // Method to check password strength
  getPasswordStrength(): string {
    if (!this.newPassword) return '';
    
    if (this.newPassword.length < 6) return 'weak';
    if (this.newPassword.length < 10) return 'medium';
    
    const hasUpperCase = /[A-Z]/.test(this.newPassword);
    const hasLowerCase = /[a-z]/.test(this.newPassword);
    const hasDigit = /\d/.test(this.newPassword);
    const hasSpecialChar = /[@$!%*?&#]/.test(this.newPassword);
    
    const strength = [hasUpperCase, hasLowerCase, hasDigit, hasSpecialChar]
      .filter(Boolean).length;
    
    if (strength >= 4) return 'strong';
    if (strength >= 3) return 'medium';
    return 'weak';
  }

  // Check if passwords match
  passwordsMatch(): boolean {
    return this.newPassword === this.confirmPassword && this.confirmPassword.length > 0;
  }

  // Helper methods for password requirements validation (aligned with backend)
  hasMinLength(): boolean {
    return this.newPassword.length >= 8;
  }

  hasUpperCase(): boolean {
    return /[A-Z]/.test(this.newPassword);
  }

  hasLowerCase(): boolean {
    return /[a-z]/.test(this.newPassword);
  }

  hasDigit(): boolean {
    return /\d/.test(this.newPassword);
  }

  hasSpecialChar(): boolean {
    return /[@$!%*?&#]/.test(this.newPassword);
  }
}