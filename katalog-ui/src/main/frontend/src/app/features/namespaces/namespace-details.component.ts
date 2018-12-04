import {Component, OnInit} from "@angular/core";
import {Namespace, Schema} from "~/shared/api/model";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";
import {NavigationService} from "~/shared/navigation/navigation.service";
import {ClrDatagridStateInterface} from "@clr/angular";
import {DataGridState} from "~/shared/datagrid.utils";

@Component({
  selector: 'app-namespace-details',
  templateUrl: './namespace-details.component.html',
  styleUrls: ['./namespace-details.component.css']
})
export class NamespaceDetailsComponent implements OnInit {
  namespace: Namespace;
  state = new DataGridState<Schema>("schema", "ASC");

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private navigation: NavigationService
  ) {
  }

  async ngOnInit() {
    this.namespace = await this.api.findNamespace(this.route.snapshot.parent.paramMap.get('namespace'));
  }

  async refresh(clrState: ClrDatagridStateInterface<Schema>) {
    this.state.applyClrState(clrState);
    await this.load();
  }

  async load() {
    await this.state.load(options => {
      return this.api.getSchemas([this.namespace], options);
    });
  }
}
