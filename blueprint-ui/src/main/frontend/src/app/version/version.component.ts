import { Component, OnInit } from '@angular/core';
import { ApiService } from "../api/api.service";
import { ActivatedRoute } from "@angular/router";
import { Schema, Version } from "../api/model";

@Component({
  selector: 'app-version',
  templateUrl: './version.component.html'
})
export class VersionComponent implements OnInit {
  version: Version;
  schema: Schema;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute
  ) {
  }

  async ngOnInit() {
    this.version = await this.api.getVersion(this.route.snapshot.paramMap.get('versionId'));
    this.schema = await this.api.getSchema(this.version.schemaId);
  }
}
