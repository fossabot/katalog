import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

export class Schema {
  name: string;
  versions: any[];
}

@Injectable()
export class SchemaService {
  constructor(private http: HttpClient) {
  }

  getSchema(namespace: string, schema: string): Observable<Schema> {
    return this.http
      .get<Schema>(`/api/v1/namespaces/${namespace}/schemas/${schema}`);
  }
}
