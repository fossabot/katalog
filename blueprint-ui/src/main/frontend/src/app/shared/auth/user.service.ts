import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {ReplaySubject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private _user$ = new ReplaySubject<User>();
  private _currentUser: User;

  constructor(private http: HttpClient) {
  }

  async ensureUserLoaded(): Promise<User> {
    if (UserService.token) {
      const result: HttpResponse<User> = await
        this.http
          .get<User>('/api/v1/auth/user-details', {
            observe: 'response'
          })
          .toPromise();
      this._user$.next(result.body);
      this._currentUser = result.body;
      return result.body;
    } else {
      this._user$.next(null);
      this._currentUser = null;
      return null;
    }
  }

  static get token() {
    return localStorage.getItem('authToken');
  }

  async setAuthToken(value: string) {
    if (value !== UserService.token) {
      if (value == null) {
        localStorage.removeItem('authToken');
      } else {
        localStorage.setItem('authToken', value);
      }
      await this.ensureUserLoaded();
    }
  }

  get user$() {
    return this._user$;
  }

  get currentUser() {
    return this._currentUser;
  }
}
