import {Component, EventEmitter, Output, ViewChild} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {Modal} from "~/shared/modal/modal";
import {button, cancelButton} from "~/shared/modal/modal-button-defaults";
import {ButtonResponses} from "~/shared/modal/modal-button";
import {Version} from "~/shared/api/model";

@Component({
  selector: 'app-modal-delete-version',
  templateUrl: './modal-delete-version.component.html'
})
export class ModalDeleteVersionComponent {
  version: Version;
  totalArtifacts: number;

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
          await this.api.deleteVersion(this.version);
          return true;
        } catch (e) {
          switch (e.status) {
            case 404:
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
    cancel.title = "Keep version";

    this.modal = {
      title: "Delete version",
      buttons: [
        cancel,
        deleteButton
      ],
      defaultButton: cancel
    };
  }

  public async open(version: Version) {
    this.version = version;

    // Find out how many artifacts are beneath this version
    const response = await this.api.getArtifacts([this.version], {});
    this.totalArtifacts = response.totalElements;

    this.component.open();
  }
}
