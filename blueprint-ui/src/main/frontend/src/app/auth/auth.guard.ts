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
    if (AuthService.hasToken) {
      const isTokenValid = await this.auth.isTokenValid();
      if (isTokenValid) {
        return true;
      }
    }

    if (!this.auth.loggedIn) {
      localStorage.setItem('authRedirect', state.url);
      this.auth.redirectToLogin();
      return false;
    } else {
      return true;
    }
  }
}
