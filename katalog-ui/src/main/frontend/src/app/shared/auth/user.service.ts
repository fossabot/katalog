import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpResponse} from '@angular/common/http';
import {ReplaySubject} from 'rxjs';
import {User} from "~/shared/auth/user";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private _user$ = new ReplaySubject<User>();
  private _currentUser: User;

  constructor(private http: HttpClient) {
  }

  async updateCurrentUser(): Promise<User> {
    try {
      const result: HttpResponse<User> = await
        this.http
          .get<User>('/api/v1/auth/user-details', {
            observe: 'response',
            withCredentials: true
          })
          .toPromise();

      this._user$.next(result.body);
      this._currentUser = result.body;
      return result.body;
    } catch (e) {
      // Couldn't get user-details

      // Did we get a redirect to a (OAuth2) login page?
      if (e instanceof HttpErrorResponse) {
        const redirect = e.headers.get("x-redirect");

        // Yes, so redirect
        if (redirect) {
          window.location.href = redirect;
          return null;
        }
      }

      // No, so just do nothing
      this._user$.next(null);
      this._currentUser = null;
      return null;
    }
  }

  get currentUser() {
    return this._currentUser;
  }
}
