import {NgModule} from '@angular/core';
import {AuthGuard} from './auth.guard';
import {AuthService} from './auth.service';
import {LoginComponent} from '~/components/login/login.component';
import {FormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {HasAuthorityDirective} from './has-authority.directive';
import {AuthInterceptor} from './auth.interceptor';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {IconsModule} from '~/shared/icon.module';

const routes = [
  {
    path: 'login',
    component: LoginComponent
  }
];

@NgModule({
  imports: [
    IconsModule,
    FormsModule,
    RouterModule.forChild(routes)
  ],
  providers: [
    AuthGuard,
    AuthService,
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
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
