import {NgModule} from "@angular/core";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {LoginComponent} from "~/shared/auth/login.component";
import {RouterModule, Routes} from "@angular/router";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {ClarityModule} from "@clr/angular";
import {HTTP_INTERCEPTORS} from "@angular/common/http";
import {AuthInterceptor} from "~/shared/auth/auth.interceptor";
import {AlertModule} from "~/shared/alerts/alert.module";
import {HasAuthorityDirective} from "~/shared/auth/has-authority.directive";
import {HasPermissionDirective} from "~/shared/auth/has-permission.directive";

const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  }
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ClarityModule,
    AlertModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    LoginComponent,
    HasAuthorityDirective,
    HasPermissionDirective
  ],
  providers: [
    AuthGuard,
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true}
  ],
  exports: [
    HasAuthorityDirective,
    HasPermissionDirective
  ]
})
export class AuthModule {
}
