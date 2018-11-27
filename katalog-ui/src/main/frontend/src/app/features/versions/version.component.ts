import {Component, OnDestroy, OnInit} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";
import {MenuService} from "~/shared/menu/menu.service";
import {Namespace, Schema, Version} from "~/shared/api/model";

@Component({
  selector: 'app-version',
  templateUrl: './version.component.html'
})
export class VersionComponent implements OnInit, OnDestroy {
  namespace: Namespace;
  schema: Schema;
  version: Version;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private menu: MenuService,
  ) {
  }

  async ngOnInit() {
    this.namespace = await this.api.findNamespace(this.route.snapshot.paramMap.get('namespace'));
    this.schema = await this.api.findSchema(this.namespace.namespace, this.route.snapshot.paramMap.get('schema'));
    this.version = await this.api.findVersion(this.namespace.namespace, this.schema.schema, this.route.snapshot.paramMap.get('version'));

    this.menu.setItems([
      {type: 'link', title: 'Details'},
      {type: 'link', title: 'Settings'}
    ]);
  }

  ngOnDestroy(): void {
    this.menu.setItems([]);
  }
}
