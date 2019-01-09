import {Injectable} from "@angular/core";
import {MenuItem} from "~/shared/menu/menu-item";
import {ReplaySubject} from "rxjs";
import {hasAnyPermission, Namespace} from "~/shared/api/model";
import {GroupService} from "~/shared/auth/group.service";

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  items$ = new ReplaySubject<MenuItem[]>();

  constructor(private groupService: GroupService) {
  }

  async setFilteredItems(namespace: Namespace, items: MenuItem[]) {
    const group = await this.groupService.findGroupById(namespace.groupId);
    if (group === null) return false;

    const filtered = items.filter(item => {
      if (!item.permissions) return true;
      return hasAnyPermission(group, item.permissions);
    });
    this.items$.next(filtered);
  }

  setItems(items: MenuItem[]) {
    this.items$.next(items);
  }
}
