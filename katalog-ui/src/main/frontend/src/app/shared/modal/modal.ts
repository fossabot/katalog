import {ModalButton} from "~/shared/modal/modal-button";

export interface Modal {
  title: string;
  buttons: ModalButton[];
  defaultButton: ModalButton
}
