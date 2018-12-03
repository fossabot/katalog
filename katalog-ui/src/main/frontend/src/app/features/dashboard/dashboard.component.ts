import {Component, OnDestroy, OnInit} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {Namespace} from "~/shared/api/model";
import {Subject, Subscription} from "rxjs";
import '~/shared/extensions';
import {NavigationService} from "~/shared/navigation/navigation.service";
import {ClrDatagridStateInterface} from "@clr/angular";
import {DataGridState} from "~/shared/datagrid.utils";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  state = new DataGridState<Namespace>("namespace", "ASC");

  private filter$ = new Subject<string>();
  private filter: string;
  private filterSubscription: Subscription;

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
  }

  ngOnDestroy() {
    this.filterSubscription.unsubscribe();
  }

  search(filter: string) {
    this.filter$.next(filter.trim());
  }

  async refresh(clrState: ClrDatagridStateInterface<Namespace>) {
    this.state.applyClrState(clrState);
    await this.load();
  }

  async load() {
    await this.state.load(options => {
      return this.api.getNamespaces({
        filter: this.filter,
        ...options
      });
    });
  }
}
