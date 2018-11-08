import {Component} from '@angular/core';
import {UserService} from '~/shared/auth/user.service';
import {LoginService} from '~/shared/auth/login.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html'
})
export class NavBarComponent {
  menuExpanded: boolean;

  constructor(
    private userService: UserService,
    private loginService: LoginService
  ) {
  }

  get user$() {
    return this.userService.user$;
  }

  async logout() {
    await this.loginService.logout();
  }

  burgerClicked() {
    this.menuExpanded = !this.menuExpanded;
  }
}
