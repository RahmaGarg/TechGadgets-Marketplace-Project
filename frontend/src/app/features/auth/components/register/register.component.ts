import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService, RegisterRequest } from '../../services/auth.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
  imports: [FormsModule, CommonModule]
})
export class RegisterComponent implements OnInit {
  fullName: string = '';
  email: string = '';
  password: string = '';
  role: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Si l'utilisateur est déjà connecté, rediriger vers le dashboard
    if (this.authService.isLoggedIn()) {
      this.redirectBasedOnRole();
    }
  }

  onRegister(): void {
    // Réinitialiser les messages
    this.errorMessage = '';
    this.successMessage = '';

    // Validation des champs
    if (!this.fullName || !this.email || !this.password || !this.role) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    // Validation du nom complet (au moins 2 mots)
    if (this.fullName.trim().split(' ').length < 2) {
      this.errorMessage = 'Please enter your first name and last name';
      return;
    }

    // Validation de l'email
    if (!this.isValidEmail(this.email)) {
      this.errorMessage = 'Please enter a valid email address';
      return;
    }

    // Validation du mot de passe (minimum 6 caractères)
    if (this.password.length < 8) {
      this.errorMessage = 'Password must be at least 8 characters long';
      return;
    }

    this.isLoading = true;

    const registerRequest: RegisterRequest = {
      name: this.fullName.trim(),
      email: this.email.trim().toLowerCase(),
      password: this.password,
      role: this.role.toUpperCase() // Le backend attend généralement le rôle en majuscules
    };

    this.authService.register(registerRequest).subscribe({
      next: (response) => {
        this.isLoading = false;
        console.log('Registration successful:', response);
        
        this.successMessage = 'Account created successfully! Redirecting...';
        
        // Attendre 1.5 secondes avant de rediriger pour montrer le message de succès
        setTimeout(() => {
          this.redirectBasedOnRole();
        }, 1500);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Registration error:', error);
        
        // Gérer les différents types d'erreurs
        if (error.status === 409 || error.status === 400) {
          // Email déjà utilisé
          this.errorMessage = error.error?.message || 'Email already exists. Please use a different email.';
        } else if (error.status === 422) {
          // Données invalides
          this.errorMessage = 'Invalid data. Please check your information.';
        } else if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please try again later.';
        } else {
          this.errorMessage = error.error?.message || 'An error occurred during registration. Please try again.';
        }
      }
    });
  }

  private redirectBasedOnRole(): void {
    const role = this.authService.getUserRole();
    console.log(role)
  }

  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  // Méthode pour vérifier la force du mot de passe (optionnel)
  getPasswordStrength(): string {
    if (!this.password) return '';
    
    if (this.password.length < 6) return 'weak';
    if (this.password.length < 10) return 'medium';
    
    const hasUpperCase = /[A-Z]/.test(this.password);
    const hasLowerCase = /[a-z]/.test(this.password);
    const hasNumbers = /\d/.test(this.password);
    const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(this.password);
    
    const strength = [hasUpperCase, hasLowerCase, hasNumbers, hasSpecialChar]
      .filter(Boolean).length;
    
    if (strength >= 3) return 'strong';
    return 'medium';
  }
}