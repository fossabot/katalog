import {Component, Input, OnInit} from "@angular/core";
import {Namespace, Schema, Version} from "~/shared/api/model";
import {ApiService} from "~/shared/api/api.service";
import {NavigationService} from "~/shared/navigation/navigation.service";

@Component({
  selector: 'app-namespace-card',
  templateUrl: './namespace-card.component.html'
})
export class NamespaceCardComponent implements OnInit {
  @Input() namespace: Namespace;
  @Input() hideTitle: boolean;
  isLoadingComplete: boolean;

  schemas: Schema[];
  versions: Map<String, Version[]> = new Map();

  constructor(
    private api: ApiService,
    private navigation: NavigationService
  ) {
  }

  async ngOnInit() {
    this.schemas = (await this.api.getSchemas([this.namespace])).data;

    const versionList = (await this.api.getVersions(this.schemas, {onlyCurrentVersions: true})).data;
    this.versions = versionList.toMultiMap(version => version.schemaId);

    this.isLoadingComplete = true;
  }

  getVersions(schema: Schema): Version[] {
    return this.versions.get(schema.id) || [];
  }
}
