import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

export class Namespace {
  constructor(public name: string) {
  }
}

@Injectable()
export class NamespaceService {
  constructor(private http: HttpClient) {
  }

  async getNamespaces(): Promise<Namespace[]> {
    return await this.http.get<Namespace[]>('/api/v1/namespaces').toPromise();
  }
}
