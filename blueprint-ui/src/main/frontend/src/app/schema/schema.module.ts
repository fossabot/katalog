import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { SchemaComponent } from './schema.component';
import { AuthGuard } from '../auth/auth.guard';
import { VersionModule } from "../version/version.module";
import { MomentModule } from "ngx-moment";
import { BreadcrumbsModule } from "../breadcrumbs/breadcrumbs.module";

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: 'schemas/:schemaId',
    component: SchemaComponent,
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [
    CommonModule,
    BreadcrumbsModule,
    MomentModule,
    RouterModule.forChild(routes),
    VersionModule
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
