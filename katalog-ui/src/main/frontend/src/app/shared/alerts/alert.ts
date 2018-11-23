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
