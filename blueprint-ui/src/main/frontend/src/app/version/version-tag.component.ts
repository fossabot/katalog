import { Component, Input } from "@angular/core";
import { Version } from "../api/model";

@Component({
  selector: 'app-version-tag',
  templateUrl: './version-tag.component.html'
})
export class VersionTagComponent {
  @Input() version: Version;
}
