import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { VersionComponent } from './version.component';
import { AuthGuard } from '../auth/auth.guard';
import { VersionTagComponent } from "./version-tag.component";
import { BreadcrumbsModule } from "../breadcrumbs/breadcrumbs.module";

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: 'namespaces/:namespace/:schema/:version',
    component: VersionComponent,
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [
    CommonModule,
    BreadcrumbsModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    VersionComponent,
    VersionTagComponent
  ],
  exports: [
    VersionComponent,
    VersionTagComponent
  ]
})
export class VersionModule {
}
