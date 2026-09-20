import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/model';

// export const authGuard: CanActivateFn = () => {
//   const auth   = inject(AuthService);
//   const router = inject(Router);

//   if (auth.isLoggedIn) return true;

//   router.navigate(['/auth/login']);
//   return false;
// };

// export const guestGuard: CanActivateFn = () => {
//   const auth   = inject(AuthService);
//   const router = inject(Router);

//   if (!auth.isLoggedIn) return true;

//   router.navigate(['/dashboard']);
//   return false;
// };

// export function roleGuard(...roles: UserRole[]): CanActivateFn {
//   return () => {
//     const auth   = inject(AuthService);
//     const router = inject(Router);

//     if (!auth.isLoggedIn) { router.navigate(['/auth/login']); return false; }
//     if (!auth.role || !roles.includes(auth.role)) { router.navigate(['/dashboard']); return false; }
//     return true;
//   };
// }


export const authGuard: CanActivateFn = () => {

  const auth = inject(AuthService);

  const router = inject(Router);


  if (auth.isLoggedIn) {
    return true;
  }


  return router.createUrlTree([
    '/auth/login'
  ]);
};



/*
|--------------------------------------------------------------------------
| GUEST GUARD
|--------------------------------------------------------------------------
|
| Used for login/register pages.
|
| Logged-in users should not return to login.
|--------------------------------------------------------------------------
*/

export const guestGuard: CanActivateFn = () => {

  const auth = inject(AuthService);

  const router = inject(Router);


  if (!auth.isLoggedIn) {
    return true;
  }


  return router.createUrlTree([
    '/dashboard'
  ]);
};



/*
|--------------------------------------------------------------------------
| ROLE GUARD
|--------------------------------------------------------------------------
*/

export function roleGuard(
  ...roles: UserRole[]
): CanActivateFn {

  return () => {

    const auth = inject(AuthService);

    const router = inject(Router);


    /*
     * Not logged in.
     */
    if (!auth.isLoggedIn) {

      return router.createUrlTree([
        '/auth/login'
      ]);
    }


    /*
     * Logged in but wrong role.
     */
    if (
      !auth.role ||
      !roles.includes(auth.role)
    ) {

      return router.createUrlTree([
        '/dashboard'
      ]);
    }


    return true;
  };
}