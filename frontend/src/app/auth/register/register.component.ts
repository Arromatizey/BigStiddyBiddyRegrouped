import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { RegisterRequest } from '../../shared/models/auth.models';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      displayName: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.loading = true;
      this.error = null;

      const registerRequest: RegisterRequest = this.registerForm.value;

      this.authService.register(registerRequest).subscribe({
        next: (response) => {
          this.loading = false;
          // Auto-login after successful registration
          this.authService.login({
            email: registerRequest.email,
            password: registerRequest.password
          }).subscribe({
            next: () => {
              this.router.navigate(['/rooms']);
            },
            error: () => {
              this.router.navigate(['/login']);
            }
          });
        },
        error: (error) => {
          this.loading = false;
          this.error = 'Erreur lors de l\'inscription. Vérifiez vos informations.';
          console.error('Register error:', error);
        }
      });
    }
  }

  navigateToLogin(): void {
    this.router.navigate(['/auth/login']);
  }
}
