import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NamespaceBrowserComponent } from './namespace-browser.component';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../auth/auth.guard';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { SchemaModule } from '../schema/schema.module';
import { SchemaSummaryComponent } from './schema-summary.component';

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: 'namespaces',
    component: NamespaceBrowserComponent,
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [
    SchemaModule,
    FontAwesomeModule,
    RouterModule.forChild(routes),
    CommonModule,
  ],
  declarations: [
    NamespaceBrowserComponent,
    SchemaSummaryComponent
  ],
  exports: [
    NamespaceBrowserComponent
  ]
})
export class NamespaceModule {
}
