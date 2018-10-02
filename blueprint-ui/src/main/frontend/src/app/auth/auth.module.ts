import {NgModule} from '@angular/core';
import {AuthGuard} from './auth.guard';
import {AuthService} from './auth.service';
import {LoginComponent} from './login.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {FormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {HasAuthorityDirective} from './has-authority.directive';

const routes = [
  {
    path: 'login',
    component: LoginComponent
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
    LoginComponent,
    HasAuthorityDirective
  ],
  exports: [
    HasAuthorityDirective
  ]
})
export class AuthModule {
}
