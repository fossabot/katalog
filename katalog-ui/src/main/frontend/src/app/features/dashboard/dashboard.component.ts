import {Component, OnDestroy, OnInit} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {Namespace} from "~/shared/api/model";
import {Subject, Subscription} from "rxjs";
import '~/shared/extensions';
import {NavigationService} from "~/shared/navigation/navigation.service";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  spinner$ = new Subject<boolean>();
  initialLoadComplete: boolean;

  namespaces: Namespace[] = [];

  private filter$ = new Subject<string>();
  private filterSubscription: Subscription;

  constructor(private api: ApiService, public navigation: NavigationService) {
  }

  async ngOnInit() {
    this.filterSubscription = this.filter$.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(async (filter: string) => {
      await this.load(filter);
    });

    await this.load();
  }

  ngOnDestroy() {
    this.filterSubscription.unsubscribe();
  }

  search(filter: string) {
    this.filter$.next(filter.trim());
  }

  private async load(filter?: string) {
    this.spinner$.next(true);
    try {
      this.namespaces = (await this.api.getNamespaces(filter)).data;
    } finally {
      this.spinner$.next(false);
      this.initialLoadComplete = true;
    }
  }
}
