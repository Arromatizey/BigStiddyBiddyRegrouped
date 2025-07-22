import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Headers de base pour toutes les requêtes
  const headers: { [key: string]: string } = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'Access-Control-Allow-Origin': 'http://localhost:4200',
    'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization, Accept, Origin, X-Requested-With',
    'Access-Control-Allow-Credentials': 'true'
  };

  // Ajouter le token d'authentification si disponible
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // Cloner la requête avec tous les headers nécessaires
  const clonedRequest = req.clone({
    setHeaders: headers
  });

  console.log('🔧 Interceptor - Request URL:', req.url);
  console.log('🔧 Interceptor - Method:', req.method);
  console.log('🔧 Interceptor - Headers:', headers);
  console.log('🔧 Interceptor - Token present:', !!token);

  return next(clonedRequest);
};
