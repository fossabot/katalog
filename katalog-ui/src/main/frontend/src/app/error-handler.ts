import {ErrorHandler, Injectable, isDevMode} from '@angular/core';
import {HttpErrorResponse} from "@angular/common/http";
import {GlobalAlertService} from "~/global-alert.service";
import {ApplicationError} from "~/application-error";

@Injectable()
export class KatalogErrorHandler implements ErrorHandler {
  constructor(
    private globalAlerts: GlobalAlertService
  ) {
  }

  handleError(error: any) {
    // Is this an ApiResponse which is already handled?
    if (error.rejection && error.rejection.handled) {
      return;
    }

    if (isDevMode()) {
      console.dir(error);
    }

    if (error instanceof HttpErrorResponse) {
      if (!navigator.onLine) {
        this.globalAlerts.alerts.push({
          message: 'No internet connection', isClosable: true, type: 'danger'
        });
      } else {
        this.globalAlerts.alerts.push({
          message: 'Could not contact server', isClosable: true, type: 'danger'
        });
      }

      return;
    }

    console.dir(error);
    console.log(error instanceof ApplicationError);
    if (error instanceof ApplicationError) {
      this.globalAlerts.alerts.push({
        message: error.message, isClosable: true, type: 'danger'
      });
      return;
    }

    this.globalAlerts.alerts.push({
      message: 'An unknown error occurred', isClosable: true, type: 'danger'
    });
  }
}
