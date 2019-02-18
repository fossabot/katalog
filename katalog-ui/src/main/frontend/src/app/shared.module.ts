import {NgModule} from "@angular/core";
import {ClarityModule, ClrFormsModule} from "@clr/angular";
import {CommonModule} from "@angular/common";
import {DirectivesModule} from "~/shared/directives/directives.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ModalModule} from "~/shared/modal/modal.module";
import {MomentModule} from "ngx-moment";
import {AuthModule} from "~/shared/auth/auth.module";
import {FileSizeModule} from "ngx-filesize";
import {AlertModule} from "~/shared/alerts/alert.module";
import {MenuModule} from "~/shared/menu/menu.module";
import {ClipboardModule} from "ngx-clipboard";

const modules = [
  AuthModule,
  AlertModule,
  ClarityModule,
  ClipboardModule,
  ClrFormsModule,
  CommonModule,
  DirectivesModule,
  FileSizeModule,
  FormsModule,
  MenuModule,
  ModalModule,
  MomentModule,
  ReactiveFormsModule
];

@NgModule({
  imports: modules,
  exports: modules
})
export class SharedModule {
}
