import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SchemaBrowserComponent} from './schema-browser.component';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from '../auth/auth.guard';

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: 'schemas',
    component: SchemaBrowserComponent,
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    CommonModule,
  ],
  declarations: [
    SchemaBrowserComponent
  ],
  exports: [
    SchemaBrowserComponent
  ]
})
export class SchemaBrowserModule {
}
