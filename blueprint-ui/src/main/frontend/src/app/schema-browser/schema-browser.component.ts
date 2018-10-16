import {Component, OnInit} from '@angular/core';
import {BrowseService, BrowseSummary} from '../api/browse.service';

@Component({
  selector: 'app-schema-browser',
  templateUrl: './schema-browser.component.html',
  styleUrls: ['./schema-browser.component.css']
})
export class SchemaBrowserComponent implements OnInit {
  namespaces: BrowseSummary.Namespace[];

  constructor(private browseService: BrowseService) {
  }

  async ngOnInit() {
    const result = await this.browseService.getBrowseSummary();
    this.namespaces = result.data;
  }
}
