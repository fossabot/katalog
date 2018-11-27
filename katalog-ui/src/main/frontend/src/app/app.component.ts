import {ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {UserService} from "~/shared/auth/user.service";
import {GlobalAlertService} from "~/global-alert.service";
import {MenuService} from "~/shared/menu/menu.service";
import {Alert} from "~/shared/alerts/alert";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit, OnDestroy {
  alerts: Alert[] = [];
  alertSubscription: Subscription;

  constructor(
    private userService: UserService,
    private globalAlerts: GlobalAlertService,
    private menuService: MenuService,
    private cd: ChangeDetectorRef
  ) {
  }

  public getUser() {
    return this.userService.currentUser;
  }

  ngOnInit(): void {
    this.alertSubscription = this.globalAlerts.alerts$.subscribe(a => {
      this.alerts = a;
      this.cd.detectChanges();
    });
  }

  ngOnDestroy(): void {
    this.alertSubscription.unsubscribe();
  }

  alertClosed(alert: Alert) {
    this.globalAlerts.remove(alert);
  }
}
