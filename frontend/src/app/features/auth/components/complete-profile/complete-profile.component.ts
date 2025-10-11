import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ProfileService, CompleteProfileRequest } from '../../services/profile.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-complete-profile',
  templateUrl: './complete-profile.component.html',
  styleUrls: ['./complete-profile.component.css'],
  imports: [CommonModule, ReactiveFormsModule]
})
export class CompleteProfileComponent implements OnInit {
  profileForm!: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private formBuilder: FormBuilder,
    private profileService: ProfileService,
    private authService: AuthService,
    private router: Router
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    // Vérifier que l'utilisateur est connecté
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    // Si le profil est déjà complété, rediriger
    if (this.authService.isProfileCompleted()) {
      this.redirectBasedOnRole();
    }
  }

  private initializeForm(): void {
    this.profileForm = this.formBuilder.group({
      country: ['', [Validators.required]],
      city: ['', [Validators.required, Validators.minLength(2)]],
      address: ['', [Validators.required, Validators.minLength(5)]],
      phone: ['', [
        Validators.required,
        Validators.pattern(/^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,4}[-\s.]?[0-9]{1,9}$/)
      ]]
    });
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.profileForm.valid) {
      this.errorMessage = 'Please fill in all required fields correctly';
      return;
    }

    this.isLoading = true;

    const formValue = this.profileForm.value;
    const request: CompleteProfileRequest = {
      country: formValue.country.trim(),
      city: formValue.city.trim(),
      address: formValue.address.trim(),
      phoneNumber: formValue.phone.trim()
    };

    this.profileService.completeProfile(request).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = 'Profile completed successfully!';
        console.log('Profile completed:', response);

        // Mettre à jour les données d'authentification
        const currentUser = this.authService.currentUserValue;
        if (currentUser) {
          currentUser.isProfileCompleted = true;
          localStorage.setItem('currentUser', JSON.stringify(currentUser));
        }

        // Rediriger après 1.5 secondes
        setTimeout(() => {
          this.redirectBasedOnRole();
        }, 1500);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Profile completion error:', error);

        if (error.status === 400) {
          this.errorMessage = error.error?.message || 'Invalid input data';
        } else if (error.status === 409) {
          this.errorMessage = 'Profile already completed';
        } else if (error.status === 401) {
          this.errorMessage = 'Your session has expired. Please login again.';
          setTimeout(() => this.router.navigate(['/login']), 2000);
        } else if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please try again later.';
        } else {
          this.errorMessage = error.error?.message || 'An error occurred. Please try again.';
        }
      }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.profileForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  private redirectBasedOnRole(): void {
    const role = this.authService.getUserRole();
    switch (role) {
      case 'ADMIN':
        this.router.navigate(['/admin/dashboard']);
        break;
      case 'SELLER':
        this.router.navigate(['/seller/dashboard']);
        break;
      case 'CLIENT':
        this.router.navigate(['/client/dashboard']);
        break;
      default:
        this.router.navigate(['/dashboard']);
        break;
    }
  }
}