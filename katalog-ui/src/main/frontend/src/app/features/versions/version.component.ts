import {Component, OnDestroy, OnInit} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";
import {MenuService} from "~/shared/menu/menu.service";
import {Artifact, Namespace, Schema, Version} from "~/shared/api/model";
import {NavigationService} from "~/shared/navigation/navigation.service";
import {ClrDatagridStateInterface} from "@clr/angular";
import {stateToPage} from "~/shared/datagrid.utils";

@Component({
  selector: 'app-version',
  templateUrl: './version.component.html'
})
export class VersionComponent implements OnInit, OnDestroy {
  namespace: Namespace;
  schema: Schema;
  version: Version;
  artifacts: Artifact[];
  totalArtifacts: number;
  isLoading = false;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private menu: MenuService,
    public navigation: NavigationService
  ) {
  }

  async ngOnInit() {
    this.namespace = await this.api.findNamespace(this.route.snapshot.paramMap.get('namespace'));
    this.schema = await this.api.findSchema(this.namespace.namespace, this.route.snapshot.paramMap.get('schema'));
    this.version = await this.api.findVersion(this.namespace.namespace, this.schema.schema, this.route.snapshot.paramMap.get('version'));

    const currentRoute = this.navigation.getNamespacesLinkByObject(this.namespace, this.schema, this.version);
    this.menu.setItems([
      {title: 'Details', commands: [...currentRoute, 'details']},
    ]);
  }

  ngOnDestroy() {
    this.menu.setItems([]);
  }

  async refresh(state: ClrDatagridStateInterface<Version>) {
    const [pagination, sorting] = stateToPage(state, "filename", "ASC");

    window.setTimeout(() => {
      this.isLoading = true;
    }, 0);

    try {
      const response = await this.api.getArtifacts([this.version], {
        pagination: pagination,
        sorting: sorting
      });

      this.artifacts = response.data;
      this.totalArtifacts = response.totalElements;
    } finally {
      window.setTimeout(() => {
        this.isLoading = false;
      }, 0);
    }
  }
}
