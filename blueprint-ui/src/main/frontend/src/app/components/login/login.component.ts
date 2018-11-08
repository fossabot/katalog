import {Component} from '@angular/core';
import {transition, trigger} from '@angular/animations';
import {ANIMATION_FAILURE} from '~/shared/animations';
import {LoginService} from '~/shared/auth/login.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  animations: [
    trigger('buttonState', [
      transition('* => failed', ANIMATION_FAILURE)
    ])
  ]
})
export class LoginComponent {
  username: string;
  password: string;
  isLoading: boolean;
  buttonState: string;
  message: string;

  constructor(private loginService: LoginService) {
  }

  async login() {
    this.buttonState = null;
    this.message = null;
    this.isLoading = true;
    const loginResult = await this.loginService.login(this.username, this.password);
    if (loginResult.ok) {
      this.loginService.redirect();
    } else {
      this.message = loginResult.message;
      this.buttonState = 'failed';
    }
    this.isLoading = false;
  }
}
