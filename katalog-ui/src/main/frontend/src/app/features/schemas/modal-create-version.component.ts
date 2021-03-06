import {Component, Input, ViewChild} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {ApiService} from "~/shared/api/api.service";
import {NavigationService} from "~/shared/navigation/navigation.service";
import {Modal} from "~/shared/modal/modal";
import {button, cancelButton} from "~/shared/modal/modal-button-defaults";
import {ButtonResponses} from "~/shared/modal/modal-button";
import {Schema} from "~/shared/api/model";

@Component({
  selector: 'app-modal-create-version',
  templateUrl: './modal-create-version.component.html'
})
export class ModalCreateVersionComponent {
  @Input("schema") schema: Schema;
  @ViewChild("component") component;
  modal: Modal;

  form = new FormGroup({
    version: new FormControl('', Validators.required),
  });

  constructor(
    private api: ApiService,
    private navigation: NavigationService
  ) {
    const createButton = button(
      "create",
      async () => {
        try {
          await this.api.createVersion(this.schema, this.form.controls.version.value);
          return true;
        } catch (e) {
          if (e.status == 409) {
              this.form.controls['version'].setErrors({'duplicate': true});
              return;
          }

          throw e;
        }
      },
      () => {
        const redirectUrl = this.navigation.getNamespacesLinkByName(this.schema.namespace.namespace, this.schema.schema, this.form.controls.version.value);
        return new ButtonResponses.Route(redirectUrl);
      }
    );

    this.modal = {
      title: "Create version",
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
