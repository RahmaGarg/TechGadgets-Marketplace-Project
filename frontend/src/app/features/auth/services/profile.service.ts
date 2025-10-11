import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CompleteProfileRequest {
  phoneNumber?: string;
  phone?: string;
  address: string;
  city: string;
  country: string;
}

export interface ProfileResponse {
  id: number;
  name: string;
  email: string;
  phoneNumber: string;
  address: string;
  city: string;
  country: string;
  isProfileCompleted: boolean;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private apiUrl = 'http://localhost:8080/api/profile';

  constructor(private http: HttpClient) {}

  completeProfile(request: CompleteProfileRequest): Observable<ProfileResponse> {
    return this.http.post<ProfileResponse>(`${this.apiUrl}/complete`, request);
  }

  getProfile(): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(this.apiUrl);
  }

  updateProfile(request: CompleteProfileRequest): Observable<ProfileResponse> {
    return this.http.put<ProfileResponse>(this.apiUrl, request);
  }

  changePassword(request: ChangePasswordRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/change-password`, request);
  }
}