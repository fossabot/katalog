import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest,} from "@angular/common/http";
import {EMPTY, Observable, throwError} from "rxjs";
import {catchError} from "rxjs/operators";
import {Router} from "@angular/router";

export class ApplicationStartingInterceptor implements HttpInterceptor {
  constructor(
    private router: Router
  ) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        // Did we get the 'starting' header and status code?
        if (error.status == 503 && error.headers.has("X-Katalog-Starting")) {
          this.router.navigate(['/starting-up']);
          return EMPTY;
        }
        return throwError(error);
      })
    );
  }
}
