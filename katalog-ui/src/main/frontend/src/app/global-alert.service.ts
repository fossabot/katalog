import {Injectable} from "@angular/core";
import {Alert} from "~/shared/alerts/alert";
import {ReplaySubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class GlobalAlertService {
  private alerts_: Alert[] = [];
  alerts$ = new ReplaySubject<Alert[]>();

  public push(alert: Alert) {
    // Don't allow duplicate alerts
    if (this.alerts_.map(a => a.message).indexOf(alert.message) !== -1) {
      return;
    }

    this.alerts_ = [alert, ...this.alerts_];
    this.alerts$.next(this.alerts_);
  }

  remove(alert: Alert) {
    const index = this.alerts_.indexOf(alert);
    this.alerts_.splice(index, 1);
    this.alerts$.next(this.alerts_);
  }
}
