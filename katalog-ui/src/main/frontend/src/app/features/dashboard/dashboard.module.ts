import {NgModule} from "@angular/core";
import {DashboardComponent} from "~/features/dashboard/dashboard.component";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {CommonModule} from "@angular/common";
import {ClarityModule, ClrFormsModule} from "@clr/angular";
import {ModalCreateNamespaceComponent} from "~/features/dashboard/modal-create-namespace.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AlertModule} from "~/shared/alerts/alert.module";
import {DirectivesModule} from "~/shared/directives/directives.module";
import {ModalModule} from "~/shared/modal/modal.module";

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
    AlertModule,
    CommonModule,
    ClarityModule,
    ClrFormsModule,
    DirectivesModule,
    FormsModule,
    ModalModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    DashboardComponent,
    ModalCreateNamespaceComponent
  ],
  exports: [
    DashboardComponent
  ]
})
export class DashboardModule {

}
