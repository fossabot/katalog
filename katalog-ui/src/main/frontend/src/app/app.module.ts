import {ClarityModule} from '@clr/angular';
import {BrowserModule} from '@angular/platform-browser';
import {APP_INITIALIZER, ErrorHandler, NgModule} from '@angular/core';

import {AppComponent} from './app.component';

import {RouterModule, Routes} from '@angular/router';
import {HttpClientModule, HttpClientXsrfModule} from '@angular/common/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {KatalogErrorHandler} from './error-handler';
import {MomentModule} from 'ngx-moment';
import {TopBarModule} from "~/features/topbar/topbar.module";
import {UserService} from "~/shared/auth/user.service";
import {DashboardModule} from "~/features/dashboard/dashboard.module";
import {AuthModule} from "~/shared/auth/auth.module";
import {AlertModule} from "~/shared/alerts/alert.module";

const appRoutes: Routes = [
  {path: '', redirectTo: '/dashboard', pathMatch: 'full'}
  //{path: '**', component: PageNotFoundComponent}
];

export function onEnsureUserLoaded(user: UserService) {
  return async () => {
    await user.ensureUserLoaded();
  };
}

@NgModule({
  declarations: [
//    PageNotFoundComponent,
    AppComponent
  ],
  imports: [
    ClarityModule,
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    HttpClientXsrfModule,
    MomentModule,
    RouterModule.forRoot(appRoutes),
    AuthModule,
    AlertModule,
    TopBarModule,
    DashboardModule
  ],
  providers: [
    {
      provide: ErrorHandler,
      useClass: KatalogErrorHandler
    },
    {
      provide: APP_INITIALIZER,
      useFactory: onEnsureUserLoaded,
      multi: true,
      deps: [UserService]
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
