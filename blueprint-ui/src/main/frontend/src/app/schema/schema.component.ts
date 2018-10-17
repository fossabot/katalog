import {Component, OnInit} from '@angular/core';
import {Schema, SchemaService} from '../api/schema.service';
import {ActivatedRoute, ParamMap} from '@angular/router';
import {switchMap} from 'rxjs/operators';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-schema',
  templateUrl: './schema.component.html'
})
export class SchemaComponent implements OnInit {
  schema$: Observable<Schema>;

  constructor(
    private route: ActivatedRoute,
    private schemaService: SchemaService
  ) {
  }

  ngOnInit() {
    this.schema$ = this.route.paramMap.pipe(
      switchMap((params: ParamMap) => this.schemaService.getSchema(params.get('namespace'), params.get('schema')))
    );
  }
}
