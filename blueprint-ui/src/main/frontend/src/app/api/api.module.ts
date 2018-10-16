import {NgModule} from '@angular/core';
import {NamespaceService} from './namespace.service';
import {BrowseService} from './browse.service';

@NgModule({
  providers: [
    BrowseService,
    NamespaceService
  ]
})
export class ApiModule {
}
