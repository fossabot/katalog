import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SchemaBrowserComponent} from './schema-browser.component';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from '../auth/auth.guard';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';

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
    FontAwesomeModule,
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
