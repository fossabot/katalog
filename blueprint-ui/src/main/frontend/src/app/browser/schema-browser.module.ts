import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SchemaBrowserComponent} from './schema-browser.component';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from '../auth/auth.guard';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {SchemaModule} from '../schema/schema.module';
import {SchemaSummaryComponent} from './schema-summary.component';

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: 'browse',
    component: SchemaBrowserComponent,
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
    SchemaBrowserComponent,
    SchemaSummaryComponent
  ],
  exports: [
    SchemaBrowserComponent
  ]
})
export class SchemaBrowserModule {
}
