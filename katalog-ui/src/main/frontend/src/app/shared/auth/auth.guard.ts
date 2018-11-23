import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot} from '@angular/router';
import {LoginService} from '~/shared/auth/login.service';
import {UserService} from '~/shared/auth/user.service';

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(
    private user: UserService,
    private login: LoginService
  ) {
  }

  async canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ) {
    if (!this.user.currentUser) {
      await this.login.logout();
      this.login.redirectToLogin(state.url);
      return false;
    } else {
      return true;
    }
  }
}
