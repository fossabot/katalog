import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
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
      this._user$.next(null);
      this._currentUser = null;
      return null;
    }
  }

  get currentUser() {
    return this._currentUser;
  }
}
