import {NgModule} from "@angular/core";
import {NamespaceComponent} from "~/features/namespaces/namespace.component";
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "~/shared/auth/auth.guard";
import {ModalCreateSchemaComponent} from "~/features/namespaces/modal-create-schema.component";
import {NamespaceSettingsComponent} from "~/features/namespaces/namespace-settings.component";
import {NamespaceDetailsComponent} from "~/features/namespaces/namespace-details.component";
import {NamespaceTokensComponent} from "~/features/namespaces/namespace-tokens.component";
import {ModalDeleteSchemaComponent} from "~/features/namespaces/modal-delete-schema.component";
import {SharedModule} from "~/shared.module";
import {ModalCreateTokenComponent} from "~/features/namespaces/modal-create-token.component";
import {ModalDeleteTokenComponent} from "~/features/namespaces/modal-delete-token.component";

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
    SharedModule,
    RouterModule.forChild(routes)
  ],
  declarations: [
    NamespaceComponent,
    NamespaceDetailsComponent,
    NamespaceTokensComponent,
    NamespaceSettingsComponent,
    ModalCreateSchemaComponent,
    ModalDeleteSchemaComponent,
    ModalCreateTokenComponent,
    ModalDeleteTokenComponent
  ],
  exports: [
    NamespaceComponent
  ]
})
export class NamespaceModule {

}
