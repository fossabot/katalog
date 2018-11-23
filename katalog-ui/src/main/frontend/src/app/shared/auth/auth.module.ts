import {NgModule} from "@angular/core";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {LoginComponent} from "~/shared/auth/login.component";
import {RouterModule, Routes} from "@angular/router";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {ClarityModule} from "@clr/angular";
import {HTTP_INTERCEPTORS} from "@angular/common/http";
import {AuthInterceptor} from "~/shared/auth/auth.interceptor";

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
    RouterModule.forChild(routes)
  ],
  declarations: [
    LoginComponent
  ],
  providers: [
    AuthGuard,
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true}
  ]
})
export class AuthModule {
}
