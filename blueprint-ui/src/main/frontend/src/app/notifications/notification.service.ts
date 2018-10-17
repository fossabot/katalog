import {Injectable} from '@angular/core';

export interface Note {
  title: string;
  description: string;
  style?: 'info' | 'success' | 'warn' | 'error';
  timeoutMs?: number;
}

export const CANNOT_CONTACT_SERVER_ERROR: Note = {
  'title': 'Error',
  description: 'There was a problem contacting the server. Please try again or notify your administrator if the issue persists.',
  style: 'error'
};

@Injectable()
export class NotificationService {
  notifications: Note[] = [];

  constructor() {
  }

  push(note: Note) {
    this.notifications.push(note);
    if (note.timeoutMs) {
      window.setTimeout(() => {
        this.remove(note);
      }, note.timeoutMs);
    }
  }

  remove(note: Note) {
    const index = this.notifications.indexOf(note);
    if (index !== -1) {
      this.notifications.splice(index, 1);
    }
  }
}
