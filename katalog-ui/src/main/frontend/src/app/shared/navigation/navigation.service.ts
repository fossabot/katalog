import {Injectable} from '@angular/core';
import {Namespace, Schema, Version} from '~/shared/api/model';

@Injectable({
  providedIn: 'root'
})
export class NavigationService {
  // noinspection JSMethodCanBeStatic
  getNamespacesLinkByName(namespace?: string, schema?: string, version?: string) {
    const result = ['/'];
    if (namespace) {
      result.push('namespaces', namespace);
      if (schema) {
        result.push('schemas', schema);
        if (version) {
          result.push('versions', version);
        }
      }
    }

    return result;
  }

  // noinspection JSMethodCanBeStatic
  getNamespacesLinkByObject(namespace?: Namespace, schema?: Schema, version?: Version) {
    return this.getNamespacesLinkByName(
      namespace ? namespace.namespace : null,
      schema ? schema.schema : null,
      version ? version.version : null
    );
  }
}
