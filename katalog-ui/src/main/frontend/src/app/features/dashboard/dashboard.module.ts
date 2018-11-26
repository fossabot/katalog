import {NgModule} from "@angular/core";
import {DashboardComponent} from "~/features/dashboard/dashboard.component";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {CommonModule} from "@angular/common";
import {ClarityModule} from "@clr/angular";

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
    CommonModule,
    ClarityModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    DashboardComponent
  ],
  exports: [
    DashboardComponent
  ]
})
export class DashboardModule {

}
