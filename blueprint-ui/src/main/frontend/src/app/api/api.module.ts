import {NgModule} from '@angular/core';
import {NamespaceService} from './namespace.service';
import {BrowseService} from './browse.service';
import {SchemaService} from './schema.service';

@NgModule({
  providers: [
    BrowseService,
    NamespaceService,
    SchemaService
  ]
})
export class ApiModule {
}
