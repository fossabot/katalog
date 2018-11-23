import {NgModule} from "@angular/core";
import {TopBarComponent} from "~/features/topbar/topbar.component";
import {ClarityModule} from "@clr/angular";

@NgModule({
  imports: [
    ClarityModule
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
