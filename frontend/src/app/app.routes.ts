import { Routes } from '@angular/router';
import { AuthLayoutComponent } from './layout/auth-layout/auth-layout/auth-layout.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout/main-layout.component';
import { ResetPasswordComponent } from './features/auth/components/reset-password/reset-password.component';
import { AdminHomeComponent } from './features/admin/components/admin-home/admin-home.component';
import { ClientHomeComponent } from './features/client/components/client-home/client-home.component';
import { FreelancerHomeComponent } from './features/freelancer/components/freelancer-home/freelancer-home.component';

export const routes: Routes = [
  // ==========================================
  // ROUTES PUBLIQUES (Auth Layout)
  // ==========================================
  {
    path: '',
    component: AuthLayoutComponent,
    children: [
      {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full'
      },
      {
        path: 'login',
        loadComponent: () => 
          import('./features/auth/components/login/login.component')
            .then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => 
          import('./features/auth/components/register/register.component')
            .then(m => m.RegisterComponent)
      },
      {
        path: 'forgot-password',
        loadComponent: () => 
          import('./features/auth/components/forgot-password/forgot-password.component')
            .then(m => m.ForgotPasswordComponent)
      }
    ]
  },
    { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'admin/home', component: AdminHomeComponent},
  { path: 'client/home', component: ClientHomeComponent},
  { path: 'freelancer/home', component: FreelancerHomeComponent },


  // ==========================================
  // ROUTES PROTÉGÉES (Main Layout)
  // ==========================================
  
  // ==========================================
  // ROUTES ADMIN
  // ==========================================
  {
    path: 'admin',
    component: MainLayoutComponent,
    data: { role: 'admin' },
    children: [
      {
        path: '',
        redirectTo: 'users',
        pathMatch: 'full'
      }
     
    ]
  },

  // ==========================================
  // ROUTES FREELANCER
  // ==========================================


  // ==========================================
  // PAGE 404
  // ==========================================
  {
    path: '**',
    redirectTo: 'login'
  }
];