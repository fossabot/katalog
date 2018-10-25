import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ApiService } from '../api/api.service';
import { Namespace, Schema, Version } from '../api/model';
import '../extensions';

@Component({
  selector: 'app-namespace-browser',
  templateUrl: './namespace-browser.component.html',
  styleUrls: ['./namespace-browser.component.css']
})
export class NamespaceBrowserComponent implements OnInit, OnDestroy {
  spinner$ = new Subject<boolean>();

  namespaces: Namespace[] = [];
  schemas: Map<String, Schema[]> = new Map();
  versions: Map<String, Version[]> = new Map();

  private filter$ = new Subject<string>();

  private filterSubscription: Subscription;

  constructor(
    private api: ApiService
  ) {
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

      const schemaList = (await this.api.getSchemas(this.namespaces)).data;
      this.schemas = schemaList.toMultiMap(schema => schema.namespace.id);

      const versionList = (await this.api.getVersions(schemaList)).data;
      this.versions = versionList.toMultiMap(version => version.schemaId);
    } finally {
      this.spinner$.next(false);
    }
  }

  getSchemas(namespace: Namespace): Schema[] {
    return this.schemas.get(namespace.id) || [];
  }

  getVersions(schema: Schema): Version[] {
    return this.versions.get(schema.id) || [];
  }
}
