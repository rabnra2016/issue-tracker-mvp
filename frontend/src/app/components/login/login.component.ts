import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  isLoginMode = true;
  email = '';
  password = '';
  name = '';
  error = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  toggleMode(): void {
    this.isLoginMode = !this.isLoginMode;
    this.error = '';
  }

  onSubmit(): void {
    this.error = '';
    this.loading = true;

    if (this.isLoginMode) {
      this.authService.login({ email: this.email, password: this.password })
        .subscribe({
          next: () => {
            this.router.navigate(['/projects']);
          },
          error: (err) => {
            this.error = err.error?.message || 'Login failed';
            this.loading = false;
          }
        });
    } else {
      this.authService.signup({ email: this.email, password: this.password, name: this.name })
        .subscribe({
          next: () => {
            this.router.navigate(['/projects']);
          },
          error: (err) => {
            this.error = err.error?.message || 'Signup failed';
            this.loading = false;
          }
        });
    }
  }
}
