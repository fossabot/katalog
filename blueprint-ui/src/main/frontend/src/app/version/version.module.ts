import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { VersionComponent } from './version.component';
import { AuthGuard } from '../auth/auth.guard';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { VersionTagComponent } from "./version-tag.component";

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: 'versions/:versionId',
    component: VersionComponent,
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [
    CommonModule,
    FontAwesomeModule,
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
