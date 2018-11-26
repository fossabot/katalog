import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpParams} from '@angular/common/http';
import {Page} from './page';
import {Namespace, Schema, Version} from './model';
import {Router} from '@angular/router';
import {ApiResponse} from './api-response';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(
    private http: HttpClient,
    private router: Router
  ) {
  }

  async createNamespace(namespace: string): Promise<void> {
    return this.http
      .post('/api/v1/namespaces', {
        namespace: namespace
      })
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async getNamespaces(filter?: string): Promise<Page<Namespace>> {
    return this.http
      .get<Page<Namespace>>('/api/v1/namespaces', {
        params: {
          filter: filter || ''
        }
      })
      .toPromise();
  }

  async findNamespace(namespace: string): Promise<Namespace> {
    return this.http
      .get<Namespace>(`/api/v1/namespaces/find/${namespace}`)
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async getNamespace(namespaceId: string): Promise<Namespace> {
    return this.http
      .get<Namespace>(`/api/v1/namespaces/${namespaceId}`)
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async getSchemas(namespaces: Namespace[]): Promise<Page<Schema>> {
    return this.http
      .get<Page<Schema>>('/api/v1/schemas', {
        params: new HttpParams()
          .set('namespaceIds', namespaces.map(n => n.id).join(','))
      })
      .toPromise();
  }

  async findSchema(namespace: string, schema: string): Promise<Schema> {
    return this.http
      .get<Schema>(`/api/v1/schemas/find/${namespace}/${schema}`)
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async getSchema(schemaId: string): Promise<Schema> {
    return this.http
      .get<Schema>(`/api/v1/schemas/${schemaId}`)
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async getVersions(schemas: Schema[], onlyCurrentVersions: boolean = true): Promise<Page<Version>> {
    return this.http
      .get<Page<Version>>('/api/v1/versions', {
        params: new HttpParams()
          .set('schemaIds', schemas.map(n => n.id).join(','))
          .set('onlyCurrentVersions', onlyCurrentVersions.toString())
      })
      .toPromise();
  }

  async findVersion(namespace: string, schema: string, version: String): Promise<Version> {
    return this.http
      .get<Version>(`/api/v1/versions/find/${namespace}/${schema}/${version}`)
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async getVersion(versionId: string): Promise<Version> {
    return this.http
      .get<Version>(`/api/v1/versions/${versionId}`)
      .toPromise()
      .catch(e => this.handleError(e));
  }

  private async handleError(error: Error | HttpErrorResponse): Promise<any> {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 404) {
        await this.router.navigateByUrl('404', {skipLocationChange: true});
        return Promise.reject(new ApiResponse(true));
      }
    }
    return Promise.reject(error);
  }
}
