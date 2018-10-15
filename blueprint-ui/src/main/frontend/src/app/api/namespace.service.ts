import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Page} from './page';

export class Namespace {
  constructor(public name: string) {
  }
}

@Injectable()
export class NamespaceService {
  constructor(private http: HttpClient) {
  }

  async getNamespaces(): Promise<Page<Namespace>> {
    return await this.http
      .get<Page<Namespace>>('/api/v1/namespaces')
      .toPromise();
  }
}
