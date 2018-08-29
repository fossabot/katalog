import {Component} from '@angular/core';
import {AuthService} from './auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent {
  username: string;
  password: string;
  isLoading: boolean;
  message: string;

  constructor(private authService: AuthService) {
  }

  async login() {
    this.message = null;
    this.isLoading = true;
    const loginResult = await this.authService.login(this.username, this.password);
    if (loginResult.ok) {
      this.authService.redirect();
    } else {
      this.message = loginResult.message;
    }
    this.isLoading = false;
  }
}
