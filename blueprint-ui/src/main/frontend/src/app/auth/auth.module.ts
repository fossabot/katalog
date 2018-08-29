import {NgModule} from '@angular/core';
import {AuthGuard} from './auth.guard';
import {AuthService} from './auth.service';
import {LoginComponent} from './login.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {FormsModule} from '@angular/forms';
import {RouterModule, Routes} from '@angular/router';
import {DashboardComponent} from '../dashboard/dashboard.component';

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
    FontAwesomeModule,
    FormsModule,
    RouterModule.forChild(routes)
  ],
  providers: [
    AuthGuard,
    AuthService
  ],
  declarations: [
    LoginComponent
  ]
})
export class AuthModule {
}
