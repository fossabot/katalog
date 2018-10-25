import { ErrorHandler, Injectable, isDevMode } from "@angular/core";
import { NotificationService } from "./notifications/notification.service";
import { HttpErrorResponse } from "@angular/common/http";

@Injectable()
export class BlueprintErrorHandler implements ErrorHandler {
  constructor(
    private notifications: NotificationService
  ) {
  }

  handleError(error: any) {
    // Is this an ApiResponse which is already handled?
    if (error.rejection.handled) {
      return;
    }

    if (isDevMode()) {
      console.dir(error);
    }

    if (error instanceof HttpErrorResponse) {
      if (!navigator.onLine) {
        return this.notifications.noInternetConnection();
      } else {
        switch (error.status) {
          default:
            return this.notifications.cannotContactServer();
        }
      }
    }

    this.notifications.cannotContactServer();
  }
}
