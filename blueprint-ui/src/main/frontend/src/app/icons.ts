import { AfterViewInit, Component, ComponentFactoryResolver, Input, ViewChild, ViewContainerRef } from "@angular/core";
import { library } from '@fortawesome/fontawesome-svg-core';
import { faArchive, faCodeBranch, faLock, faProjectDiagram, faSearch, faUser } from "@fortawesome/free-solid-svg-icons";

library.add(faUser, faLock, faSearch, faArchive, faProjectDiagram, faCodeBranch);

@Component({
  selector: 'app-icon',
  template: '<template #container></template>'
})
export class IconComponent implements AfterViewInit {
  @Input() icon: any;
  @ViewChild("container", { read: ViewContainerRef }) container;

  constructor(
    private resolver: ComponentFactoryResolver
  ) {
  }

  ngAfterViewInit(): void {
    const factory = this.resolver.resolveComponentFactory(this.icon);
    const componentRef = this.container.createComponent(factory);
    componentRef.changeDetectorRef.detectChanges();
  }
}

@Component({
  selector: 'app-icon-namespace',
  template: '<span class="icon has-text-primary"><fa-icon icon="archive"></fa-icon></span>'
})
export class IconNamespace {
}

@Component({
  selector: 'app-icon-schema',
  template: '<span class="icon has-text-primary"><fa-icon icon="project-diagram"></fa-icon></span>'
})
export class IconSchema {
}

@Component({
  selector: 'app-icon-version',
  template: '<span class="icon has-text-primary"><fa-icon icon="code-branch"></fa-icon></span>'
})
export class IconVersion {
}

@Component({
  selector: 'app-icon-search',
  template: '<span class="icon is-left"><fa-icon icon="search"></fa-icon></span>'
})
export class IconSearch {
}

@Component({
  selector: 'app-icon-user',
  template: '<span class="icon is-left"><fa-icon icon="user"></fa-icon></span>'
})
export class IconUser {
}

@Component({
  selector: 'app-icon-password',
  template: '<span class="icon is-left"><fa-icon icon="lock"></fa-icon></span>'
})
export class IconPassword {
}

export const ICONS = [
  IconComponent,
  IconNamespace,
  IconPassword,
  IconSchema,
  IconSearch,
  IconUser,
  IconVersion
];
