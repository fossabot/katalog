import {Router} from '@angular/router';
import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpResponse} from '@angular/common/http';
import {LoginResult} from './login-result';
import {UserService} from 'app/shared/auth/user.service';
import {User} from "~/shared/auth/user";

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  constructor(
    private router: Router,
    private http: HttpClient,
    private user: UserService
  ) {
  }

  redirectToLogin(targetUrl: string) {
    localStorage.setItem('authRedirect', targetUrl);
    this.router.navigate(['login'], {skipLocationChange: true}).then(() => {
    });
  }

  /**
   * Try to login using basic auth
   */
  async login(username: string, password: string) {
    try {
      const result: HttpResponse<User> = await this.http
        .post<User>('/api/v1/auth/login', `username=${username}&password=${password}`, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          observe: 'response'
        })
        .toPromise();

      if (result.ok) {
        await this.user.setAuthToken(result.headers.get('X-AUTH-TOKEN'));
        return new LoginResult(true);
      }
    } catch (err) {
      console.log(err);
      if (err instanceof HttpErrorResponse) {
        if (err.status !== 401) {
          return new LoginResult(false, 'There was a problem contacting the server.');
        }
      }
    }
    return new LoginResult(false, 'The provided credentials are incorrect.');
  }

  /**
   * We're logged in, so redirect back to where we came from
   */
  redirect() {
    const authRedirect = localStorage.getItem('authRedirect');
    let redirect = decodeURI(authRedirect);
    if (authRedirect === null || redirect === '/login') {
      redirect = '/';
    }
    this.router.navigate([redirect]).then(() => {
    });
    localStorage.removeItem('authRedirect');
  }

  async logout() {
    await this.user.setAuthToken(null);
    try {
      await this.http
        .post<User>('/api/v1/auth/logout', null, {
          observe: 'response'
        })
        .toPromise();
    } catch (e) {
      console.log('Could not logout', e);
    }
  }
}
