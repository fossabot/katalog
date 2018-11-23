import {Component} from '@angular/core';
import {UserService} from "~/shared/auth/user.service";
import {GlobalAlertService} from "~/global-alert.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent {
  constructor(private userService: UserService, private globalAlerts: GlobalAlertService) {
  }

  public getUser() {
    return this.userService.currentUser;
  }

  public getGlobalAlerts() {
    return this.globalAlerts.alerts;
  }
}
