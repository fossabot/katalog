import {HttpErrorResponse} from "@angular/common/http";

export interface AlertAction {
  label: string;
  click: () => Promise<boolean>
}

export interface Alert {
  message: string,
  isClosable: boolean,
  type: "info" | "warning" | "success" | "danger",
  icon?: string,
  button?: AlertAction,
  dropdowns?: AlertAction[]
}

export const ALERT_UNAUTHORIZED: Alert = {
  message: "You are not logged in or you are not allowed to perform this request.",
  isClosable: true,
  type: "danger"
};

export const ALERT_UNKNOWN: Alert = {
  message: "There was an unknown error when processing your request. Please try again later.",
  isClosable: true,
  type: "danger"
};

export function toAlert(e: HttpErrorResponse): Alert {
  switch (e.status) {
    case 401:
      return ALERT_UNAUTHORIZED;
    default:
      return ALERT_UNKNOWN;
  }
}
