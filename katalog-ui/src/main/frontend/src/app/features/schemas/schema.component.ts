import {Component, OnInit} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";
import {MenuService} from "~/shared/menu/menu.service";
import {Namespace, Schema, Version} from "~/shared/api/model";
import {NavigationService} from "~/shared/navigation/navigation.service";
import {ClrDatagridStateInterface} from "@clr/angular";
import {stateToPage} from "~/shared/datagrid.utils";

@Component({
  selector: 'app-schema',
  templateUrl: './schema.component.html',
  styleUrls: ['./schema.component.css']
})
export class SchemaComponent implements OnInit {
  namespace: Namespace;
  schema: Schema;
  versions: Version[];
  totalVersions: number;
  isLoading = false;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private menu: MenuService,
    public navigation: NavigationService
  ) {
  }

  async ngOnInit() {
    const currentRoute = this.navigation.getNamespacesLinkByObject(this.namespace, this.schema);
    this.menu.setItems([
      {title: 'Details', commands: [...currentRoute, 'details']},
    ]);

    this.namespace = await this.api.findNamespace(this.route.snapshot.paramMap.get('namespace'));
    this.schema = await this.api.findSchema(this.namespace.namespace, this.route.snapshot.paramMap.get('schema'));
  }

  async refresh(state: ClrDatagridStateInterface<Version>) {
    const [pagination, sorting] = stateToPage(state, "version", "DESC");

    window.setTimeout(() => {
      this.isLoading = true;
    }, 0);

    try {
      const response = await this.api.getVersions([this.schema], {
        onlyCurrentVersions: false,
        pagination: pagination,
        sorting: sorting
      });

      this.versions = response.data;
      this.totalVersions = response.totalElements;
    } finally {
      window.setTimeout(() => {
        this.isLoading = false;
      }, 0);
    }
  }
}
