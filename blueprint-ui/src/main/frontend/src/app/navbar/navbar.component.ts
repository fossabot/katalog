import {Component} from '@angular/core';
import {AuthService} from '../auth/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html'
})
export class NavBarComponent {
  constructor(private auth: AuthService) {

  }

  get user() {
    return this.auth.user;
  }

  logout() {
    this.auth.logout();
  }
}
