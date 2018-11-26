import {Directive, ElementRef, Input, OnChanges, SimpleChanges} from "@angular/core";

@Directive({
  selector: '[appAutoFocus]'
})
export class AutofocusDirective implements OnChanges {
  @Input('appAutoFocus') shouldTrigger: boolean;

  constructor(private el: ElementRef) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.shouldTrigger) {
      window.setTimeout(() => {
        this.el.nativeElement.focus();
      }, 0);
    }
  }
}
