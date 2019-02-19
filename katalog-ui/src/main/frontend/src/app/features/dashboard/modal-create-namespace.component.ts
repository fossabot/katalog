import {Component, OnInit, ViewChild} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ApiService} from "~/shared/api/api.service";
import {NavigationService} from "~/shared/navigation/navigation.service";
import {Modal} from "~/shared/modal/modal";
import {button, cancelButton} from "~/shared/modal/modal-button-defaults";
import {ButtonResponses} from "~/shared/modal/modal-button";
import {Group, hasPermission} from "~/shared/api/model";
import {GlobalAlertService} from "~/global-alert.service";
import {PopupAlert} from "~/shared/alerts/popup-alert";
import {GroupService} from "~/shared/auth/group.service";

@Component({
  selector: 'app-modal-create-namespace',
  templateUrl: './modal-create-namespace.component.html'
})
export class ModalCreateNamespaceComponent implements OnInit {
  @ViewChild("component") component;
  modal: Modal;
  form: FormGroup;
  groups: Group[];
  popup: PopupAlert;

  constructor(
    private api: ApiService,
    private group: GroupService,
    private navigation: NavigationService,
    private globalAlertService: GlobalAlertService,
    private fb: FormBuilder
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
          if (e.status == 409) {
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
    if (!this.groups.length) {
      this.popup = new PopupAlert(
        "Insufficient permissions",
        "You don't have any permissions that allow you to create namespaces in any of your groups."
      );
      return;
    }

    this.form.reset();
    this.form.controls.group.setValue(this.groups[0]);
    this.component.open();
  }

  async ngOnInit() {
    this.groups = this.group.getGroups()
      .filter(userGroup => hasPermission(userGroup, "CREATE"))
  }
}
