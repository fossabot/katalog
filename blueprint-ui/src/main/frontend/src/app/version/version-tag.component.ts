import { Component, Input } from "@angular/core";
import { Schema, Version } from "../api/model";

@Component({
  selector: 'app-version-tag',
  templateUrl: './version-tag.component.html'
})
export class VersionTagComponent {
  @Input() schema: Schema;
  @Input() version: Version;
}
