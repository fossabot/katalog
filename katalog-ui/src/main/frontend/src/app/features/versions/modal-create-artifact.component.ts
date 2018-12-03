import {Component, EventEmitter, Input, Output, ViewChild} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {ApiService} from "~/shared/api/api.service";
import {Modal} from "~/shared/modal/modal";
import {button, cancelButton} from "~/shared/modal/modal-button-defaults";
import {ButtonResponses} from "~/shared/modal/modal-button";
import {Version} from "~/shared/api/model";

@Component({
  selector: 'app-modal-create-artifact',
  templateUrl: './modal-create-artifact.component.html'
})
export class ModalCreateArtifactComponent {
  @Input("version") version: Version;
  @ViewChild("component") component;
  @Output() artifactCreated = new EventEmitter<void>();
  modal: Modal;

  form = new FormGroup({
    artifact: new FormControl(null, Validators.required),
  });

  private file: File;

  constructor(
    private api: ApiService
  ) {
    const createButton = button(
      "create",
      async () => {
        try {
          await this.api.createArtifact(this.version, this.file);
          return true;
        } catch (e) {
          switch (e.status) {
            case 409:
              this.form.controls['artifact'].setErrors({'duplicate': true});
              return;
          }

          throw e;
        }
      },
      () => {
        this.artifactCreated.emit();
        return new ButtonResponses.Done();
      }
    );

    this.modal = {
      title: "Create artifact",
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

  onFileChange(event) {
    if (event.target.files && event.target.files.length) {
      const [file] = event.target.files;
      this.file = file;
    }
  }
}
