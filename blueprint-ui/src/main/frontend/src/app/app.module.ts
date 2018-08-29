import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';

import {library} from '@fortawesome/fontawesome-svg-core';
import {faLock, faUser} from '@fortawesome/free-solid-svg-icons';
import {DashboardModule} from './dashboard/dashboard.module';

import {RouterModule, Routes} from '@angular/router';
import {AuthModule} from './auth/auth.module';
import {HttpClientModule} from '@angular/common/http';
import {PageNotFoundComponent} from './page-not-found.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

library.add(faUser, faLock);

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
    RouterModule.forRoot(appRoutes),
    FontAwesomeModule,
    AuthModule,
    DashboardModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
