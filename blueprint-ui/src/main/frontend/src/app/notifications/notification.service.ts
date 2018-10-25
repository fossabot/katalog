import { Injectable } from '@angular/core';
import { Subject } from "rxjs";

export interface Note {
  title: string;
  description: string;
  style?: 'info' | 'success' | 'warn' | 'error';
  timeoutMs?: number;
}

@Injectable()
export class NotificationService {
  private notifications: Note[] = [];
  notifications$ = new Subject<Note[]>();

  constructor() {
  }

  push(note: Note) {
    const index = this.notifications.indexOf(note);
    if (index !== -1) {
      // We don't want duplicate notifications
      return;
    }

    this.notifications.push(note);
    this.notifications$.next(this.notifications);
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
      this.notifications$.next(this.notifications);
    }
  }

  cannotContactServer() {
    this.push({
      title: 'Error',
      description: 'There was a problem contacting the server. Please try again or notify your administrator if the issue persists.',
      style: 'error'
    });
  }

  noInternetConnection() {
    this.push({
      title: 'Offline',
      description: 'You are offline. Please try again once your internet connectivity is restored.',
      style: 'error'
    });
  }
}
