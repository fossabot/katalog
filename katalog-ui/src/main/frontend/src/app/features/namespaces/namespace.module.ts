import {NgModule} from "@angular/core";
import {NamespaceComponent} from "~/features/namespaces/namespace.component";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {CommonModule} from "@angular/common";
import {ClarityModule, ClrFormsModule} from "@clr/angular";
import {MomentModule} from "ngx-moment";
import {ModalCreateSchemaComponent} from "~/features/namespaces/modal-create-schema.component";
import {DirectivesModule} from "~/shared/directives/directives.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ModalModule} from "~/shared/modal/modal.module";

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: 'namespaces/:namespace',
    redirectTo: '/namespaces/:namespace/details',
    pathMatch: 'full'
  },
  {
    canActivate: [AuthGuard],
    path: 'namespaces/:namespace/details',
    component: NamespaceComponent,
    pathMatch: 'full'
  }

];

@NgModule({
  imports: [
    ClarityModule,
    ClrFormsModule,
    CommonModule,
    DirectivesModule,
    FormsModule,
    ModalModule,
    MomentModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    NamespaceComponent,
    ModalCreateSchemaComponent
  ],
  exports: [
    NamespaceComponent
  ]
})
export class NamespaceModule {

}
