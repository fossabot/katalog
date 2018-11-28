import {Component, OnDestroy, OnInit} from "@angular/core";
import {MenuService} from "~/shared/menu/menu.service";
import {MenuItem} from "~/shared/menu/menu-item";
import {Subscription} from "rxjs";
import {NavigationStart, Router} from "@angular/router";

@Component({
  selector: '[app-menu]',
  templateUrl: './menu.component.html'
})
export class MenuComponent implements OnInit, OnDestroy {
  items: MenuItem[] = [];
  private itemSubscription: Subscription;
  private routerSubscription: Subscription;

  constructor(
    private menuService: MenuService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.itemSubscription = this.menuService.items$.subscribe(items => {
      this.items = items;
    });

    this.routerSubscription = this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        // Clear menu when navigating. The component that is routed to will set the menu, if needed.
        this.menuService.setItems([]);
      }
    });
  }

  ngOnDestroy(): void {
    this.itemSubscription.unsubscribe();
  }
}
