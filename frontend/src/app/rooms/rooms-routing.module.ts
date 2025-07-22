import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RoomsListComponent } from './rooms-list/rooms-list.component';

const routes: Routes = [
  { path: '', component: RoomsListComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RoomsRoutingModule { }
