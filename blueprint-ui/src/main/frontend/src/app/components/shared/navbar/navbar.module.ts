import {NgModule} from '@angular/core';
import {NavBarComponent} from './navbar.component';
import {AuthModule} from '~/shared/auth/auth.module';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';

@NgModule({
  imports: [
    CommonModule,
    AuthModule,
    RouterModule
  ],
  declarations: [
    NavBarComponent
  ],
  exports: [
    NavBarComponent
  ]
})
export class NavBarModule {
}
