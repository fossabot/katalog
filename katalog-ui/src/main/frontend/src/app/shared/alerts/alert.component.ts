import {Component, EventEmitter, Input, Output, ViewChild} from "@angular/core";
import {Alert} from "./alert";
import {ClrAlerts} from "@clr/angular";

@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html'
})
export class AlertComponent {
  @Input() alerts: Alert[];
  @Input() isAppLevel: boolean = false;
  @Output() alertClosed = new EventEmitter<Alert>();
  @ViewChild("clrAlerts") clrAlerts: ClrAlerts;

  public async onAlertClick(alert: Alert, action: () => Promise<boolean>) {
    const shouldClose = await action();
    if (shouldClose) this.alertClosed.emit(alert);
  }

  clrAlertClosedChange(alert: Alert) {
    this.alertClosed.emit(alert);
  }
}
