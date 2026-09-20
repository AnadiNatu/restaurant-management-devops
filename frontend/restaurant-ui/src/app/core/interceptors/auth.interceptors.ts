import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';
import { Router } from '@angular/router';

// export const authInterceptor: HttpInterceptorFn = (req, next) => {
//   const auth  = inject(AuthService);
//   const token = auth.token;

//   const authReq = token
//     ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
//     : req;

//   return next(authReq);
// };

// export const errorInterceptor: HttpInterceptorFn = (req, next) => {
//   const toast  = inject(ToastService);
//   const auth   = inject(AuthService);
//   const router = inject(Router);

//   return next(req).pipe(
//     catchError((err: HttpErrorResponse) => {
//       if (err.status === 401) {
//         auth.logout();
//         toast.error('Session expired. Please log in again.');
//       } else if (err.status === 403) {
//         toast.error('You do not have permission to perform this action.');
//         router.navigate(['/dashboard']);
//       } else if (err.status === 404) {
//         toast.error(err.error?.message || 'Resource not found.');
//       } else if (err.status === 409) {
//         toast.error(err.error?.message || 'A conflict occurred.');
//       } else if (err.status >= 500) {
//         toast.error('Server error. Please try again later.');
//       }
//       return throwError(() => err);
//     })
//   );
// };

// ─────────────────────────────────────────────
// Authentication Interceptor
// ─────────────────────────────────────────────
export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const auth = inject(AuthService);

  const token = auth.token;


  /*
   * Authentication endpoints do not need the access token.
   */
  const isPublicRequest =
    req.url.includes('/api/auth/login') ||
    req.url.includes('/api/auth/register') ||
    req.url.includes('/api/auth/forgot-password') ||
    req.url.includes('/api/auth/reset-password') ||
    req.url.includes('/api/auth/refresh') ||
    req.url.includes('/api/otp/');


  /*
   * Public request.
   *
   * Send it exactly as the component/service created it.
   */
  if (isPublicRequest) {
    return next(req);
  }


  /*
   * No JWT available.
   *
   * Do NOT block the request here.
   *
   * Let Spring Security decide whether the endpoint is public
   * or protected.
   */
  if (!token) {
    return next(req);
  }


  /*
   * Protected request.
   *
   * Add JWT.
   */
  const authenticatedRequest = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });


  console.log(
    '[AUTH INTERCEPTOR] JWT attached:',
    authenticatedRequest.method,
    authenticatedRequest.url
  );


  return next(authenticatedRequest);
};



/*
|--------------------------------------------------------------------------
| ERROR INTERCEPTOR
|--------------------------------------------------------------------------
*/

export const errorInterceptor: HttpInterceptorFn = (req, next) => {

  const toast = inject(ToastService);
  const auth = inject(AuthService);
  const router = inject(Router);


  return next(req).pipe(

    catchError((err: HttpErrorResponse) => {

      console.error(
        '[HTTP ERROR]',
        err.status,
        err.url,
        err.error
      );


      /*
       * 401
       *
       * JWT is missing, invalid, or expired.
       */
      if (err.status === 401) {

        auth.clearSession();

        toast.error(
          'Session expired. Please log in again.'
        );

        router.navigate(['/auth/login']);
      }


      /*
       * 403
       *
       * User is authenticated but does not have
       * sufficient authority.
       */
      else if (err.status === 403) {

        toast.error(
          'You do not have permission to perform this action.'
        );

        /*
         * Do not logout the user.
         *
         * A 403 means the JWT was accepted but the
         * requested resource is forbidden.
         */
      }


      /*
       * 404
       */
      else if (err.status === 404) {

        toast.error(
          err.error?.message ||
          'Resource not found.'
        );
      }


      /*
       * 409
       */
      else if (err.status === 409) {

        toast.error(
          err.error?.message ||
          'A conflict occurred.'
        );
      }


      /*
       * 500+
       */
      else if (err.status >= 500) {

        toast.error(
          'Server error. Please try again later.'
        );
      }


      return throwError(() => err);
    })
  );
};