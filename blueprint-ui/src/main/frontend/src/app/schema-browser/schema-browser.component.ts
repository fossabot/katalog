import {Component, OnInit} from '@angular/core';
import {Namespace, NamespaceService} from '../api/namespace.service';

@Component({
  selector: 'app-schema-browser',
  templateUrl: './schema-browser.component.html'
})
export class SchemaBrowserComponent implements OnInit {
  namespaces: Namespace[];

  constructor(private namespaceService: NamespaceService) {
  }

  async ngOnInit() {
    this.namespaces = await this.namespaceService.getNamespaces();
  }
}
