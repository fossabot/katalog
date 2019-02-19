import {Component, EventEmitter, Output, ViewChild} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {Modal} from "~/shared/modal/modal";
import {button, cancelButton} from "~/shared/modal/modal-button-defaults";
import {ButtonResponses} from "~/shared/modal/modal-button";
import {Schema} from "~/shared/api/model";

@Component({
  selector: 'app-modal-delete-schema',
  templateUrl: './modal-delete-schema.component.html'
})
export class ModalDeleteSchemaComponent {
  schema: Schema;
  totalVersions: number;

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
          await this.api.deleteSchema(this.schema);
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
    cancel.title = "Keep schema";

    this.modal = {
      title: "Delete schema",
      buttons: [
        cancel,
        deleteButton
      ],
      defaultButton: cancel
    };
  }

  public async open(schema: Schema) {
    this.schema = schema;

    // Find out how many versions are beneath this schema
    const response = await this.api.getVersions(this.schema, {onlyCurrentVersions: false});
    this.totalVersions = response.totalElements;

    this.component.open();
  }
}
