import {Directive, Input, TemplateRef, ViewContainerRef} from '@angular/core';
import {AuthService} from './auth.service';

@Directive({selector: '[appHasAuthority]'})
export class HasAuthorityDirective {
  private hasView = false;

  constructor(
    private auth: AuthService,
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef) {
  }

  @Input() set appHasAuthority(authority: string) {
    const condition = this.auth.user != null && this.auth.user.authorities.find(a => a.authority === authority);

    if (condition && !this.hasView) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else if (!condition && this.hasView) {
      this.viewContainer.clear();
      this.hasView = false;
    }
  }
}
