import {Directive, Input, TemplateRef, ViewContainerRef} from '@angular/core';
import {UserService} from 'app/shared/auth/user.service';
import {GroupPermission, hasUserPermissions, Namespace} from "app/shared/api/model";

@Directive({
  selector: '[appHasPermission]'
})
export class HasPermissionDirective {
  private permissions: GroupPermission[];
  private namespace: Namespace;
  private elseRef: TemplateRef<any>;
  private hasView = false;
  private hasElseView = false;

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

  @Input()
  set appHasPermissionElse(elseRef: TemplateRef<any>) {
    this.elseRef = elseRef;
    this.update();
  }

  update() {
    if (!this.permissions || !this.namespace) {
      return;
    }

    const condition = hasUserPermissions(this.user.currentUser, this.namespace.group, this.permissions);

    if (condition) {
      if (this.hasView) return;

      if (this.hasElseView) {
        this.viewContainer.clear();
        this.hasElseView = false;
      }

      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else {
      if (this.hasElseView) return;

      if (this.elseRef) {
        this.viewContainer.createEmbeddedView(this.elseRef);
        this.hasElseView = true;
      } else {
        this.viewContainer.clear();
        this.hasView = false;
      }
    }
  }
}
