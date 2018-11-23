import {NgModule} from "@angular/core";
import {TopBarComponent} from "~/features/topbar/topbar.component";
import {ClarityModule} from "@clr/angular";
import {AuthModule} from "~/shared/auth/auth.module";

@NgModule({
  imports: [
    ClarityModule,
    AuthModule
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
