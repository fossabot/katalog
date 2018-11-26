import {Component} from "@angular/core";
import {ClrLoadingState} from "@clr/angular";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {ApiService} from "~/shared/api/api.service";
import {Alert, toAlert} from "~/shared/alerts/alert";

@Component({
  selector: 'app-modal-new-namespace',
  templateUrl: './modal-new-namespace.component.html'
})
export class ModalNewNamespaceComponent {
  isOpen: boolean;
  submitButtonState = ClrLoadingState.DEFAULT;
  alert: Alert;

  form = new FormGroup({
    name: new FormControl('', Validators.required),
  });

  constructor(private api: ApiService) {
  }

  public open() {
    this.isOpen = true;
    this.form.reset();
  }

  public isSubmitting() {
    return this.submitButtonState === ClrLoadingState.LOADING;
  }

  public cancel() {
    if (this.isSubmitting()) {
      return;
    } else {
      this.isOpen = false;
    }
  }

  public async create() {
    this.submitButtonState = ClrLoadingState.LOADING;
    this.alert = null;

    try {
      await this.api.createNamespace(this.form.controls['name'].value);

      this.submitButtonState = ClrLoadingState.SUCCESS;
      setTimeout(() => {
        this.isOpen = false;
      }, 500);
    } catch (e) {
      switch (e.status) {
        case 409:
          this.form.controls['name'].setErrors({'duplicate': true});
          break;
        default:
          this.alert = toAlert(e);
      }
      this.submitButtonState = ClrLoadingState.ERROR;
    }
  }
}
