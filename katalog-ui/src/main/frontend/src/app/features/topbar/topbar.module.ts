import {NgModule} from "@angular/core";
import {TopBarComponent} from "~/features/topbar/topbar.component";
import {RouterModule} from "@angular/router";
import {SharedModule} from "~/shared.module";

@NgModule({
  imports: [
    SharedModule,
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
