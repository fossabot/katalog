import {NgModule} from "@angular/core";
import {DashboardComponent} from "~/features/dashboard/dashboard.component";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {CommonModule} from "@angular/common";
import {ClarityModule, ClrFormsModule} from "@clr/angular";
import {ModalNewNamespaceComponent} from "~/features/dashboard/modal-new-namespace.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AlertModule} from "~/shared/alerts/alert.module";

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
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    DashboardComponent,
    ModalNewNamespaceComponent
  ],
  exports: [
    DashboardComponent
  ]
})
export class DashboardModule {

}
