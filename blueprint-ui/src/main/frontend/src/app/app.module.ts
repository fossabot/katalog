import { BrowserModule } from '@angular/platform-browser';
import { ErrorHandler, NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { library } from '@fortawesome/fontawesome-svg-core';
import { faArchive, faCodeBranch, faLock, faProjectDiagram, faSearch, faUser } from '@fortawesome/free-solid-svg-icons';
import { DashboardModule } from './dashboard/dashboard.module';

import { RouterModule, Routes } from '@angular/router';
import { AuthModule } from './auth/auth.module';
import { HttpClientModule, HttpClientXsrfModule } from '@angular/common/http';
import { PageNotFoundComponent } from './page-not-found.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NavBarModule } from './navbar/navbar.module';
import { NamespaceModule } from './namespaces/namespace.module';
import { ApiModule } from './api/api.module';
import { NotificationModule } from './notifications/notification.module';
import { SchemaModule } from './schema/schema.module';
import { BlueprintErrorHandler } from "./error-handler";
import { VersionModule } from "./version/version.module";
import { MomentModule } from "ngx-moment";

library.add(faUser, faLock, faSearch, faArchive, faProjectDiagram, faCodeBranch);

const appRoutes: Routes = [
  {path: '**', component: PageNotFoundComponent}
];

@NgModule({
  declarations: [
    PageNotFoundComponent,
    AppComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    HttpClientXsrfModule,
    MomentModule,
    NotificationModule,
    RouterModule.forRoot(appRoutes),
    FontAwesomeModule,
    AuthModule,
    DashboardModule,
    NavBarModule,
    NamespaceModule,
    SchemaModule,
    VersionModule,
    ApiModule
  ],
  providers: [{
    provide: ErrorHandler, useClass: BlueprintErrorHandler
  }],
  bootstrap: [AppComponent]
})
export class AppModule {
}
