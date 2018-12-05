import {Component, OnInit, ViewChild} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ApiService} from "~/shared/api/api.service";
import {NavigationService} from "~/shared/navigation/navigation.service";
import {Modal} from "~/shared/modal/modal";
import {button, cancelButton} from "~/shared/modal/modal-button-defaults";
import {ButtonResponses} from "~/shared/modal/modal-button";
import {Group} from "~/shared/api/model";

@Component({
  selector: 'app-modal-create-namespace',
  templateUrl: './modal-create-namespace.component.html'
})
export class ModalCreateNamespaceComponent implements OnInit {
  @ViewChild("component") component;
  modal: Modal;
  form: FormGroup;
  groups: Group[];

  constructor(
    private api: ApiService,
    private navigation: NavigationService,
    fb: FormBuilder
  ) {
    this.form = fb.group({
      name: ['', Validators.required],
      group: ['', Validators.required]
    });

    const createButton = button(
      "create",
      async () => {
        try {
          const namespace = this.form.controls.name.value;
          const group = this.form.controls.group.value;
          await this.api.createNamespace(namespace, group);
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
    if (this.groups.length) {
      this.form.controls.group.setValue(this.groups[0]);
    }
    this.component.open();
  }

  async ngOnInit() {
    this.groups = await this.api.getGroups();
  }
}
