import {NgModule} from "@angular/core";
import {TopBarComponent} from "~/features/topbar/topbar.component";
import {ClarityModule} from "@clr/angular";
import {AuthModule} from "~/shared/auth/auth.module";
import {RouterModule} from "@angular/router";

@NgModule({
  imports: [
    AuthModule,
    ClarityModule,
    RouterModule
  ],
  declarations: [
    TopBarComponent
  ],
  exports: [
    TopBarComponent
  ]
})
export class TopBarModule {
}
