import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RoomDetailComponent } from './room-detail/room-detail.component';

const routes: Routes = [
  { path: '', component: RoomDetailComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RoomDetailRoutingModule { }
