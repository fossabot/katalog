import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Note} from './notification.service';

@Component({
  selector: 'app-notification',
  templateUrl: './notification.component.html'
})
export class NotificationComponent {
  @Input() public note: Note;
  @Output() public deleted: EventEmitter<any> = new EventEmitter();

  get hasDelete() {
    return !(this.note.timeoutMs > 0);
  }

  get noteClass() {
    switch (this.note.style) {
      case 'info':
        return 'is-info';
      case 'success':
        return 'is-success';
      case 'warn':
        return 'is-warning';
      case 'error':
        return 'is-danger';
    }
  }

  remove() {
    this.deleted.emit();
  }
}
