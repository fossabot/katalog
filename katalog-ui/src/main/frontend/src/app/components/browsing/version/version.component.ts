import {Component, OnInit} from '@angular/core';
import {ApiService} from '~/shared/api/api.service';
import {ActivatedRoute} from '@angular/router';
import {Schema, Version} from '~/shared/api/model';

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
    this.version = await this.api.findVersion(
      this.route.snapshot.paramMap.get('namespace'),
      this.route.snapshot.paramMap.get('schema'),
      this.route.snapshot.paramMap.get('version')
    );
    this.schema = await this.api.getSchema(this.version.schemaId);
  }
}
