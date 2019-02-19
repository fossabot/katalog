import {Component, EventEmitter, Input, Output, ViewChild} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {ApiService} from "~/shared/api/api.service";
import {Modal} from "~/shared/modal/modal";
import {button, cancelButton} from "~/shared/modal/modal-button-defaults";
import {ButtonResponses} from "~/shared/modal/modal-button";
import {CreatedToken, Namespace} from "~/shared/api/model";
import {ClrLoadingState} from "@clr/angular";

@Component({
  selector: 'app-modal-create-token',
  templateUrl: './modal-create-token.component.html',
  styleUrls: ['./modal-create-token.component.css']
})
export class ModalCreateTokenComponent {
  @Input("namespace") namespace: Namespace;
  @ViewChild("component") component;
  @Output() onSuccess = new EventEmitter();
  modal: Modal;
  createdToken: CreatedToken;
  copyTokenState = ClrLoadingState.DEFAULT;

  form = new FormGroup({
    description: new FormControl('', Validators.required),
  });

  constructor(
    private api: ApiService
  ) {
    const createButton = button(
      "create",
      async () => {
        try {
          this.createdToken = await this.api.createToken(this.namespace, this.form.controls.description.value);
          return true;
        } catch (e) {
          if (e.status == 409) {
            this.form.controls['description'].setErrors({'duplicate': true});
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

    this.modal = {
      title: "Create token",
      buttons: [
        cancelButton(),
        createButton
      ],
      defaultButton: createButton
    };
  }

  public open() {
    this.form.reset();
    this.createdToken = null;
    this.copyTokenState = ClrLoadingState.DEFAULT;
    this.component.open();
  }

  onTokenCopied() {
    if (this.copyTokenState == ClrLoadingState.SUCCESS) {
      this.copyTokenState = ClrLoadingState.DEFAULT;
      setTimeout(() => {
        this.copyTokenState = ClrLoadingState.SUCCESS;
      }, 0);
    } else {
      this.copyTokenState = ClrLoadingState.SUCCESS;
    }
  }
}
