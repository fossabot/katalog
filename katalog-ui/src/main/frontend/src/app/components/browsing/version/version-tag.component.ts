import {Component, Input} from '@angular/core';
import {Schema, Version} from '~/shared/api/model';
import {LinkService} from '../breadcrumbs/link.service';

@Component({
  selector: 'app-version-tag',
  templateUrl: './version-tag.component.html'
})
export class VersionTagComponent {
  @Input() schema: Schema;
  @Input() version: Version;

  constructor(public links: LinkService) {
  }
}
