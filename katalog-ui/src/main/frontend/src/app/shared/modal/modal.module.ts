import {NgModule} from "@angular/core";
import {ModalComponent} from "~/shared/modal/modal.component";
import {ClarityModule} from "@clr/angular";
import {CommonModule} from "@angular/common";
import {AlertModule} from "~/shared/alerts/alert.module";

@NgModule({
  imports: [
    AlertModule,
    CommonModule,
    ClarityModule
  ],
  declarations: [
    ModalComponent
  ],
  exports: [
    ModalComponent
  ]
})
export class ModalModule {
}
