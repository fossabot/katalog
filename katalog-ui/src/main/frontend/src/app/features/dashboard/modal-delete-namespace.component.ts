import {Component, EventEmitter, Output, ViewChild} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {Modal} from "~/shared/modal/modal";
import {button, cancelButton} from "~/shared/modal/modal-button-defaults";
import {ButtonResponses} from "~/shared/modal/modal-button";
import {Namespace} from "~/shared/api/model";

@Component({
  selector: 'app-modal-delete-namespace',
  templateUrl: './modal-delete-namespace.component.html'
})
export class ModalDeleteNamespaceComponent {
  namespace: Namespace;
  totalSchemas: number;

  @ViewChild("component") component;
  modal: Modal;
  @Output() onSuccess = new EventEmitter();

  constructor(
    private api: ApiService
  ) {
    const deleteButton = button(
      "Delete",
      async () => {
        try {
          await this.api.deleteNamespace(this.namespace);
          return true;
        } catch (e) {
          if (e.status == 404) {
            return;
          }

          throw e;
        }
      },
      () => {
        this.onSuccess.emit();
        return new ButtonResponses.Done();
      }
    );
    deleteButton.classes = ['btn-danger'];

    const cancel = cancelButton();
    cancel.title = "Keep namespace";

    this.modal = {
      title: "Delete namespace",
      buttons: [
        cancel,
        deleteButton
      ],
      defaultButton: cancel
    };
  }

  public async open(namespace: Namespace) {
    this.namespace = namespace;

    // Find out how many schemas are beneath this namespace
    const response = await this.api.getSchemas([this.namespace], {});
    this.totalSchemas = response.totalElements;

    this.component.open();
  }
}
