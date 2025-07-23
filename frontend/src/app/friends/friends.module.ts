import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { FriendsRoutingModule } from './friends-routing.module';
import { FriendsListComponent } from './friends-list/friends-list.component';
import { FriendRequestsComponent } from './friend-requests/friend-requests.component';
import { AddFriendComponent } from './add-friend/add-friend.component';
import { FriendsPageComponent } from './friends-page/friends-page.component';

@NgModule({
  declarations: [
    FriendsListComponent,
    FriendRequestsComponent,
    AddFriendComponent,
    FriendsPageComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    FriendsRoutingModule
  ]
})
export class FriendsModule { } 