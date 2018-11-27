import {Component, OnDestroy, OnInit} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";
import {MenuService} from "~/shared/menu/menu.service";
import {Namespace, Schema, Version} from "~/shared/api/model";
import {NavigationService} from "~/shared/navigation/navigation.service";

@Component({
  selector: 'app-schema',
  templateUrl: './schema.component.html'
})
export class SchemaComponent implements OnInit, OnDestroy {
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
    this.namespace = await this.api.findNamespace(this.route.snapshot.paramMap.get('namespace'));
    this.schema = await this.api.findSchema(this.namespace.namespace, this.route.snapshot.paramMap.get('schema'));

    this.versions = (await this.api.getVersions([this.schema])).data;

    this.menu.setItems([
      {type: 'link', title: 'Details'},
      {type: 'link', title: 'Settings'}
    ]);
  }

  ngOnDestroy(): void {
    this.menu.setItems([]);
  }
}
