import {BrowserModule} from '@angular/platform-browser';
import {ErrorHandler, NgModule} from '@angular/core';

import {AppComponent} from './app.component';

import {DashboardModule} from './components/dashboard/dashboard.module';

import {RouterModule, Routes} from '@angular/router';
import {AuthModule} from './shared/auth/auth.module';
import {HttpClientModule, HttpClientXsrfModule} from '@angular/common/http';
import {PageNotFoundComponent} from './shared/page-not-found.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NavBarModule} from './components/shared/navbar/navbar.module';
import {NamespaceModule} from './components/browsing/namespaces/namespace.module';
import {ApiModule} from './shared/api/api.module';
import {NotificationModule} from './components/shared/notifications/notification.module';
import {SchemaModule} from './components/browsing/schema/schema.module';
import {BlueprintErrorHandler} from './error-handler';
import {VersionModule} from './components/browsing/version/version.module';
import {MomentModule} from 'ngx-moment';
import {IconsModule} from './shared/icon.module';

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
    IconsModule,
    NotificationModule,
    RouterModule.forRoot(appRoutes),
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
