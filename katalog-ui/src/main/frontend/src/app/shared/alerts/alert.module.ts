import {NgModule} from "@angular/core";
import {ClarityModule} from "@clr/angular";
import {AlertComponent} from "~/shared/alerts/alert.component";
import {CommonModule} from "@angular/common";

@NgModule({
  imports: [
    CommonModule,
    ClarityModule
  ],
  declarations: [
    AlertComponent
  ],
  exports: [
    AlertComponent
  ]
})
export class AlertModule {
}
