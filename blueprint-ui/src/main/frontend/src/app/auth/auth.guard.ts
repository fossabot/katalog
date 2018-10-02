import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot} from '@angular/router';
import {AuthService} from './auth.service';

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(private auth: AuthService) {
  }

  async canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ) {
    const isTokenValid = await this.auth.isTokenValid();
    if (isTokenValid) {
      return true;
    }

    if (!this.auth.loggedIn) {
      this.auth.redirectToLogin(state.url);
      return false;
    } else {
      return true;
    }
  }
}
