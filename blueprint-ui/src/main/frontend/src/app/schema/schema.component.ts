import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Schema } from '../api/model';
import { ApiService } from "../api/api.service";

@Component({
  selector: 'app-schema',
  templateUrl: './schema.component.html'
})
export class SchemaComponent implements OnInit {
  schema: Schema;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute
  ) {
  }

  async ngOnInit() {
    this.schema = await this.api.getSchema(this.route.snapshot.paramMap.get('schemaId'));
  }
}
