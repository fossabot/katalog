import {BrowserModule} from '@angular/platform-browser';
import {APP_INITIALIZER, ErrorHandler, NgModule} from '@angular/core';

import {AppComponent} from './app.component';

import {Router, RouterModule, Routes} from '@angular/router';
import {HTTP_INTERCEPTORS, HttpClientModule, HttpClientXsrfModule} from '@angular/common/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {KatalogErrorHandler} from './error-handler';
import {TopBarModule} from "~/features/topbar/topbar.module";
import {UserService} from "~/shared/auth/user.service";
import {DashboardModule} from "~/features/dashboard/dashboard.module";
import {PageNotFoundComponent} from "~/features/pagenotfound/page-not-found.component";
import {PageNotFoundModule} from "~/features/pagenotfound/page-not-found.module";
import {NamespaceModule} from "~/features/namespaces/namespace.module";
import {SchemaModule} from "~/features/schemas/schema.module";
import {VersionModule} from "~/features/versions/version.module";
import {SharedModule} from "~/shared.module";
import {GroupService} from "~/shared/auth/group.service";
import {ApplicationStartingInterceptor} from "~/shared/api/application-starting.interceptor";
import {StartingUpModule} from "~/features/startingup/starting-up.module";

const appRoutes: Routes = [
  {path: '', redirectTo: '/dashboard', pathMatch: 'full'},
  {path: '**', component: PageNotFoundComponent}
];

export function onEnsureUserAndGroupsLoaded(user: UserService, group: GroupService) {
  return async () => {
    await user.updateCurrentUser();
    await group.updateGroups();
  };
}

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    SharedModule,
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    HttpClientXsrfModule,
    RouterModule.forRoot(appRoutes),

    DashboardModule,
    NamespaceModule,
    PageNotFoundModule,
    SchemaModule,
    StartingUpModule,
    TopBarModule,
    VersionModule
  ],
  providers: [
    {
      provide: ErrorHandler,
      useClass: KatalogErrorHandler
    },
    {
      provide: APP_INITIALIZER,
      useFactory: onEnsureUserAndGroupsLoaded,
      multi: true,
      deps: [UserService, GroupService]
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ApplicationStartingInterceptor,
      multi: true,
      deps: [Router]
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
