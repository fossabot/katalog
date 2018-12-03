import {Component, OnDestroy, OnInit} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";
import {MenuService} from "~/shared/menu/menu.service";
import {Namespace, Schema, Version} from "~/shared/api/model";
import {NavigationService} from "~/shared/navigation/navigation.service";
import {ClrDatagridStateInterface} from "@clr/angular";
import {DataGridState} from "~/shared/datagrid.utils";

@Component({
  selector: 'app-schema',
  templateUrl: './schema.component.html',
  styleUrls: ['./schema.component.css']
})
export class SchemaComponent implements OnInit, OnDestroy {
  namespace: Namespace;
  schema: Schema;
  state = new DataGridState<Version>("version", "DESC");

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

    const currentRoute = this.navigation.getNamespacesLinkByObject(this.namespace, this.schema);
    this.menu.setItems([
      {title: 'Details', commands: [...currentRoute, 'details']},
    ]);
  }

  ngOnDestroy() {
    this.menu.setItems([]);
  }

  async refresh(clrState: ClrDatagridStateInterface<Version>) {
    this.state.applyClrState(clrState);
    await this.state.load(options => {
      return this.api.getVersions([this.schema], {
        onlyCurrentVersions: false,
        ...options
      });
    });
  }
}
