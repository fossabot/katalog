import {NgModule} from "@angular/core";
import {AutofocusDirective} from "./autofocus.directive";
import {RemoveHostDirective} from "~/shared/directives/remove-host.directive";

@NgModule({
  declarations: [
    AutofocusDirective,
    RemoveHostDirective
  ],
  exports: [
    AutofocusDirective,
    RemoveHostDirective
  ]
})
export class DirectivesModule {
}
