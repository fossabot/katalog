import {Component, OnInit} from '@angular/core';
import {BrowseService, BrowseSummary} from '../api/browse.service';
import {Observable, Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, map, startWith, switchMap} from 'rxjs/operators';

@Component({
  selector: 'app-schema-browser',
  templateUrl: './schema-browser.component.html',
  styleUrls: ['./schema-browser.component.css']
})
export class SchemaBrowserComponent implements OnInit {
  namespaces$: Observable<BrowseSummary.Namespace[]>;
  private filter = new Subject<string>();

  constructor(private browseService: BrowseService) {
  }

  ngOnInit() {
    this.namespaces$ = this.filter.pipe(
      startWith(''),
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((filter: string) => {
        return this.browseService.getBrowseSummary(filter).pipe(map(response => response.data));
      }),
    );
  }

  search(filter: string) {
    this.filter.next(filter.trim());
  }
}
