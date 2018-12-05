import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {SchemaComponent} from "~/features/schemas/schema.component";
import {ModalCreateVersionComponent} from "~/features/schemas/modal-create-version.component";
import {ModalDeleteVersionComponent} from "~/features/schemas/modal-delete-version.component";
import {SharedModule} from "~/shared.module";

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
    SharedModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    SchemaComponent,
    ModalCreateVersionComponent,
    ModalDeleteVersionComponent
  ],
  exports: [
    SchemaComponent
  ]
})
export class SchemaModule {
}
