import {Component, OnInit} from "@angular/core";
import {Namespace} from "~/shared/api/model";
import {ApiService} from "~/shared/api/api.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-namespace-settings',
  templateUrl: './namespace-settings.component.html'
})
export class NamespaceSettingsComponent implements OnInit {
  namespace: Namespace;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute
  ) {
  }

  async ngOnInit() {
    this.namespace = await this.api.findNamespace(this.route.snapshot.parent.paramMap.get('namespace'));
  }
}
