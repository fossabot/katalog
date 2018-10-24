import { ErrorHandler, Injectable, isDevMode } from "@angular/core";
import { NotificationService } from "./notifications/notification.service";

@Injectable()
export class BlueprintErrorHandler implements ErrorHandler {
  constructor(
    private notifications: NotificationService
  ) {
  }

  handleError(error) {
    if (isDevMode()) console.log(error);
    this.notifications.cannotContactServer();
  }
}
