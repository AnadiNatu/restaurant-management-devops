import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import {
  AuthResponse, LoginRequest, RegisterRequest,
  ForgotPasswordRequest, ResetPasswordRequest,
  ChangePasswordRequest, UserRole
} from '../models/model';
import { environment } from '../../../environments/environment';

// const BASE = `${environment.apiBaseUrl}/api/auth`;
// // const BASE = 'http://localhost:8080/api/auth';
// const TOKEN_KEY = 'rms_token';
// const REFRESH_KEY = 'rms_refresh';
// const USER_KEY = 'rms_user';

// @Injectable({ providedIn: 'root' })
// export class AuthService {
//   // [x: string]: any;

//   private http   = inject(HttpClient);
//   private router = inject(Router);

//   private _user$ = new BehaviorSubject<AuthResponse | null>(this.loadUser());
//   readonly user$ = this._user$.asObservable();

//   // ── Getters ──────────────────────────────────────────────

//   get currentUser(): AuthResponse | null { return this._user$.value; }
//   get token(): string | null { return localStorage.getItem(TOKEN_KEY); }
//   get isLoggedIn(): boolean { return !!this.token && !!this.currentUser; }
//   get role(): UserRole | null { return this.currentUser?.role ?? null; }

//   isSuperAdmin(): boolean { return this.role === 'SUPER_ADMIN'; }
//   isAdmin(): boolean      { return this.role === 'ADMIN' || this.role === 'SUPER_ADMIN'; }
//   isEmployee(): boolean   { return this.role === 'EMPLOYEE'; }

//   // ── API Calls ─────────────────────────────────────────────

//   register(req: RegisterRequest): Observable<AuthResponse> {
//     return this.http.post<AuthResponse>(`${BASE}/register`, req)
//       .pipe(tap(res => this.saveSession(res)));
//   }

//   login(req: LoginRequest): Observable<AuthResponse> {
//     console.log('🔥 Calling AuthService.login()');
//     return this.http.post<AuthResponse>(`${BASE}/login`, req)
//       .pipe(tap(res => this.saveSession(res)));
//   }

//   forgotPassword(req: ForgotPasswordRequest): Observable<string> {
//     return this.http.post(`${BASE}/forgot-password`, req, { responseType: 'text' });
//   }

//   resetPassword(req: ResetPasswordRequest): Observable<string> {
//     return this.http.post(`${BASE}/reset-password`, req, { responseType: 'text' });
//   }

//   changePassword(req: ChangePasswordRequest): Observable<string> {
//     return this.http.post(`${BASE}/change-password`, req, { responseType: 'text' });
//   }

//   refreshToken(): Observable<AuthResponse> {
//     const refreshToken = localStorage.getItem(REFRESH_KEY) ?? '';
//     return this.http.post<AuthResponse>(`${BASE}/refresh`, null, {
//       params: { refreshToken }
//     }).pipe(tap(res => this.saveSession(res)));
//   }

//   // ── Session ───────────────────────────────────────────────

//   private saveSession(res: AuthResponse): void {
//     localStorage.setItem(TOKEN_KEY, res.token);
//     localStorage.setItem(REFRESH_KEY, res.refreshToken);
//     localStorage.setItem(USER_KEY, JSON.stringify(res));
//     this._user$.next(res);
//   }

//   private loadUser(): AuthResponse | null {
//     const raw = localStorage.getItem(USER_KEY);
//     return raw ? JSON.parse(raw) : null;
//   }

//   logout(): void {
//     localStorage.removeItem(TOKEN_KEY);
//     localStorage.removeItem(REFRESH_KEY);
//     localStorage.removeItem(USER_KEY);
//     this._user$.next(null);
//     this.router.navigate(['/auth/login']);
//   }

//   clearSession(): void {
//   localStorage.removeItem('rms_token');
//   localStorage.removeItem('rms_refresh');
//   localStorage.removeItem('rms_user');

//   this._user$.next(null);
// }
// }


const BASE = `${environment.apiBaseUrl}/api/auth`;


/*
|--------------------------------------------------------------------------
| SESSION STORAGE KEYS
|--------------------------------------------------------------------------
*/

const TOKEN_KEY = 'rms_token';
const REFRESH_KEY = 'rms_refresh';
const USER_KEY = 'rms_user';



@Injectable({
  providedIn: 'root'
})
export class AuthService {


  private http = inject(HttpClient);

  private router = inject(Router);


  /*
   * Current logged-in user.
   */
  private _user$ =
    new BehaviorSubject<AuthResponse | null>(
      this.loadUser()
    );


  readonly user$ =
    this._user$.asObservable();



  /*
  |--------------------------------------------------------------------------
  | GETTERS
  |--------------------------------------------------------------------------
  */

  get currentUser(): AuthResponse | null {
    return this._user$.value;
  }


  get token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }


  get isLoggedIn(): boolean {

    return (
      !!this.token &&
      !!this.currentUser
    );
  }


  get role(): UserRole | null {

    return this.currentUser?.role ?? null;
  }



  /*
  |--------------------------------------------------------------------------
  | ROLE HELPERS
  |--------------------------------------------------------------------------
  */

  isSuperAdmin(): boolean {

    return this.role === 'SUPER_ADMIN';
  }


  isAdmin(): boolean {

    return (
      this.role === 'ADMIN' ||
      this.role === 'SUPER_ADMIN'
    );
  }


  isEmployee(): boolean {

    return this.role === 'EMPLOYEE';
  }



  /*
  |--------------------------------------------------------------------------
  | AUTH API
  |--------------------------------------------------------------------------
  */

  register(
    req: RegisterRequest
  ): Observable<AuthResponse> {

    return this.http
      .post<AuthResponse>(
        `${BASE}/register`,
        req
      )
      .pipe(
        tap(response =>
          this.saveSession(response)
        )
      );
  }



  login(
    req: LoginRequest
  ): Observable<AuthResponse> {

    console.log(
      '🔥 Calling AuthService.login()'
    );

    console.log(
      '🔥 Login URL:',
      `${BASE}/login`
    );


    return this.http
      .post<AuthResponse>(
        `${BASE}/login`,
        req
      )
      .pipe(
        tap(response => {

          console.log(
            '🔥 LOGIN RESPONSE RECEIVED'
          );

          this.saveSession(response);

        })
      );
  }



  forgotPassword(
    req: ForgotPasswordRequest
  ): Observable<string> {

    return this.http.post(
      `${BASE}/forgot-password`,
      req,
      {
        responseType: 'text'
      }
    );
  }



  resetPassword(
    req: ResetPasswordRequest
  ): Observable<string> {

    return this.http.post(
      `${BASE}/reset-password`,
      req,
      {
        responseType: 'text'
      }
    );
  }



  changePassword(
    req: ChangePasswordRequest
  ): Observable<string> {

    return this.http.post(
      `${BASE}/change-password`,
      req,
      {
        responseType: 'text'
      }
    );
  }



  refreshToken(): Observable<AuthResponse> {

    const refreshToken =
      localStorage.getItem(REFRESH_KEY) ?? '';


    return this.http
      .post<AuthResponse>(
        `${BASE}/refresh`,
        null,
        {
          params: {
            refreshToken
          }
        }
      )
      .pipe(
        tap(response =>
          this.saveSession(response)
        )
      );
  }



  /*
  |--------------------------------------------------------------------------
  | SESSION
  |--------------------------------------------------------------------------
  */

  private saveSession(
    response: AuthResponse
  ): void {

    localStorage.setItem(
      TOKEN_KEY,
      response.token
    );


    localStorage.setItem(
      REFRESH_KEY,
      response.refreshToken
    );


    localStorage.setItem(
      USER_KEY,
      JSON.stringify(response)
    );


    this._user$.next(response);


    console.log(
      '🔥 Session saved successfully'
    );

    console.log(
      '🔥 Role:',
      response.role
    );
  }



  private loadUser(): AuthResponse | null {

    const raw =
      localStorage.getItem(USER_KEY);


    if (!raw) {
      return null;
    }


    try {

      return JSON.parse(raw) as AuthResponse;

    } catch (error) {

      console.error(
        'Invalid stored user session:',
        error
      );


      localStorage.removeItem(USER_KEY);

      return null;
    }
  }



  /*
  |--------------------------------------------------------------------------
  | LOGOUT
  |--------------------------------------------------------------------------
  */

  logout(): void {

    this.clearSession();

    this.router.navigate([
      '/auth/login'
    ]);
  }



  /*
  |--------------------------------------------------------------------------
  | CLEAR SESSION
  |--------------------------------------------------------------------------
  */

  clearSession(): void {

    localStorage.removeItem(
      TOKEN_KEY
    );

    localStorage.removeItem(
      REFRESH_KEY
    );

    localStorage.removeItem(
      USER_KEY
    );


    this._user$.next(null);
  }
}