export class ApplicationError extends Error {
  constructor(public message: string) {
    super(message);
    Object.setPrototypeOf(this, new.target.prototype);
  }
}
