import {NgModule} from "@angular/core";
import {NamespaceComponent} from "~/features/namespaces/namespace.component";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {CommonModule} from "@angular/common";
import {ClarityModule} from "@clr/angular";
import {MomentModule} from "ngx-moment";

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
    CommonModule,
    MomentModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    NamespaceComponent
  ],
  exports: [
    NamespaceComponent
  ]
})
export class NamespaceModule {

}
