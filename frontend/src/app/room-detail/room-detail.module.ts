import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { RoomDetailRoutingModule } from './room-detail-routing.module';
import { RoomDetailComponent } from './room-detail/room-detail.component';
import { ChatComponent } from './chat/chat.component';
import { PomodoroTimerComponent } from './pomodoro-timer/pomodoro-timer.component';


@NgModule({
  declarations: [
    RoomDetailComponent,
    ChatComponent,
    PomodoroTimerComponent
  ],
  imports: [
    CommonModule,
    RoomDetailRoutingModule
  ]
})
export class RoomDetailModule { }
