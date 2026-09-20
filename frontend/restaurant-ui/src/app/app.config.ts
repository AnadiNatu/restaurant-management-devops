import {
  ApplicationConfig,
  provideZoneChangeDetection
} from '@angular/core';

import {
  provideRouter,
  withViewTransitions
} from '@angular/router';

import {
  HttpEvent,
  HttpHandlerFn,
  HttpRequest,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';

import {
  provideAnimationsAsync
} from '@angular/platform-browser/animations/async';

import { routes } from './app.routes';
import { authInterceptor, errorInterceptor } from './core/interceptors/auth.interceptors';
import { Observable } from 'rxjs';


// export const appConfig: ApplicationConfig = {

//   providers: [

//     provideZoneChangeDetection({eventCoalescing: true}),
//     provideRouter(routes,withViewTransitions()),
//     provideHttpClient(withInterceptors([authInterceptor])),
//     provideAnimationsAsync()
//   ]
// };


export const appConfig: ApplicationConfig = {

  providers: [

    provideZoneChangeDetection({
      eventCoalescing: true
    }),

    provideRouter(
      routes,
      withViewTransitions()
    ),

    /*
     * IMPORTANT:
     *
     * Every HttpClient request will pass through:
     *
     * 1. authInterceptor
     * 2. errorInterceptor
     */
    provideHttpClient(
      withInterceptors([
        authInterceptor,
        errorInterceptor
      ])
    ),

    provideAnimationsAsync()

  ]
};

// function errorInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
//   throw new Error('Function not implemented.');
// }
