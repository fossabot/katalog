import {Component} from "@angular/core";
import {LoginService} from "~/shared/auth/login.service";
import {ClrLoadingState} from "@clr/angular";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  username: string;
  password: string;
  submitState = ClrLoadingState.DEFAULT;
  error: string;

  constructor(private loginService: LoginService) {
  }

  async login() {
    this.error = null;
    this.submitState = ClrLoadingState.LOADING;
    const loginResult = await this.loginService.login(this.username, this.password);
    if (loginResult.ok) {
      this.submitState = ClrLoadingState.SUCCESS;
      this.loginService.redirect();
    } else {
      this.error = loginResult.message;
      this.submitState = ClrLoadingState.ERROR;
    }
  }
}
