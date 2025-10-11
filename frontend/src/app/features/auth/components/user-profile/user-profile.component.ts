import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { ProfileResponse, ProfileService } from '../../services/profile.service';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css'],
  imports: [CommonModule, ReactiveFormsModule, FormsModule]
})
export class UserProfileComponent implements OnInit, OnDestroy {
  activeTab: 'profile' | 'password' = 'profile';

  profileForm!: FormGroup;
  passwordForm!: FormGroup;

  userProfileData: ProfileResponse | null = null;
  isLoading = false;
  loadingError = '';

  isEditingProfile = false;
  isEditingPassword = false;
  isSaving = false;
  isChangingPassword = false;
  showSuccessMessage = false;
  successMessage = '';

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private profileService: ProfileService
  ) {
    this.initializeForms();
  }

  initializeForms(): void {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required]],
      address: ['', [Validators.required]],
      city: ['', [Validators.required, Validators.minLength(2)]],
      country: ['', [Validators.required, Validators.minLength(2)]]
    });

    this.passwordForm = this.fb.group(
      {
        currentPassword: ['', [Validators.required]],
        newPassword: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', [Validators.required]]
      },
      { validators: this.passwordMatchValidator }
    );
  }

  ngOnInit(): void {
    this.loadUserProfile();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadUserProfile(): void {
    this.isLoading = true;
    this.loadingError = '';

    this.profileService.getProfile()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (profile) => {
          this.userProfileData = profile;
          this.initializeProfileForm();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading profile:', error);
          this.loadingError = 'Failed to load profile data. Please try again.';
          this.isLoading = false;
        }
      });
  }

  initializeProfileForm(): void {
    if (this.userProfileData) {
      this.profileForm.patchValue({
        name: this.userProfileData.name,
        email: this.userProfileData.email,
        phoneNumber: this.userProfileData.phoneNumber,
        address: this.userProfileData.address,
        city: this.userProfileData.city,
        country: this.userProfileData.country
      });
    }
  }

  setActiveTab(tab: 'profile' | 'password'): void {
    this.activeTab = tab;
  }

  toggleEditProfile(): void {
    this.isEditingProfile = !this.isEditingProfile;
    if (!this.isEditingProfile) {
      this.initializeProfileForm();
    }
  }

  toggleEditPassword(): void {
    this.isEditingPassword = !this.isEditingPassword;
    if (!this.isEditingPassword) {
      this.passwordForm.reset();
    }
  }

  saveProfile(): void {
    if (this.profileForm.valid && this.userProfileData) {
      this.isSaving = true;

      const updateRequest = {
        phoneNumber: this.profileForm.value.phoneNumber,
        address: this.profileForm.value.address,
        city: this.profileForm.value.city,
        country: this.profileForm.value.country
      };

      this.profileService.updateProfile(updateRequest)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (updatedProfile) => {
            this.userProfileData = updatedProfile;
            this.isSaving = false;
            this.isEditingProfile = false;
            this.showSuccessMsg('Profile updated successfully!');
          },
          error: (error) => {
            console.error('Error updating profile:', error);
            this.isSaving = false;
            this.showErrorMsg('Failed to update profile. Please try again.');
          }
        });
    }
  }

  changePassword(): void {
    if (this.passwordForm.valid) {
      this.isChangingPassword = true;

      const changePasswordRequest = {
        oldPassword: this.passwordForm.value.currentPassword,
        newPassword: this.passwordForm.value.newPassword,
        confirmPassword: this.passwordForm.value.confirmPassword
      };

      this.profileService.changePassword(changePasswordRequest)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.isChangingPassword = false;
            this.isEditingPassword = false;
            this.passwordForm.reset();
            this.showSuccessMsg('Password changed successfully!');
          },
          error: (error) => {
            console.error('Error changing password:', error);
            this.isChangingPassword = false;
            this.showErrorMsg('Failed to change password. Please try again.');
          }
        });
    }
  }

  private passwordMatchValidator(group: FormGroup): { [key: string]: any } | null {
    const password = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  private showSuccessMsg(message: string): void {
    this.successMessage = message;
    this.showSuccessMessage = true;
    setTimeout(() => {
      this.showSuccessMessage = false;
    }, 3000);
  }

  private showErrorMsg(message: string): void {
    this.successMessage = message;
    this.showSuccessMessage = true;
    setTimeout(() => {
      this.showSuccessMessage = false;
    }, 3000);
  }

  cancelEdit(): void {
    this.isEditingProfile = false;
    this.isEditingPassword = false;
    this.initializeProfileForm();
    this.passwordForm.reset();
  }
}