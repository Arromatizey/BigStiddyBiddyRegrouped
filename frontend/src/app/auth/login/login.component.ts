import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { LoginRequest } from '../../shared/models/auth.models';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.loading = true;
      this.error = null;

      const loginRequest: LoginRequest = this.loginForm.value;

      this.authService.login(loginRequest).subscribe({
        next: (response) => {
          this.loading = false;
          this.router.navigate(['/rooms']);
        },
        error: (error) => {
          this.loading = false;
          this.error = 'Invalid email or password';
          console.error('Login error:', error);
        }
      });
    }
  }

  navigateToRegister(): void {
    this.router.navigate(['/auth/register']);
  }

  // Méthode de test temporaire pour déboguer l'API
  testAuthAPI(): void {
    console.log('🧪 Testing auth API directly...');
    
    const testCredentials = {
      email: this.loginForm.value.email || 'test@example.com',
      password: this.loginForm.value.password || 'password123'
    };
    
    console.log('📤 Sending test request with:', testCredentials);
    
    fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(testCredentials)
    })
    .then(response => {
      console.log('📥 Response status:', response.status);
      console.log('📥 Response headers:', response.headers);
      return response.json();
    })
    .then(data => {
      console.log('🔍 Raw API response:', data);
      console.log('🔑 Token in response:', data.token);
      console.log('🆔 ID in response:', data.id);
      console.log('📋 Response keys:', Object.keys(data));
    })
    .catch(error => {
      console.error('❌ Test API error:', error);
    });
  }
}
