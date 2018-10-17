import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';

import {library} from '@fortawesome/fontawesome-svg-core';
import {faLock, faSearch, faUser} from '@fortawesome/free-solid-svg-icons';
import {DashboardModule} from './dashboard/dashboard.module';

import {RouterModule, Routes} from '@angular/router';
import {AuthModule} from './auth/auth.module';
import {HttpClientModule, HttpClientXsrfModule} from '@angular/common/http';
import {PageNotFoundComponent} from './page-not-found.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NavBarModule} from './navbar/navbar.module';
import {SchemaBrowserModule} from './schema-browser/schema-browser.module';
import {ApiModule} from './api/api.module';
import {NotificationModule} from './notifications/notification.module';

library.add(faUser, faLock, faSearch);

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
    ApiModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
