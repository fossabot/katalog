import {NgModule} from "@angular/core";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {LoginComponent} from "~/shared/auth/login.component";
import {RouterModule, Routes} from "@angular/router";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {ClarityModule} from "@clr/angular";
import {AlertModule} from "~/shared/alerts/alert.module";
import {HasAuthorityDirective} from "~/shared/auth/has-authority.directive";
import {HasPermissionDirective} from "~/shared/auth/has-permission.directive";
import {InsufficientPermissionsComponent} from "~/shared/auth/insufficient-permissions-alert.component";

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
    InsufficientPermissionsComponent,
    HasAuthorityDirective,
    HasPermissionDirective
  ],
  providers: [
    AuthGuard
  ],
  exports: [
    InsufficientPermissionsComponent,
    HasAuthorityDirective,
    HasPermissionDirective
  ]
})
export class AuthModule {
}
