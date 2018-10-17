import {Component} from '@angular/core';
import {Note, NotificationService} from './notification.service';

@Component({
  selector: 'app-notification-list',
  templateUrl: './notification-list.component.html'
})
export class NotificationListComponent {
  constructor(private notificationService: NotificationService) {
  }

  notifications() {
    return this.notificationService.notifications;
  }

  remove(note: Note) {
    this.notificationService.remove(note);
  }
}
