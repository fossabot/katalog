import { Component, Input } from '@angular/core';
import { Namespace, Schema, Version } from "../api/model";
import { IconNamespace, IconSchema, IconVersion } from "../icons";

@Component({
  selector: 'app-breadcrumbs',
  templateUrl: './breadcrumbs.component.html',
  styleUrls: ['./breadcrumbs.component.css']
})
export class BreadcrumbsComponent {
  @Input() public namespace?: Namespace;
  @Input() public schema?: Schema;
  @Input() public version?: Version;

  get items() {
    let result = [];
    result.push({ text: 'Namespaces', routerLink: ['/namespaces'] });
    if (this.namespace) {
      result.push({ text: this.namespace.namespace, icon: IconNamespace, routerLink: ['/namespaces', this.namespace.id] });
    }
    if (this.schema) {
      result.push({ text: this.schema.schema, icon: IconSchema, routerLink: ['/schemas', this.schema.id] });
    }
    if (this.version) {
      result.push({ text: this.version.version, icon: IconVersion, routerLink: ['/versions', this.version.id] });
    }

    // The last breadcrumb is considered active
    result[result.length - 1].isActive = true;

    return result;
  }
}
