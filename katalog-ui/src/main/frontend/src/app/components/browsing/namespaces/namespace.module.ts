import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NamespaceBrowserComponent} from './namespace-browser.component';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from '~/shared/auth/auth.guard';
import {SchemaModule} from '../schema/schema.module';
import {SchemaSummaryComponent} from './schema-summary.component';
import {VersionModule} from '../version/version.module';
import {NamespaceComponent} from './namespace.component';
import {BreadcrumbsModule} from '../breadcrumbs/breadcrumbs.module';
import {IconsModule} from '~/shared/icon.module';

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: 'namespaces',
    component: NamespaceBrowserComponent,
    pathMatch: 'full'
  },
  {
    canActivate: [AuthGuard],
    path: 'namespaces/:namespace',
    component: NamespaceComponent,
    pathMatch: 'full'
  }

];

@NgModule({
  imports: [
    SchemaModule,
    IconsModule,
    RouterModule.forChild(routes),
    CommonModule,
    BreadcrumbsModule,
    VersionModule
  ],
  declarations: [
    NamespaceBrowserComponent,
    NamespaceComponent,
    SchemaSummaryComponent
  ],
  exports: [
    NamespaceBrowserComponent,
    NamespaceComponent
  ]
})
export class NamespaceModule {
}
