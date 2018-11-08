import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BreadcrumbsComponent} from './breadcrumbs.component';
import {RouterModule} from '@angular/router';
import {IconsModule} from '~/shared/icon.module';
import {LinkService} from './link.service';

@NgModule({
  imports: [
    CommonModule,
    IconsModule,
    RouterModule,
  ],
  providers: [
    LinkService
  ],
  declarations: [
    BreadcrumbsComponent,
  ],
  exports: [
    BreadcrumbsComponent
  ]
})
export class BreadcrumbsModule {
}
