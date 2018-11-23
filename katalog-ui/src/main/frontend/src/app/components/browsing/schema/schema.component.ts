import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Schema, Version} from '~/shared/api/model';
import {ApiService} from '~/shared/api/api.service';
import '~/shared/extensions';

@Component({
  selector: 'app-schema',
  templateUrl: './schema.component.html',
  styleUrls: ['./schema.component.css']
})
export class SchemaComponent implements OnInit {
  schema: Schema;
  versionGroupKeys: Number[];
  versionGroups: Map<Number, Version[]>;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute
  ) {
  }

  async ngOnInit() {
    this.schema = await this.api.findSchema(this.route.snapshot.paramMap.get('namespace'), this.route.snapshot.paramMap.get('schema'));

    const versions = (await this.api.getVersions([this.schema], false)).data;
    this.versionGroups = versions.toMultiMap(version => version.major);
    this.versionGroupKeys = Array.from(this.versionGroups.keys());
  }

  getVersions(major: Number) {
    return this.versionGroups.get(major);
  }
}
