import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {ClarityModule} from "@clr/angular";
import {CommonModule} from "@angular/common";
import {MomentModule} from "ngx-moment";
import {VersionComponent} from "~/features/versions/version.component";

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: 'namespaces/:namespace/schemas/:schema/versions/:version',
    component: VersionComponent,
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [
    ClarityModule,
    CommonModule,
    MomentModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    VersionComponent
  ],
  exports: [
    VersionComponent
  ]
})
export class VersionModule {
}
