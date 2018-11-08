import {NgModule} from '@angular/core';
import {ICONS} from './icons';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';

@NgModule({
  imports: [
    FontAwesomeModule
  ],
  entryComponents: ICONS,
  declarations: ICONS,
  exports: ICONS
})
export class IconsModule {
}
