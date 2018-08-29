import {animate, keyframes, style} from '@angular/animations';

export const animationFailure = [
  animate(300, keyframes([
    style({transform: 'translateX(0)', offset: 0}),
    style({transform: 'translateX(-20%)', offset: 0.333}),
    style({transform: 'translateX(20%)', offset: 0.666}),
    style({transform: 'translateX(0)', offset: 1.0})
  ]))
];
