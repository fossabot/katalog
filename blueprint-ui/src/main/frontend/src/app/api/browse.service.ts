import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Page} from './page';
import {Observable} from 'rxjs';

export namespace BrowseSummary {
  export class Namespace {
    name: string;
    schemas: Schema[];
  }

  export class Schema {
    name: string;
    versions: Version[];
  }

  export class Version {
    version: string;
    stable: boolean;
  }
}

@Injectable()
export class BrowseService {
  constructor(private http: HttpClient) {
  }

  getBrowseSummary(filter?: string): Observable<Page<BrowseSummary.Namespace>> {
    return this.http.get<Page<BrowseSummary.Namespace>>(`/api/v1/browse?filter=${filter || ''}`);
  }
}
