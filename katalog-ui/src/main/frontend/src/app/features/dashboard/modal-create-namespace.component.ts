import {Component, ViewChild} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {ApiService} from "~/shared/api/api.service";
import {NavigationService} from "~/shared/navigation/navigation.service";
import {Modal} from "~/shared/modal/modal";
import {button, cancelButton} from "~/shared/modal/modal-button-defaults";
import {ButtonResponses} from "~/shared/modal/modal-button";

@Component({
  selector: 'app-modal-create-namespace',
  templateUrl: './modal-create-namespace.component.html'
})
export class ModalCreateNamespaceComponent {
  @ViewChild("component") component;
  modal: Modal;

  form = new FormGroup({
    name: new FormControl('', Validators.required),
  });

  constructor(
    private api: ApiService,
    private navigation: NavigationService
  ) {
    const createButton = button(
      "create",
      async () => {
        try {
          await this.api.createNamespace(this.form.controls.name.value);
          return true;
        } catch (e) {
          switch (e.status) {
            case 409:
              this.form.controls['name'].setErrors({'duplicate': true});
              return;
          }

          throw e;
        }
      },
      () => {
        const redirectUrl = this.navigation.getNamespacesLinkByName(this.form.controls.name.value);
        return new ButtonResponses.Route(redirectUrl);
      }
    );

    this.modal = {
      title: "Create namespace",
      buttons: [
        cancelButton(),
        createButton
      ],
      defaultButton: createButton
    };
  }

  public open() {
    this.form.reset();
    this.component.open();
  }
}
