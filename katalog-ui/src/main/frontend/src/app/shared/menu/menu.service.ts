import {Injectable} from "@angular/core";
import {MenuItem} from "~/shared/menu/menu-item";
import {ReplaySubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  items$ = new ReplaySubject<MenuItem[]>();

  setItems(items: MenuItem[]) {
    this.items$.next(items);
  }
}
