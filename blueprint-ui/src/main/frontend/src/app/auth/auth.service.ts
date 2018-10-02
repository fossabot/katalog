import {Router} from '@angular/router';
import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpResponse} from '@angular/common/http';
import {LoginResult} from './login-result';

@Injectable()
export class AuthService {
  private currentUser?: User;
  loggedIn: boolean;

  constructor(private router: Router, private http: HttpClient) {
  }

  redirectToLogin(targetUrl: string) {
    localStorage.setItem('authRedirect', targetUrl);
    this.router.navigate(['login']).then(() => {
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
        localStorage.setItem('authToken', result.headers.get('X-AUTH-TOKEN'));
        this.loggedIn = true;
        this.currentUser = result.body;
        return new LoginResult(true);
      }
    } catch (err) {
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

  // noinspection JSMethodCanBeStatic
  get token() {
    return localStorage.getItem('authToken');
  }

  get user() {
    return this.currentUser;
  }

  /**
   * Determines if the stored auth token is still valid
   */
  async isTokenValid() {
    try {
      if (this.token) {
        const result: HttpResponse<User> = await
          this.http
            .get<User>('/api/v1/auth/user-details', {
              observe: 'response'
            })
            .toPromise();
        this.currentUser = result.body;
        return result.ok;
      }
    } catch (err) {
      return false;
    }
  }

  async logout() {
    localStorage.removeItem('authToken');
    this.currentUser = null;
    await this.http
      .post<User>('/api/v1/auth/logout', null, {
        observe: 'response'
      })
      .toPromise();

    this.redirectToLogin('');
  }
}
