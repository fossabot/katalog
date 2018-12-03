import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {ClarityModule, ClrFormsModule} from "@clr/angular";
import {CommonModule} from "@angular/common";
import {MomentModule} from "ngx-moment";
import {VersionComponent} from "~/features/versions/version.component";
import {FileSizeModule} from "ngx-filesize";
import {ModalCreateArtifactComponent} from "~/features/versions/modal-create-artifact.component";
import {ReactiveFormsModule} from "@angular/forms";
import {DirectivesModule} from "~/shared/directives/directives.module";
import {ModalModule} from "~/shared/modal/modal.module";

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
    ClarityModule,
    ClrFormsModule,
    CommonModule,
    DirectivesModule,
    FileSizeModule,
    ModalModule,
    MomentModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    VersionComponent,
    ModalCreateArtifactComponent
  ],
  exports: [
    VersionComponent
  ]
})
export class VersionModule {
}
