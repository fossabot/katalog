import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Namespace, Schema, Version} from '~/shared/api/model';
import {ApiService} from '~/shared/api/api.service';
import '~/shared/extensions';

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
    private route: ActivatedRoute
  ) {
  }

  async ngOnInit() {
    this.namespace = await this.api.findNamespace(this.route.snapshot.paramMap.get('namespace'));
    this.schemas = (await this.api.getSchemas([this.namespace])).data;

    const versionList = (await this.api.getVersions(this.schemas)).data;
    this.versions = versionList.toMultiMap(version => version.schemaId);
  }

  getVersions(schema: Schema): Version[] {
    return this.versions.get(schema.id) || [];
  }
}
