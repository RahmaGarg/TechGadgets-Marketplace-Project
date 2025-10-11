import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css'],
  imports: [CommonModule, ReactiveFormsModule, FormsModule]
})
export class UserProfileComponent implements OnInit {
  activeTab: 'profile' | 'password' = 'profile';

  profileForm!: FormGroup;
  passwordForm!: FormGroup;

  userProfileData = {
    fullName: 'John Doe',
    email: 'johndoe@mail.com',
    phoneNumber: '+1 (555) 123-4567',
    address: '123 Tech Street, San Francisco, CA 94102',
    role: 'Project Manager'
  };

  isEditingProfile = false;
  isEditingPassword = false;
  isSaving = false;
  isChangingPassword = false;
  showSuccessMessage = false;
  successMessage = '';

  constructor(private fb: FormBuilder) {
    this.initializeForms();
  }

  initializeForms(): void {
    this.profileForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required]],
      address: ['', [Validators.required]],
      role: ['', [Validators.required, Validators.minLength(2)]]
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
    this.initializeProfileForm();
  }

  initializeProfileForm(): void {
    this.profileForm.patchValue({
      fullName: this.userProfileData.fullName,
      email: this.userProfileData.email,
      phoneNumber: this.userProfileData.phoneNumber,
      address: this.userProfileData.address,
      role: this.userProfileData.role
    });
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
    if (this.profileForm.valid) {
      this.isSaving = true;
      setTimeout(() => {
        this.userProfileData = {
          ...this.userProfileData,
          ...this.profileForm.value
        };
        this.isSaving = false;
        this.isEditingProfile = false;
        this.showSuccessMsg('Profile updated successfully!');
      }, 1500);
    }
  }

  changePassword(): void {
    if (this.passwordForm.valid) {
      this.isChangingPassword = true;
      setTimeout(() => {
        this.isChangingPassword = false;
        this.isEditingPassword = false;
        this.passwordForm.reset();
        this.showSuccessMsg('Password changed successfully!');
      }, 1500);
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

  cancelEdit(): void {
    this.isEditingProfile = false;
    this.isEditingPassword = false;
    this.initializeProfileForm();
    this.passwordForm.reset();
  }
}