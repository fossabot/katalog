import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {ClarityModule, ClrFormsModule} from "@clr/angular";
import {CommonModule} from "@angular/common";
import {MomentModule} from "ngx-moment";
import {SchemaComponent} from "~/features/schemas/schema.component";
import {DirectivesModule} from "~/shared/directives/directives.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ModalModule} from "~/shared/modal/modal.module";
import {ModalCreateVersionComponent} from "~/features/schemas/modal-create-version.component";

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: 'namespaces/:namespace/schemas/:schema',
    redirectTo: '/namespaces/:namespace/schemas/:schema/details',
    pathMatch: 'full'
  },
  {
    canActivate: [AuthGuard],
    path: 'namespaces/:namespace/schemas/:schema/details',
    component: SchemaComponent,
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
    SchemaComponent,
    ModalCreateVersionComponent
  ],
  exports: [
    SchemaComponent
  ]
})
export class SchemaModule {
}
