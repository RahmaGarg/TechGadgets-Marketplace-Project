import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-complete-profile',
  templateUrl: './complete-profile.component.html',
  styleUrls: ['./complete-profile.component.css'],
  imports: [CommonModule, FormsModule, ReactiveFormsModule]
})
export class CompleteProfileComponent implements OnInit {
  profileForm: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private router: Router
    // Inject your user service here if you have one
    // private userService: UserService
  ) {
    this.profileForm = this.fb.group({
      country: ['', Validators.required],
      city: ['', Validators.required],
      address: ['', Validators.required],
      phone: ['', [Validators.required, Validators.pattern(/^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$/)]]
    });
  }

  ngOnInit(): void {
    // You can load existing user data here if needed
    this.loadUserData();
  }

  loadUserData(): void {
    // Load existing profile data if user already has partial profile
    // Example:
    // this.userService.getUserProfile().subscribe(
    //   (data) => {
    //     this.profileForm.patchValue({
    //       country: data.country,
    //       city: data.city,
    //       address: data.address,
    //       phone: data.phone
    //     });
    //   }
    // );
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.profileForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  onSubmit(): void {
    this.errorMessage = null;
    this.successMessage = null;

    if (this.profileForm.invalid) {
      this.errorMessage = 'Please fill in all required fields correctly';
      return;
    }

    this.isLoading = true;

    const profileData = {
      country: this.profileForm.get('country')?.value,
      city: this.profileForm.get('city')?.value,
      address: this.profileForm.get('address')?.value,
      phone: this.profileForm.get('phone')?.value
    };

    // Call your API service to save the profile
    // Example:
    // this.userService.updateUserProfile(profileData).subscribe(
    //   (response) => {
    //     this.isLoading = false;
    //     this.successMessage = 'Profile completed successfully!';
    //     setTimeout(() => {
    //       this.router.navigate(['/dashboard']); // or wherever you want to redirect
    //     }, 1500);
    //   },
    //   (error) => {
    //     this.isLoading = false;
    //     this.errorMessage = error.error?.message || 'An error occurred while saving your profile';
    //   }
    // );

    // For testing without a backend:
    setTimeout(() => {
      this.isLoading = false;
      this.successMessage = 'Profile completed successfully! Redirecting...';
      setTimeout(() => {
        this.router.navigate(['/dashboard']);
      }, 1500);
    }, 2000);
  }
}