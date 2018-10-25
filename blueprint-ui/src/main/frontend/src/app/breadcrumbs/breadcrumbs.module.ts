import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BreadcrumbsComponent } from "./breadcrumbs.component";
import { RouterModule } from "@angular/router";
import { IconsModule } from "../icon.module";

@NgModule({
  imports: [
    CommonModule,
    IconsModule,
    RouterModule,
  ],
  declarations: [
    BreadcrumbsComponent,
  ],
  exports: [
    BreadcrumbsComponent
  ]
})
export class BreadcrumbsModule {
}
