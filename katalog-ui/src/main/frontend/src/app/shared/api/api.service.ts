import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpParams, HttpRequest} from '@angular/common/http';
import {Page, PaginationRequest} from './page';
import {Artifact, Group, Namespace, Schema, Version} from './model';
import {Router} from '@angular/router';
import {ApiResponse} from './api-response';
import {SortingRequest} from "~/shared/api/sorting";

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(
    private http: HttpClient,
    private router: Router
  ) {
  }

  async createNamespace(namespace: string, group: Group): Promise<void> {
    return this.http
      .post('/api/v1/namespaces', {
        namespace,
        groupId: group.id
      }, {withCredentials: true})
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async deleteNamespace(namespace: Namespace): Promise<void> {
    return this.http
      .delete(`/api/v1/namespaces/${namespace.id}`, {withCredentials: true})
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async getNamespaces(options: { filter?: string, pagination?: PaginationRequest, sorting?: SortingRequest }): Promise<Page<Namespace>> {
    let params = new HttpParams();

    if (options.filter) {
      params = params.set('filter', options.filter);
    }
    params = setPagination(params, options.pagination);
    params = setSorting(params, options.sorting);

    return this.http
      .get<Page<Namespace>>('/api/v1/namespaces', {
        params,
        withCredentials: true
      })
      .toPromise();
  }

  async findNamespace(namespace: string): Promise<Namespace> {
    return this.http
      .get<Namespace>(`/api/v1/namespaces/find/${namespace}`, {withCredentials: true})
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async getNamespace(namespaceId: string): Promise<Namespace> {
    return this.http
      .get<Namespace>(`/api/v1/namespaces/${namespaceId}`, {withCredentials: true})
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async createSchema(namespace: Namespace, schema: string): Promise<void> {
    return this.http
      .post('/api/v1/schemas', {
        namespaceId: namespace.id,
        schema
      }, {withCredentials: true})
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async deleteSchema(schema: Schema): Promise<void> {
    return this.http
      .delete(`/api/v1/schemas/${schema.id}`, {withCredentials: true})
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async getSchemas(namespaces: Namespace[], options: { pagination?: PaginationRequest, sorting?: SortingRequest }): Promise<Page<Schema>> {
    let params = new HttpParams()
      .set('namespaceIds', namespaces.map(n => n.id).join(','));
    params = setPagination(params, options.pagination);
    params = setSorting(params, options.sorting);

    return this.http
      .get<Page<Schema>>('/api/v1/schemas', {params, withCredentials: true})
      .toPromise();
  }

  async findSchema(namespace: string, schema: string): Promise<Schema> {
    return this.http
      .get<Schema>(`/api/v1/schemas/find/${namespace}/${schema}`, {withCredentials: true})
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async getSchema(schemaId: string): Promise<Schema> {
    return this.http
      .get<Schema>(`/api/v1/schemas/${schemaId}`, {withCredentials: true})
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async createVersion(schema: Schema, version: string): Promise<void> {
    return this.http
      .post('/api/v1/versions', {
        schemaId: schema.id,
        version
      }, {withCredentials: true})
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async deleteVersion(version: Version): Promise<void> {
    return this.http
      .delete(`/api/v1/versions/${version.id}`, {withCredentials: true})
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async getVersions(schemas: Schema[], options: { onlyCurrentVersions: boolean, pagination?: PaginationRequest, sorting?: SortingRequest }): Promise<Page<Version>> {
    let params = new HttpParams()
      .set('schemaIds', schemas.map(n => n.id).join(','))
      .set('onlyCurrentVersions', options.onlyCurrentVersions.toString());
    params = setPagination(params, options.pagination);
    params = setSorting(params, options.sorting);

    return this.http
      .get<Page<Version>>('/api/v1/versions', {params, withCredentials: true})
      .toPromise();
  }

  async findVersion(namespace: string, schema: string, version: String): Promise<Version> {
    return this.http
      .get<Version>(`/api/v1/versions/find/${namespace}/${schema}/${version}`, {withCredentials: true})
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async getVersion(versionId: string): Promise<Version> {
    return this.http
      .get<Version>(`/api/v1/versions/${versionId}`, {withCredentials: true})
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async createArtifact(version: Version, file: File): Promise<void> {
    let formData = new FormData();
    formData.append('file', file);

    let params = new HttpParams()
      .set('versionId', version.id);

    const req = new HttpRequest('POST', '/api/v1/artifacts', formData, {params, withCredentials: true});

    return this.http
      .request(req)
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async deleteArtifact(artifact: Artifact): Promise<void> {
    return this.http
      .delete(`/api/v1/artifacts/${artifact.id}`, {withCredentials: true})
      .toPromise()
      .catch(e => this.handleError(e));
  }

  async getArtifacts(versions: Version[], options: { pagination?: PaginationRequest, sorting?: SortingRequest }): Promise<Page<Artifact>> {
    let params = new HttpParams()
      .set('versionIds', versions.map(n => n.id).join(','));
    params = setPagination(params, options.pagination);
    params = setSorting(params, options.sorting);

    return this.http
      .get<Page<Artifact>>('/api/v1/artifacts', {params, withCredentials: true})
      .toPromise();
  }

  async getGroups(): Promise<Group[]> {
    return this.http
      .get<Group[]>(`/api/v1/groups`, {withCredentials: true})
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

function setPagination(params: HttpParams, pagination?: PaginationRequest) {
  if (pagination) {
    params = params
      .set('page', pagination.page.toString())
      .set('size', pagination.size.toString())
  }

  return params;
}

function setSorting(params: HttpParams, sorting?: SortingRequest) {
  if (sorting) {
    params = params
      .set('sortColumn', sorting.column)
      .set('sortDirection', sorting.direction)
  }

  return params;
}
