import {Injectable} from "@angular/core";
import {MenuItem} from "~/shared/menu/menu-item";
import {ReplaySubject} from "rxjs";
import {UserService} from "~/shared/auth/user.service";
import {hasUserPermissions, Namespace} from "~/shared/api/model";

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  items$ = new ReplaySubject<MenuItem[]>();

  constructor(private user: UserService) {
  }

  setFilteredItems(namespace: Namespace, items: MenuItem[]) {
    const filtered = items.filter(item => {
      if (!item.permissions) return true;

      return hasUserPermissions(this.user.currentUser, namespace.group, item.permissions);
    });
    this.items$.next(filtered);
  }

  setItems(items: MenuItem[]) {
    this.items$.next(items);
  }
}
