import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  items: any[];

  getItems() {
    return this.items;
  }

  setItems(items: any[]) {
    this.items = items;
  }
}
