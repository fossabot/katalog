import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot} from '@angular/router';
import {AuthService} from './auth.service';

@Injectable()
export class AuthGuard implements CanActivate {
  private isUserValid: boolean;

  constructor(private auth: AuthService) {
  }

  async canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ) {
    const isTokenValid = await this.auth.isTokenValid();
    this.isUserValid = this.auth.user && isTokenValid;

    if (!this.isUserValid) {
      await this.auth.logout();
      this.auth.redirectToLogin(state.url);
      return false;
    } else {
      return true;
    }
  }
}
