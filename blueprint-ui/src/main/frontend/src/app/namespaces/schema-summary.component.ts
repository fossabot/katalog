import {Component, Input} from '@angular/core';
import {BrowseSummary} from '../api/browse.service';

@Component({
  selector: 'app-schema-summary',
  templateUrl: './schema-summary.component.html'
})
export class SchemaSummaryComponent {
  @Input() public namespace: BrowseSummary.Namespace;
  @Input() public schema: BrowseSummary.Schema;
}
