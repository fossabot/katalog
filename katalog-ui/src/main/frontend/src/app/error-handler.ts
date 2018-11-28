import {ErrorHandler, Injectable, Injector, isDevMode} from '@angular/core';
import {HttpErrorResponse} from "@angular/common/http";
import {GlobalAlertService} from "~/global-alert.service";
import {ApplicationError} from "~/application-error";

@Injectable()
export class KatalogErrorHandler implements ErrorHandler {
  constructor(
    private injector: Injector
  ) {
  }

  handleError(error: any) {
    // Is this an ApiResponse which is already handled?
    if (error.rejection && error.rejection.handled) {
      return;
    }

    if (isDevMode()) {
      console.error("Caught an error in KatalogErrorHandler", error);
    }

    const globalAlerts = this.injector.get(GlobalAlertService);

    // See if we can unwrap the error
    let unwrapped = error;
    let maxUnwrapping = 10;
    while (unwrapped && maxUnwrapping-- > 0) {
      if (unwrapped instanceof HttpErrorResponse) {
        if (!navigator.onLine) {
          globalAlerts.push({
            message: 'No internet connection', isClosable: true, type: 'danger'
          });
        } else {
          globalAlerts.push({
            message: 'Could not contact server', isClosable: true, type: 'danger'
          });
        }

        return;
      }

      if (unwrapped instanceof ApplicationError) {
        globalAlerts.push({
          message: unwrapped.message, isClosable: true, type: 'danger'
        });
        return;
      }

      unwrapped = unwrapped.rejection;
    }

    globalAlerts.push({
      message: 'An unknown error occurred', isClosable: true, type: 'danger'
    });
  }
}