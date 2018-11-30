import {Component, OnDestroy, OnInit} from "@angular/core";
import {Namespace} from "~/shared/api/model";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";
import {MenuService} from "~/shared/menu/menu.service";
import {NavigationService} from "~/shared/navigation/navigation.service";

@Component({
  selector: 'app-namespace',
  templateUrl: './namespace.component.html',
  styleUrls: ['./namespace.component.css']
})
export class NamespaceComponent implements OnInit, OnDestroy {
  namespace: Namespace;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private menu: MenuService,
    private navigation: NavigationService
  ) {
  }

  async ngOnInit() {
    this.namespace = await this.api.findNamespace(this.route.snapshot.paramMap.get('namespace'));

    const currentRoute = this.navigation.getNamespacesLinkByObject(this.namespace);
    this.menu.setItems([
      {title: 'Details', commands: [...currentRoute, 'details']},
      {title: 'Members', commands: [...currentRoute, 'members']},
      {title: 'Settings', commands: [...currentRoute, 'settings']}
    ]);
  }

  ngOnDestroy() {
    this.menu.setItems([]);
  }
}
