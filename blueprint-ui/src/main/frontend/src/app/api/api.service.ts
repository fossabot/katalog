import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Page } from './page';
import { Namespace, Schema, Version } from './model';

@Injectable()
export class ApiService {
  constructor(private http: HttpClient) {
  }

  async getNamespaces(filter?: string): Promise<Page<Namespace>> {
    return this.http
      .get<Page<Namespace>>('/api/v1/namespaces', {
        params: {
          filter: filter
        }
      })
      .toPromise();
  }

  async getSchemas(namespaces: Namespace[]): Promise<Page<Schema>> {
    return this.http
      .get<Page<Schema>>('/api/v1/schemas', {
        params: {
          namespaceIds: namespaces.map(n => n.id)
        }
      })
      .toPromise();
  }

  async getSchema(schemaId: string): Promise<Schema> {
    return this.http
      .get<Schema>(`/api/v1/schemas/${schemaId}`)
      .toPromise();
  }

  async getVersions(schemas: Schema[]): Promise<Page<Version>> {
    return this.http
      .get<Page<Version>>('/api/v1/versions', {
        params: {
          schemaIds: schemas.map(n => n.id)
        }
      })
      .toPromise();
  }
}
