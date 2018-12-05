import {NgModule} from "@angular/core";
import {ClarityModule} from "@clr/angular";
import {AlertComponent} from "~/shared/alerts/alert.component";
import {CommonModule} from "@angular/common";
import {PopupAlertComponent} from "~/shared/alerts/popup-alert.component";

@NgModule({
  imports: [
    CommonModule,
    ClarityModule
  ],
  declarations: [
    AlertComponent,
    PopupAlertComponent
  ],
  exports: [
    AlertComponent,
    PopupAlertComponent
  ]
})
export class AlertModule {
}
