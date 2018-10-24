import { Component, Input } from '@angular/core';
import { Namespace, Schema, Version } from "../api/model";

@Component({
  selector: 'app-schema-summary',
  templateUrl: './schema-summary.component.html'
})
export class SchemaSummaryComponent {
  @Input() namespace: Namespace;
  @Input() schema: Schema;
  @Input() versions: Version[];
}
