import {Component, OnInit} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";
import {MenuService} from "~/shared/menu/menu.service";
import {Namespace, Schema, Version} from "~/shared/api/model";
import {NavigationService} from "~/shared/navigation/navigation.service";

@Component({
  selector: 'app-version',
  templateUrl: './version.component.html'
})
export class VersionComponent implements OnInit {
  namespace: Namespace;
  schema: Schema;
  version: Version;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private menu: MenuService,
    public navigation: NavigationService
  ) {
  }

  async ngOnInit() {
    const currentRoute = this.navigation.getNamespacesLinkByObject(this.namespace, this.schema, this.version);
    this.menu.setItems([
      {title: 'Details', commands: [...currentRoute, 'details']},
    ]);

    this.namespace = await this.api.findNamespace(this.route.snapshot.paramMap.get('namespace'));
    this.schema = await this.api.findSchema(this.namespace.namespace, this.route.snapshot.paramMap.get('schema'));
    this.version = await this.api.findVersion(this.namespace.namespace, this.schema.schema, this.route.snapshot.paramMap.get('version'));
  }
}
