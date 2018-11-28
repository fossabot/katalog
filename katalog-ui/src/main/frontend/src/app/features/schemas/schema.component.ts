import {Component, OnInit} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";
import {MenuService} from "~/shared/menu/menu.service";
import {Namespace, Schema, Version} from "~/shared/api/model";
import {NavigationService} from "~/shared/navigation/navigation.service";
import {ClrDatagridStateInterface} from "@clr/angular";
import {SortingRequest} from "~/shared/api/sorting";

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

    await this.loadVersions(1, null);
  }

  async refresh(state: ClrDatagridStateInterface<Version>) {
    const page = state.page.from / state.page.size;

    let sorting: SortingRequest = null;

    if (state.sort) {
      sorting =
        {
          column: state.sort.by.toString(),
          direction: state.sort.reverse ? "DESC" : "ASC"
        };
    } else {
      sorting = {
        column: "version",
        direction: "DESC"
      }
    }

    await this.loadVersions(page + 1, sorting);
  }

  async loadVersions(page: number, sorting: SortingRequest) {
    const response = await this.api.getVersions([this.schema], {
      onlyCurrentVersions: false,
      pagination: {page: page, size: 10},
      sorting: sorting
    });

    this.versions = response.data;
    this.totalVersions = response.totalElements;
  }
}
