import {NgModule} from "@angular/core";
import {StartingUpComponent} from "~/features/startingup/starting-up.component";
import {RouterModule, Routes} from "@angular/router";

const routes: Routes = [
  {
    path: 'starting-up',
    component: StartingUpComponent,
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes)
  ],
  declarations: [
    StartingUpComponent
  ],
  exports: [
    StartingUpComponent
  ]
})
export class StartingUpModule {
}
