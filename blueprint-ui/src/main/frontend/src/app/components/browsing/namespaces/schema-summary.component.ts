import {Component, Input} from '@angular/core';
import {Namespace, Schema, Version} from '~/shared/api/model';
import {LinkService} from '../breadcrumbs/link.service';

@Component({
  selector: 'app-schema-summary',
  templateUrl: './schema-summary.component.html'
})
export class SchemaSummaryComponent {
  @Input() namespace: Namespace;
  @Input() schema: Schema;
  @Input() versions: Version[];

  constructor(public links: LinkService) {
  }
}
