import {Router} from '@angular/router';
import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpResponse} from '@angular/common/http';
import {LoginResult} from './login-result';

@Injectable()
export class AuthService {
  loggedIn: boolean;

  constructor(private router: Router, private http: HttpClient) {
  }

  redirectToLogin() {
    this.router.navigate(['login']).then(() => {
    });
  }

  /**
   * Try to login using basic auth
   */
  async login(username: string, password: string) {
    try {
      const result: HttpResponse<User> = await this.http
        .get<User>('/api/v1/auth/user', {
          headers: {
            'Authorization': 'Basic ' + btoa(`${username}:${password}`),
            'X-Requested-With': 'XMLHttpRequest'
          },
          observe: 'response'
        })
        .toPromise();

      if (result.ok) {
        localStorage.setItem('authToken', result.headers.get('X-AUTH-TOKEN'));
        this.loggedIn = true;
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

  /**
   * Is there a stored token?
   */
  static get hasToken(): boolean {
    return localStorage.getItem('authToken') != null;
  }

  /**
   * Determines if the stored auth token is still valid
   */
  async isTokenValid() {
    try {
      const authToken = localStorage.getItem('authToken');
      if (authToken) {
        const result: HttpResponse<User> = await
          this.http
            .get<User>('/api/v1/auth/user', {
              headers: {
                'X-AUTH-TOKEN': authToken,
                'X-Requested-With': 'XMLHttpRequest'
              },
              observe: 'response'
            })
            .toPromise();
        return result.ok;
      }
    } catch (err) {
      return false;
    }
  }
}
