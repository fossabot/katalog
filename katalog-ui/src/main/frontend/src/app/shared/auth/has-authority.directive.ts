import {Directive, Input, TemplateRef, ViewContainerRef} from '@angular/core';
import {UserService} from '~/shared/auth/user.service';

@Directive({selector: '[appHasAuthority]'})
export class HasAuthorityDirective {
  private hasView = false;

  constructor(
    private user: UserService,
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef) {
  }

  @Input() set appHasAuthority(authority: string) {
    const condition = this.user.currentUser.authorities.find(a => a.authority === authority);

    if (condition && !this.hasView) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else if (!condition && this.hasView) {
      this.viewContainer.clear();
      this.hasView = false;
    }
  }
}