import {Directive, Input, TemplateRef, ViewContainerRef} from '@angular/core';
import {UserService} from 'app/shared/auth/user.service';
import {GroupPermission, hasUserPermissions, Namespace} from "app/shared/api/model";

@Directive({
  selector: '[appHasPermission]'
})
export class HasPermissionDirective {
  permissions: GroupPermission[];
  namespace: Namespace;
  private hasView = false;

  constructor(private user: UserService,
              private templateRef: TemplateRef<any>,
              private viewContainer: ViewContainerRef) {
  }

  @Input()
  set appHasPermission(permissions: GroupPermission[]) {
    this.permissions = permissions;
    this.update();
  }

  @Input()
  set appHasPermissionNamespace(namespace: Namespace) {
    this.namespace = namespace;
    this.update();
  }

  update() {
    if (!this.permissions || !this.namespace) {
      return;
    }

    const condition = hasUserPermissions(this.user.currentUser, this.namespace.group, this.permissions);

    if (condition && !this.hasView) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else if (!condition && this.hasView) {
      this.viewContainer.clear();
      this.hasView = false;
    }
  }
}
