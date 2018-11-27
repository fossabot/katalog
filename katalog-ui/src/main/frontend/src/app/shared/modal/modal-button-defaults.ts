import {ButtonResponses, ModalButton} from "~/shared/modal/modal-button";
import {ClrLoadingState} from "@clr/angular";
import {toAlert} from "~/shared/alerts/alert";

export function cancelButton(): ModalButton {
  return {
    title: "Cancel",
    classes: ["btn-outline"],
    isDisabled: () => false,
    click: async function* () {
      yield new ButtonResponses.Done();
    }
  }
}

export function button(
  title: string,
  action: () => Promise<boolean>,
  onSuccess: () => ButtonResponses.Response,
  onFailure: () => ButtonResponses.Response = null
): ModalButton {
  return {
    title: title,
    classes: ["btn-primary"],
    isDisabled: () => false,
    click: async function* () {
      yield new ButtonResponses.LoadingState(ClrLoadingState.LOADING);

      try {
        const result = await action();
        if (result) {
          yield new ButtonResponses.LoadingState(ClrLoadingState.SUCCESS);
          yield new ButtonResponses.Delay(700);
          yield onSuccess();
        } else {
          yield new ButtonResponses.LoadingState(ClrLoadingState.ERROR);
          yield new ButtonResponses.Delay(700);
          if (onFailure) {
            yield onFailure();
          }
        }
      } catch (e) {
        yield new ButtonResponses.LoadingState(ClrLoadingState.ERROR);
        yield new ButtonResponses.UnexpectedFailure(toAlert(e));
      }
    }
  }
}
