import { Component, OnInit } from '@angular/core';
import { ApiService } from "../api/api.service";
import { ActivatedRoute } from "@angular/router";
import { Version } from "../api/model";

@Component({
  selector: 'app-version',
  templateUrl: './version.component.html'
})
export class VersionComponent implements OnInit {
  version: Version;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute
  ) {
  }

  async ngOnInit() {
    this.version = await this.api.getVersion(this.route.snapshot.paramMap.get('versionId'));
  }
}
