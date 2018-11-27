import {Component, Input} from "@angular/core";
import {ClrLoadingState} from "@clr/angular";
import {Alert} from "~/shared/alerts/alert";
import {Modal} from "~/shared/modal/modal";
import {Router} from "@angular/router";
import {ApplicationError} from "~/application-error";
import {ButtonResponses, ModalButton} from "~/shared/modal/modal-button";

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html'
})
export class ModalComponent {
  @Input() modal: Modal;

  isOpen: boolean;
  alerts: Alert[] = [];
  loadingState = new Map<ModalButton, ClrLoadingState>();

  constructor(
    private router: Router
  ) {
  }

  public open() {
    this.isOpen = true;
  }

  // Form is blocked if any of the buttons is 'loading'
  public isBlocked() {
    for (let state of Array.from(this.loadingState.values())) {
      if (state === ClrLoadingState.LOADING) {
        return true;
      }
    }

    return false;
  }


  public close() {
    if (this.isBlocked()) {
      return;
    } else {
      this.isOpen = false;
    }
  }

  public getClasses(button: ModalButton): string[] {
    return button.classes;
  }

  public getLoadingState(button: ModalButton) {
    return this.loadingState.get(button);
  }

  public async click(button: ModalButton) {
    if (this.isBlocked()) return;
    this.alerts = [];
    for await (const response of button.click()) {
      await this.handleResponse(button, response);
    }
  }

  private async handleResponse(button: ModalButton, response: ButtonResponses.Response) {
    if (response instanceof ButtonResponses.Done) {
      this.close();
      return;
    }

    if (response instanceof ButtonResponses.UnexpectedFailure) {
      this.alerts = [response.alert];
      return;
    }

    if (response instanceof ButtonResponses.Route) {
      await this.router.navigate(response.commands);
      return;
    }

    if (response instanceof ButtonResponses.LoadingState) {
      this.loadingState.set(button, response.state);
      return;
    }

    if (response instanceof ButtonResponses.Delay) {
      await new Promise<void>(resolve => {
        setTimeout(resolve, response.delayMs);
      });
      return;
    }

    console.dir(response);
    throw new ApplicationError(`Unknown button response type: ${response}`)
  }

  public async defaultAction() {
    await this.click(this.modal.defaultButton);
  }
}
