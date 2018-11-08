import {Injectable} from "@angular/core";
import {Namespace, Schema, Version} from "../api/model";

@Injectable()
export class LinkService {
  // noinspection JSMethodCanBeStatic
  getNamespacesLinkByName(namespace?: string, schema?: string, version?: string) {
    let result = ['/'];
    if (namespace) {
      result.push('namespaces', namespace);
      if (schema) {
        result.push(schema);
        if (version) {
          result.push(version);
        }
      }
    }

    return result;
  }

  // noinspection JSMethodCanBeStatic
  getNamespacesLinkByObject(namespace?: Namespace, schema?: Schema, version?: Version) {
    return this.getNamespacesLinkByName(namespace ? namespace.namespace : null, schema ? schema.schema : null, version ? version.version : null);
  }
}
