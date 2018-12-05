import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {VersionComponent} from "~/features/versions/version.component";
import {ModalCreateArtifactComponent} from "~/features/versions/modal-create-artifact.component";
import {ModalDeleteArtifactComponent} from "~/features/versions/modal-delete-artifact.component";
import {SharedModule} from "~/shared.module";

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: 'namespaces/:namespace/schemas/:schema/versions/:version',
    redirectTo: 'namespaces/:namespace/schemas/:schema/versions/:version/details',
    pathMatch: 'full'
  },
  {
    canActivate: [AuthGuard],
    path: 'namespaces/:namespace/schemas/:schema/versions/:version/details',
    component: VersionComponent,
    pathMatch: 'full'
  }

];

@NgModule({
  imports: [
    SharedModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    VersionComponent,
    ModalCreateArtifactComponent,
    ModalDeleteArtifactComponent
  ],
  exports: [
    VersionComponent
  ]
})
export class VersionModule {
}
