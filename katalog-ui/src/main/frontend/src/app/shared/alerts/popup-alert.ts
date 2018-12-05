export class PopupAlert {
  isOpen: boolean;

  constructor(
    readonly title: string,
    readonly message: string
  ) {
    this.isOpen = true;
  }
}
