import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserService} from '~/shared/auth/user.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (UserService.token && !req.headers.has('X-AUTH-TOKEN')) {
      req = req.clone({
        headers: req.headers.append('X-AUTH-TOKEN', UserService.token)
      });
    }

    return next.handle(req);
  }
}
