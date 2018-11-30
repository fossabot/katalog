import {Component, OnInit} from "@angular/core";
import {Namespace, Schema, Version} from "~/shared/api/model";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";
import {MenuService} from "~/shared/menu/menu.service";
import {NavigationService} from "~/shared/navigation/navigation.service";
import {ClrDatagridStateInterface} from "@clr/angular";
import {stateToPage} from "~/shared/datagrid.utils";

@Component({
  selector: 'app-namespace',
  templateUrl: './namespace.component.html',
  styleUrls: ['./namespace.component.css']
})
export class NamespaceComponent implements OnInit {
  namespace: Namespace;
  schemas: Schema[];
  totalSchemas = 0;
  isLoading: boolean;

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
  }

  async refresh(state: ClrDatagridStateInterface<Version>) {
    const [pagination, sorting] = stateToPage(state, "schema", "ASC");

    window.setTimeout(() => {
      this.isLoading = true;
    }, 0);

    try {
      const response = await this.api.getSchemas([this.namespace], {
        pagination: pagination,
        sorting: sorting
      });

      this.schemas = response.data;
      this.totalSchemas = response.totalElements;
    } finally {
      window.setTimeout(() => {
        this.isLoading = false;
      }, 0);
    }
  }
}
