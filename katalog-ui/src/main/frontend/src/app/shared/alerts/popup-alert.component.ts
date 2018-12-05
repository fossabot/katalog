import {Component, EventEmitter, Input, Output} from "@angular/core";
import {PopupAlert} from "~/shared/alerts/popup-alert";

@Component({
  selector: 'app-popup-alert',
  templateUrl: './popup-alert.component.html'
})
export class PopupAlertComponent {
  @Input() popup: PopupAlert;
  @Output() onClose = new EventEmitter();
}
