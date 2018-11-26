import {Component} from "@angular/core";
import {ClrLoadingState} from "@clr/angular";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {ApiService} from "~/shared/api/api.service";
import {Alert, toAlert} from "~/shared/alerts/alert";
import {NavigationService} from "~/shared/navigation/navigation.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-modal-create-namespace',
  templateUrl: './modal-create-namespace.component.html'
})
export class ModalCreateNamespaceComponent {
  isOpen: boolean;
  submitButtonState = ClrLoadingState.DEFAULT;
  alert: Alert;

  form = new FormGroup({
    name: new FormControl('', Validators.required),
  });

  constructor(
    private api: ApiService,
    private navigation: NavigationService,
    private router: Router
  ) {
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
    const newNamespace = this.form.controls['name'].value;

    this.submitButtonState = ClrLoadingState.LOADING;
    this.alert = null;

    try {
      await this.api.createNamespace(newNamespace);

      this.submitButtonState = ClrLoadingState.SUCCESS;
      setTimeout(async () => {
        const redirectUrl = this.navigation.getNamespacesLinkByName(newNamespace);
        await this.router.navigate(redirectUrl);
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
