import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

import { RoomsRoutingModule } from './rooms-routing.module';
import { RoomsListComponent } from './rooms-list/rooms-list.component';
import { CreateRoomDialogComponent } from './create-room-dialog/create-room-dialog.component';


@NgModule({
  declarations: [
    RoomsListComponent,
    CreateRoomDialogComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RoomsRoutingModule
  ]
})
export class RoomsModule { }
