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
    let user: User = null;

    try {
      const result: HttpResponse<User> = await
        this.http
          .get<User>('/api/v1/auth/user-details', {
            observe: 'response',
            withCredentials: true
          })
          .toPromise();

      user = result.body;
    } catch (e) {
    }

    this._user$.next(user);
    this._currentUser = user;
    return user;
  }

  get currentUser() {
    return this._currentUser;
  }
}
