import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { DmRoutingModule } from './dm-routing.module';
import { ConversationsListComponent } from './conversations-list/conversations-list.component';
import { DmChatComponent } from './dm-chat/dm-chat.component';

@NgModule({
  declarations: [
    ConversationsListComponent,
    DmChatComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    DmRoutingModule
  ]
})
export class DmModule { } 