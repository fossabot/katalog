import {NgModule} from '@angular/core';
import {DashboardComponent} from './dashboard.component';
import {LoginComponent} from '../auth/login.component';
import {RouterModule} from '@angular/router';

const routes = [
  {
    path: 'login',
    component: LoginComponent
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes)
  ],
  declarations: [
    DashboardComponent
  ]
})
export class DashboardModule {
}
