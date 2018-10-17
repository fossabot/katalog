import {Component, OnDestroy, OnInit} from '@angular/core';
import {BrowseService, BrowseSummary} from '../api/browse.service';
import {concat, Subject, Subscription} from 'rxjs';
import {catchError, debounceTime, distinctUntilChanged, finalize, map, switchMap} from 'rxjs/operators';
import {CANNOT_CONTACT_SERVER_ERROR, NotificationService} from '../notifications/notification.service';

@Component({
  selector: 'app-schema-browser',
  templateUrl: './schema-browser.component.html',
  styleUrls: ['./schema-browser.component.css']
})
export class SchemaBrowserComponent implements OnInit, OnDestroy {
  namespaces$: Subject<BrowseSummary.Namespace[]> = new Subject();
  spinner = new Subject<boolean>();

  private filter = new Subject<string>();

  private dataSubscription: Subscription;

  constructor(
    private browseService: BrowseService,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit() {
    const filtered = this.filter.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((filter: string) => this.load(filter)),
    );

    const initial = this.load('');

    this.dataSubscription = concat(initial, filtered).subscribe(data => {
      this.namespaces$.next(data);
    });
  }

  ngOnDestroy() {
    this.dataSubscription.unsubscribe();
  }

  search(filter: string) {
    this.filter.next(filter.trim());
  }

  private load(filter: string) {
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
  }
}
