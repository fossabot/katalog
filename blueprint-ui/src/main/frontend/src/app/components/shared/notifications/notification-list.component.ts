import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {Note, NotificationService} from './notification.service';

@Component({
  selector: 'app-notification-list',
  templateUrl: './notification-list.component.html'
})
export class NotificationListComponent implements OnInit {
  notifications: Note[];

  constructor(
    private notificationService: NotificationService,
    private changeDetectorRef: ChangeDetectorRef
  ) {
  }

  ngOnInit(): void {
    this.notificationService.notifications$.subscribe(n => {
      this.notifications = n;
      this.changeDetectorRef.detectChanges();
    });
  }

  remove(note: Note) {
    this.notificationService.remove(note);
  }
}
