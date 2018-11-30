import {Component, OnDestroy, OnInit} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {Namespace, Version} from "~/shared/api/model";
import {Subject, Subscription} from "rxjs";
import '~/shared/extensions';
import {NavigationService} from "~/shared/navigation/navigation.service";
import {ClrDatagridStateInterface} from "@clr/angular";
import {SortingRequest} from "~/shared/api/sorting";
import {PaginationRequest} from "~/shared/api/page";
import {stateToPage} from "~/shared/datagrid.utils";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  namespaces: Namespace[] = [];
  totalNamespaces = 0;
  isLoading: boolean;

  private filter$ = new Subject<string>();
  private filter: string;
  private filterSubscription: Subscription;
  private pagination: PaginationRequest;
  private sorting: SortingRequest;

  constructor(
    private api: ApiService,
    public navigation: NavigationService
  ) {
  }

  async ngOnInit() {
    this.filterSubscription = this.filter$.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(async (filter: string) => {
      this.filter = filter;
      await this.load();
    });

    await this.load();
  }

  ngOnDestroy() {
    this.filterSubscription.unsubscribe();
  }

  search(filter: string) {
    this.filter$.next(filter.trim());
  }

  async refresh(state: ClrDatagridStateInterface<Version>) {
    [this.pagination, this.sorting] = stateToPage(state, "namespace", "ASC");
    await this.load();
  }

  async load() {
    window.setTimeout(() => {
      this.isLoading = true;
    }, 0);

    try {
      const response = await this.api.getNamespaces({
        filter: this.filter,
        pagination: this.pagination,
        sorting: this.sorting
      });

      this.namespaces = response.data;
      this.totalNamespaces = response.totalElements;
    } finally {
      window.setTimeout(() => {
        this.isLoading = false;
      }, 0);
    }
  }
}
