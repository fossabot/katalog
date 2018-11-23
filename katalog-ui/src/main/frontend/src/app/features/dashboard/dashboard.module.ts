import {NgModule} from "@angular/core";
import {DashboardComponent} from "~/features/dashboard/dashboard.component";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: 'dashboard',
    component: DashboardComponent,
    pathMatch: 'full'
  }
];

@NgModule({
  declarations: [
    DashboardComponent
  ],
  imports: [
    RouterModule.forChild(routes)
  ],
  exports: [
    DashboardComponent
  ]
})
export class DashboardModule {

}
