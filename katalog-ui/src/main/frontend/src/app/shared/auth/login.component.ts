import {Component, OnInit} from "@angular/core";
import {LoginService} from "~/shared/auth/login.service";
import {ClrLoadingState} from "@clr/angular";
import {Alert} from "~/shared/alerts/alert";
import {LoginOptions} from "~/shared/auth/login-options";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  username: string;
  password: string;
  submitState = ClrLoadingState.DEFAULT;
  alerts: Alert[] = [];
  loginOptions: LoginOptions;

  constructor(private loginService: LoginService) {
  }

  async login() {
    this.alerts = [];
    this.submitState = ClrLoadingState.LOADING;
    const loginResult = await this.loginService.login(this.username, this.password);
    if (loginResult.ok) {
      this.submitState = ClrLoadingState.SUCCESS;
      this.loginService.redirect();
    } else {
      this.alerts = [
        {message: loginResult.message, type: "danger", isClosable: false}
      ];
      this.submitState = ClrLoadingState.ERROR;
    }
  }

  async ngOnInit() {
    this.loginOptions = await this.loginService.getLoginOptions();
  }

  loginOAuth2() {
    window.location.href = this.loginOptions.oauth2LoginUrl;
  }
}
