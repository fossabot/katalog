import {Component, EventEmitter, Output, ViewChild} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {Modal} from "~/shared/modal/modal";
import {button, cancelButton} from "~/shared/modal/modal-button-defaults";
import {ButtonResponses} from "~/shared/modal/modal-button";
import {Artifact} from "~/shared/api/model";

@Component({
  selector: 'app-modal-delete-artifact',
  templateUrl: './modal-delete-artifact.component.html'
})
export class ModalDeleteArtifactComponent {
  artifact: Artifact;

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
          await this.api.deleteArtifact(this.artifact);
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
    cancel.title = "Keep artifact";

    this.modal = {
      title: "Delete artifact",
      buttons: [
        cancel,
        deleteButton
      ],
      defaultButton: cancel
    };
  }

  public async open(artifact: Artifact) {
    this.artifact = artifact;
    this.component.open();
  }
}
