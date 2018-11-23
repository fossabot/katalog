import {Component} from '@angular/core';
import {UserService} from "~/shared/auth/user.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent {
  constructor(private userService: UserService) {
  }

  public getUser() {
    return this.userService.currentUser;
  }
}
