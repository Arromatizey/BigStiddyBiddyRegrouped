import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConversationsListComponent } from './conversations-list/conversations-list.component';
import { DmChatComponent } from './dm-chat/dm-chat.component';

const routes: Routes = [
  { path: '', component: ConversationsListComponent },
  { path: 'chat/:userId', component: DmChatComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DmRoutingModule { } 