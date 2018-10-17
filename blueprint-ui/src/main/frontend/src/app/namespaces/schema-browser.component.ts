import {Component, OnInit} from '@angular/core';
import {BrowseService, BrowseSummary} from '../api/browse.service';
import {Observable, Subject} from 'rxjs';
import {catchError, debounceTime, distinctUntilChanged, finalize, map, startWith, switchMap} from 'rxjs/operators';
import {CANNOT_CONTACT_SERVER_ERROR, NotificationService} from '../notifications/notification.service';

@Component({
  selector: 'app-schema-browser',
  templateUrl: './schema-browser.component.html',
  styleUrls: ['./schema-browser.component.css']
})
export class SchemaBrowserComponent implements OnInit {
  namespaces$: Observable<BrowseSummary.Namespace[]>;
  spinner = new Subject<boolean>();
  private filter = new Subject<string>();

  constructor(
    private browseService: BrowseService,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit() {
    this.namespaces$ = this.filter.pipe(
      startWith(''),
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((filter: string) => {
        this.spinner.next(true);
        return this.browseService.getBrowseSummary(filter).pipe(
          map(response => response.data),
          catchError(() => {
            this.notificationService.push(CANNOT_CONTACT_SERVER_ERROR);
            return [];
          }),
          finalize(() => {
            this.spinner.next(false);
          })
        );
      }),
    );
  }

  search(filter: string) {
    this.filter.next(filter.trim());
  }
}
