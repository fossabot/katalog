import {Injectable} from "@angular/core";
import {Alert} from "~/shared/alerts/alert";

@Injectable({
  providedIn: 'root'
})
export class GlobalAlertService {
  public alerts: Alert[] = [];
}
