import {Component} from "@angular/core";
import {LoginService} from "~/shared/auth/login.service";
import {UserService} from "~/shared/auth/user.service";

@Component({
  selector: 'app-topbar',
  templateUrl: './topbar.component.html'
})
export class TopBarComponent {
  constructor(private loginService: LoginService, private userService: UserService) {
  }

  currentUser() {
    return this.userService.currentUser;
  }

  async logout() {
    await this.loginService.logout();
    this.loginService.redirectToLogin('');
  }
}
