import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Page} from './page';

export namespace BrowseSummary {
  export class Namespace {
    constructor(public name: string, public schemas: Schema[]) {
    }
  }

  export class Schema {
    constructor(public name: string, public versions: Version[]) {
    }
  }

  export class Version {
    constructor(public version: string) {
    }
  }
}

@Injectable()
export class BrowseService {
  constructor(private http: HttpClient) {
  }

  async getBrowseSummary(): Promise<Page<BrowseSummary.Namespace>> {
    return await this.http
      .get<Page<BrowseSummary.Namespace>>('/api/v1/browse')
      .toPromise();
  }
}
