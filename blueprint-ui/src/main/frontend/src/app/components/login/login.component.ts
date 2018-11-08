import {Component} from '@angular/core';
import {AuthService} from '~/shared/auth/auth.service';
import {transition, trigger} from '@angular/animations';
import {ANIMATION_FAILURE} from '~/shared/animations';

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
