import {Component, OnDestroy, OnInit} from "@angular/core";
import {Namespace, Schema, Version} from "~/shared/api/model";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";
import {MenuService} from "~/shared/menu/menu.service";

@Component({
  selector: 'app-namespace',
  templateUrl: './namespace.component.html'
})
export class NamespaceComponent implements OnInit, OnDestroy {
  namespace: Namespace;
  schemas: Schema[];
  versions: Map<String, Version[]> = new Map();

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private menu: MenuService
  ) {
  }

  async ngOnInit() {
    this.namespace = await this.api.findNamespace(this.route.snapshot.paramMap.get('namespace'));
    this.schemas = (await this.api.getSchemas([this.namespace])).data;

    const versionList = (await this.api.getVersions(this.schemas)).data;
    this.versions = versionList.toMultiMap(version => version.schemaId);

    this.menu.setItems([
      {type: 'link', title: 'a'},
      {type: 'link', title: 'b'}
    ]);
  }

  ngOnDestroy(): void {
    this.menu.setItems([]);
  }

  getVersions(schema: Schema): Version[] {
    return this.versions.get(schema.id) || [];
  }
}
