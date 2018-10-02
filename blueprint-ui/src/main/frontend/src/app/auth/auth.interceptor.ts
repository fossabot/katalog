import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AuthService} from './auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (this.auth.token && !req.headers.has('X-AUTH-TOKEN')) {
      const authReq = req.clone({
        headers: req.headers.append('X-AUTH-TOKEN', this.auth.token)
      });
      return next.handle(authReq);
    } else {
      return next.handle(req);
    }
  }
}
