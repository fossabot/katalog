import {Component, EventEmitter, Output, ViewChild} from "@angular/core";
import {ApiService} from "~/shared/api/api.service";
import {Modal} from "~/shared/modal/modal";
import {button, cancelButton} from "~/shared/modal/modal-button-defaults";
import {ButtonResponses} from "~/shared/modal/modal-button";
import {Token} from "~/shared/api/model";

@Component({
  selector: 'app-modal-delete-token',
  templateUrl: './modal-delete-token.component.html'
})
export class ModalDeleteTokenComponent {
  token: Token;

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
          await this.api.deleteToken(this.token);
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
    cancel.title = "Keep token";

    this.modal = {
      title: "Delete token",
      buttons: [
        cancel,
        deleteButton
      ],
      defaultButton: cancel
    };
  }

  public async open(token: Token) {
    this.token = token;
    this.component.open();
  }
}
