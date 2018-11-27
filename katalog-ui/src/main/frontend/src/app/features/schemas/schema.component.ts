import {Component, OnInit} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";
import {MenuService} from "~/shared/menu/menu.service";
import {Namespace, Schema, Version} from "~/shared/api/model";
import {NavigationService} from "~/shared/navigation/navigation.service";

@Component({
  selector: 'app-schema',
  templateUrl: './schema.component.html'
})
export class SchemaComponent implements OnInit {
  namespace: Namespace;
  schema: Schema;
  versions: Version[];

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private menu: MenuService,
    private navigation: NavigationService
  ) {
  }

  async ngOnInit() {
    const currentRoute = this.navigation.getNamespacesLinkByObject(this.namespace, this.schema);
    this.menu.setItems([
      {title: 'Details', commands: [...currentRoute, 'details']},
    ]);

    this.namespace = await this.api.findNamespace(this.route.snapshot.paramMap.get('namespace'));
    this.schema = await this.api.findSchema(this.namespace.namespace, this.route.snapshot.paramMap.get('schema'));

    this.versions = (await this.api.getVersions([this.schema])).data;
  }
}
