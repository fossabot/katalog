import {Component, Input, ViewChild} from "@angular/core";
import {Alert} from "./alert";
import {ClrAlerts} from "@clr/angular";

@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html'
})
export class AlertComponent {
  @Input() alerts: Alert[];
  @Input() isAppLevel: boolean = false;
  @ViewChild("clrAlerts") clrAlerts: ClrAlerts;

  public async onAlertClick(alert: Alert, action: () => Promise<boolean>) {
    const shouldClose = await action();
    if (shouldClose) this.remove(alert);
  }

  clrAlertClosedChange(alert: Alert) {
    this.remove(alert);
  }

  private remove(alert: Alert) {
    // Make sure we don't remove the active alert
    this.clrAlerts.multiAlertService.previous();

    const index = this.alerts.indexOf(alert);
    this.alerts.splice(index, 1);
  }
}
