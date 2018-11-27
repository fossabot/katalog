import {Component, OnInit} from "@angular/core";
import {Namespace, Schema, Version} from "~/shared/api/model";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";
import {MenuService} from "~/shared/menu/menu.service";
import {NavigationService} from "~/shared/navigation/navigation.service";

@Component({
  selector: 'app-namespace',
  templateUrl: './namespace.component.html'
})
export class NamespaceComponent implements OnInit {
  namespace: Namespace;
  schemas: Schema[];
  versions: Map<String, Version[]> = new Map();

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private menu: MenuService,
    private navigation: NavigationService
  ) {
  }

  async ngOnInit() {
    const currentRoute = this.navigation.getNamespacesLinkByObject(this.namespace);
    this.menu.setItems([
      {title: 'Details', commands: [...currentRoute, 'details']},
      {title: 'Members', commands: [...currentRoute, 'members']},
      {title: 'Settings', commands: [...currentRoute, 'settings']}
    ]);

    this.namespace = await this.api.findNamespace(this.route.snapshot.paramMap.get('namespace'));
    this.schemas = (await this.api.getSchemas([this.namespace])).data;

    const versionList = (await this.api.getVersions(this.schemas)).data;
    this.versions = versionList.toMultiMap(version => version.schemaId);
  }
}
