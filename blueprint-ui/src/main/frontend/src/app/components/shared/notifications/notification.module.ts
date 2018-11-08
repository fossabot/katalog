import {NgModule} from '@angular/core';
import {NotificationService} from './notification.service';
import {NotificationComponent} from './notification.component';
import {NotificationListComponent} from './notification-list.component';
import {CommonModule} from '@angular/common';

@NgModule({
  imports: [
    CommonModule
  ],
  providers: [
    NotificationService
  ],
  declarations: [
    NotificationComponent,
    NotificationListComponent
  ],
  exports: [
    NotificationListComponent
  ]
})
export class NotificationModule {
}
