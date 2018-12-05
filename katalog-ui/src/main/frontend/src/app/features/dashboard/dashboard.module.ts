import {NgModule} from "@angular/core";
import {DashboardComponent} from "~/features/dashboard/dashboard.component";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {ModalCreateNamespaceComponent} from "~/features/dashboard/modal-create-namespace.component";
import {NamespaceModule} from "~/features/namespaces/namespace.module";
import {ModalDeleteNamespaceComponent} from "~/features/dashboard/modal-delete-namespace.component";
import {SharedModule} from "~/shared.module";

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: 'dashboard',
    component: DashboardComponent,
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [
    SharedModule,
    NamespaceModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    DashboardComponent,
    ModalCreateNamespaceComponent,
    ModalDeleteNamespaceComponent
  ],
  exports: [
    DashboardComponent
  ]
})
export class DashboardModule {

}
