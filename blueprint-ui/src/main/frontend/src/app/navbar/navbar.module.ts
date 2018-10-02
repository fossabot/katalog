import {NgModule} from '@angular/core';
import {NavBarComponent} from './navbar.component';
import {AuthModule} from '../auth/auth.module';
import {CommonModule} from '@angular/common';

@NgModule({
  imports: [
    CommonModule,
    AuthModule
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
