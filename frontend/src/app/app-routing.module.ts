import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { authGuard } from './auth/auth.guard';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { 
    path: 'auth', 
    loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule) 
  },
  { 
    path: 'rooms', 
    loadChildren: () => import('./rooms/rooms.module').then(m => m.RoomsModule),
    canActivate: [authGuard]
  },
  { 
    path: 'room/:id', 
    loadChildren: () => import('./room-detail/room-detail.module').then(m => m.RoomDetailModule),
    canActivate: [authGuard]
  },
  { 
    path: 'friends', 
    loadChildren: () => import('./friends/friends.module').then(m => m.FriendsModule),
    canActivate: [authGuard]
  },
  { path: 'login', redirectTo: '/auth/login' },
  { path: 'register', redirectTo: '/auth/register' },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { } 