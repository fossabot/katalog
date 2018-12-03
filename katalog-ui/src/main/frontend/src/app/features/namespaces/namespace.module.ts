import {NgModule} from "@angular/core";
import {NamespaceComponent} from "~/features/namespaces/namespace.component";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {CommonModule} from "@angular/common";
import {ClarityModule, ClrFormsModule} from "@clr/angular";
import {MomentModule} from "ngx-moment";
import {ModalCreateSchemaComponent} from "~/features/namespaces/modal-create-schema.component";
import {DirectivesModule} from "~/shared/directives/directives.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ModalModule} from "~/shared/modal/modal.module";
import {NamespaceSettingsComponent} from "~/features/namespaces/namespace-settings.component";
import {NamespaceDetailsComponent} from "~/features/namespaces/namespace-details.component";
import {NamespaceTokensComponent} from "~/features/namespaces/namespace-tokens.component";

const routes: Routes = [
  {
    canActivate: [AuthGuard],
    path: 'namespaces/:namespace',
    component: NamespaceComponent,
    children: [
      {
        path: '',
        redirectTo: 'details',
        pathMatch: 'full'
      },
      {
        path: 'details',
        component: NamespaceDetailsComponent
      },
      {
        path: 'tokens',
        component: NamespaceTokensComponent
      },
      {
        path: 'settings',
        component: NamespaceSettingsComponent
      }
    ]
  },
];

@NgModule({
  imports: [
    ClarityModule,
    ClrFormsModule,
    CommonModule,
    DirectivesModule,
    FormsModule,
    ModalModule,
    MomentModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    NamespaceComponent,
    NamespaceDetailsComponent,
    NamespaceTokensComponent,
    NamespaceSettingsComponent,
    ModalCreateSchemaComponent
  ],
  exports: [
    NamespaceComponent
  ]
})
export class NamespaceModule {

}
