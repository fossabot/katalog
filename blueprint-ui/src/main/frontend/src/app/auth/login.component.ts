import {Component} from '@angular/core';
import {AuthService} from './auth.service';
import {transition, trigger} from '@angular/animations';
import {animationFailure} from '../animations';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  animations: [
    trigger('buttonState', [
      transition('* => failed', animationFailure)
    ])
  ]
})
export class LoginComponent {
  username: string;
  password: string;
  isLoading: boolean;
  buttonState: string;
  message: string;

  constructor(private authService: AuthService) {
  }

  async login() {
    this.buttonState = null;
    this.message = null;
    this.isLoading = true;
    const loginResult = await this.authService.login(this.username, this.password);
    if (loginResult.ok) {
      this.authService.redirect();
    } else {
      this.message = loginResult.message;
      this.buttonState = 'failed';
    }
    this.isLoading = false;
  }
}