import {NgModule} from '@angular/core';
import {DashboardComponent} from './dashboard.component';
import {LoginComponent} from '../auth/login.component';
import {RouterModule} from '@angular/router';
import {AuthModule} from '../auth/auth.module';

const routes = [
  {
    path: 'login',
    component: LoginComponent
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    AuthModule
  ],
  declarations: [
    DashboardComponent
  ]
})
export class DashboardModule {
}
