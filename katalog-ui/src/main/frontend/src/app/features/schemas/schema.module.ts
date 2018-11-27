import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {ClarityModule} from "@clr/angular";
import {CommonModule} from "@angular/common";
import {MomentModule} from "ngx-moment";
import {SchemaComponent} from "~/features/schemas/schema.component";

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
    ClarityModule,
    CommonModule,
    MomentModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    SchemaComponent
  ],
  exports: [
    SchemaComponent
  ]
})
export class SchemaModule {
}
