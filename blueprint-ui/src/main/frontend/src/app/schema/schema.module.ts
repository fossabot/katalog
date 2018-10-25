import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { SchemaComponent } from './schema.component';
import { AuthGuard } from '../auth/auth.guard';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { VersionModule } from "../version/version.module";

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
    FontAwesomeModule,
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
