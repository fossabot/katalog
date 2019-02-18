import {Component, OnInit} from "@angular/core";
import {Namespace, Token} from "~/shared/api/model";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";
import {DataGridState} from "~/shared/datagrid.utils";
import {ClrDatagridStateInterface} from "@clr/angular";

@Component({
  selector: 'app-namespace-tokens',
  templateUrl: './namespace-tokens.component.html',
  styleUrls: ['./namespace-tokens.component.css']
})
export class NamespaceTokensComponent implements OnInit {
  namespace: Namespace;
  state = new DataGridState<Token>("description", "ASC");
  isOpen: boolean = false;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
  ) {
  }

  async ngOnInit() {
    this.namespace = await this.api.findNamespace(this.route.snapshot.parent.paramMap.get('namespace'));
  }

  async refresh(clrState: ClrDatagridStateInterface<Token>) {
    this.state.applyClrState(clrState);
    await this.load();
  }

  async load() {
    await this.state.load(options => {
      return this.api.getTokens(options);
    });
  }
}
