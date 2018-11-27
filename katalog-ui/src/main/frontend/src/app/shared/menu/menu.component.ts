import {Component, OnDestroy, OnInit} from "@angular/core";
import {MenuService} from "~/shared/menu/menu.service";
import {MenuItem} from "~/shared/menu/menu-item";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html'
})
export class MenuComponent implements OnInit, OnDestroy {
  items: MenuItem[] = [];
  itemSubscription: Subscription;

  constructor(private menuService: MenuService) {
  }

  ngOnInit(): void {
    this.itemSubscription = this.menuService.items$.subscribe(items => {
      this.items = items;
    });
  }

  ngOnDestroy(): void {
    this.itemSubscription.unsubscribe();
  }
}
