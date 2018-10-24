import { BrowserModule } from '@angular/platform-browser';
import { ErrorHandler, NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { library } from '@fortawesome/fontawesome-svg-core';
import { faArchive, faLock, faProjectDiagram, faSearch, faUser } from '@fortawesome/free-solid-svg-icons';
import { DashboardModule } from './dashboard/dashboard.module';

import { RouterModule, Routes } from '@angular/router';
import { AuthModule } from './auth/auth.module';
import { HttpClientModule, HttpClientXsrfModule } from '@angular/common/http';
import { PageNotFoundComponent } from './page-not-found.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NavBarModule } from './navbar/navbar.module';
import { SchemaBrowserModule } from './namespaces/schema-browser.module';
import { ApiModule } from './api/api.module';
import { NotificationModule } from './notifications/notification.module';
import { SchemaModule } from './schema/schema.module';
import { BlueprintErrorHandler } from "./error-handler";

library.add(faUser, faLock, faSearch, faArchive, faProjectDiagram);

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
    NotificationModule,
    RouterModule.forRoot(appRoutes),
    FontAwesomeModule,
    AuthModule,
    DashboardModule,
    NavBarModule,
    SchemaBrowserModule,
    SchemaModule,
    ApiModule
  ],
  providers: [{
    provide: ErrorHandler, useClass: BlueprintErrorHandler
  }],
  bootstrap: [AppComponent]
})
export class AppModule {
}
