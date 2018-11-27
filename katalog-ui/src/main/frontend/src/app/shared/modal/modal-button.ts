import {Alert} from "~/shared/alerts/alert";
import {ClrLoadingState} from "@clr/angular";

export interface ModalButton {
  title: string;
  classes: string[];
  isDisabled: () => boolean;
  click: () => AsyncIterable<ButtonResponses.Response>;
}

export namespace ButtonResponses {
  export abstract class Response {
  }

  export class Done implements Response {
  }

  export class UnexpectedFailure implements Response {
    constructor(public alert: Alert) {
    }
  }

  export class Route implements Response {
    constructor(public commands: any[]) {
    }
  }

  export class LoadingState implements Response {
    constructor(public state: ClrLoadingState) {
    }
  }

  export class Delay implements Response {
    constructor(public delayMs: number) {
    }
  }
}
