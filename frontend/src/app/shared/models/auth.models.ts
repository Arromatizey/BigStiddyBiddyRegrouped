export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  displayName?: string;
}

export interface AuthResponse {
  token: string;
  id: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
} 