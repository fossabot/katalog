import {NgModule} from '@angular/core';
import {DashboardComponent} from './dashboard.component';
import {RouterModule, Routes} from '@angular/router';
import {AuthModule} from '~/shared/auth/auth.module';
import {AuthGuard} from '~/shared/auth/auth.guard';

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: '',
    component: DashboardComponent,
    pathMatch: 'full'
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
