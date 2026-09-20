import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  MenuItemResponse, CreateMenuItemRequest, UpdateMenuItemRequest,
  InventoryResponse, CreateInventoryRequest, UpdateInventoryRequest, RestockRequest, CostSummaryResponse,
  OrderResponse, CreateOrderRequest, UpdateStatusRequest, BillResponse,
  CustomerResponse, CreateCustomerRequest, UpdateCustomerRequest, CustomerType,
  UserResponse, CreateUserRequest, UpdateUserRequest, UpdateRoleRequest, UserRole,
  DashboardSummary, RevenueSummary,
  ActivityLog,
  InventoryCostSummary
} from '../models/model';
import { environment } from '../../../environments/environment';

const API = 'http://localhost:8080/api';
// const API = `${environment.apiBaseUrl}/api`;

// ── Menu Service ──────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class MenuService {
  private http = inject(HttpClient);
  private BASE = `${API}/menu`;

  getAll(): Observable<MenuItemResponse[]>                { return this.http.get<MenuItemResponse[]>(`${this.BASE}/getAll`); }
  getById(id: number): Observable<MenuItemResponse>       { return this.http.get<MenuItemResponse>(`${this.BASE}/${id}`); }
  getAvailable(): Observable<MenuItemResponse[]>          { return this.http.get<MenuItemResponse[]>(`${this.BASE}/available`); }
  getByCategory(cat: string): Observable<MenuItemResponse[]> { return this.http.get<MenuItemResponse[]>(`${this.BASE}/category/${cat}`); }

  create(req: CreateMenuItemRequest): Observable<MenuItemResponse> {
    return this.http.post<MenuItemResponse>(`${this.BASE}/create`, req);
  }

  update(id: number, req: UpdateMenuItemRequest): Observable<MenuItemResponse> {
    return this.http.put<MenuItemResponse>(`${this.BASE}/${id}`, req);
  }

  toggleAvailability(id: number): Observable<MenuItemResponse> {
    return this.http.patch<MenuItemResponse>(`${this.BASE}/${id}/toggle-availability`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/${id}`);
  }
}

// ── Inventory Service ─────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private http = inject(HttpClient);
  private BASE = `${API}/inventory`;

  getAll(): Observable<InventoryResponse[]> { return this.http.get<InventoryResponse[]>(`${this.BASE}/getAll`); }
  getById(id: number): Observable<InventoryResponse> {return this.http.get<InventoryResponse>(`${this.BASE}/${id}`); }
  getLowStock(): Observable<InventoryResponse[]> {return this.http.get<InventoryResponse[]>(`${this.BASE}/low-stock`); }
  getWeeklyCost(): Observable<CostSummaryResponse> {return this.http.get<CostSummaryResponse>(`${this.BASE}/cost/weekly`); }
  getMonthlyCost(): Observable<CostSummaryResponse> { return this.http.get<CostSummaryResponse>(`${this.BASE}/cost/monthly`); }

  getCostBetween(from: string, to: string): Observable<CostSummaryResponse> {
    const params = {from, to};
    return this.http.get<CostSummaryResponse>(`${this.BASE}/between`, { params });
  }

  create(req: CreateInventoryRequest): Observable<InventoryResponse> {
    return this.http.post<InventoryResponse>(`${this.BASE}/create`, req);
  }

  update(id: number, req: UpdateInventoryRequest): Observable<InventoryResponse> {
    return this.http.put<InventoryResponse>(`${this.BASE}/${id}`, req);
  }

  restock(id: number, req: RestockRequest): Observable<InventoryResponse> {
    return this.http.patch<InventoryResponse>(`${this.BASE}/${id}/restock`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/${id}`);
  }
}

// ── Order Service ─────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class OrderService {
  private http = inject(HttpClient);
  private BASE = `${API}/orders`;

  getAll(): Observable<OrderResponse[]>{ return this.http.get<OrderResponse[]>(`${this.BASE}/admin/all`); }
  getById(id: number): Observable<OrderResponse> { return this.http.get<OrderResponse>(`${this.BASE}/${id}`); }
  getMyOrders(): Observable<OrderResponse[]> { return this.http.get<OrderResponse[]>(`${this.BASE}/my`); }
  getByStatus(status: string): Observable<OrderResponse[]> { return this.http.get<OrderResponse[]>(`${this.BASE}/status/${status}`); }
  getByCustomer(id: number): Observable<OrderResponse[]> { return this.http.get<OrderResponse[]>(`${this.BASE}/customer/${id}`); }

 create(req: CreateOrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.BASE}/create`, req);
  }

 updateStatus(id: number, req: UpdateStatusRequest): Observable<OrderResponse> {
    return this.http.patch<OrderResponse>(`${this.BASE}/${id}/status`, req);
  }

  generateBill(id: number): Observable<BillResponse> {
    return this.http.post<BillResponse>(`${this.BASE}/${id}/bill`, {});
  }

  getAllOrderDetailsAdmin(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.BASE}/admin/all`);
  }

  cancel(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/admin/${id}/cancel`);
  }
}

// ── Customer Service ──────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private http = inject(HttpClient);
  private BASE = `${API}/customers`;

  getAll(): Observable<CustomerResponse[]>                  { return this.http.get<CustomerResponse[]>(`${this.BASE}/getAll`); }
  getById(id: number): Observable<CustomerResponse>         { return this.http.get<CustomerResponse>(`${this.BASE}/${id}`); }
  getByType(t: CustomerType): Observable<CustomerResponse[]>{ return this.http.get<CustomerResponse[]>(`${this.BASE}/type/${t}`); }

  create(req: CreateCustomerRequest): Observable<CustomerResponse> {
    return this.http.post<CustomerResponse>(`${this.BASE}/create`, req);
  }

  update(id: number, req: UpdateCustomerRequest): Observable<CustomerResponse> {
    return this.http.put<CustomerResponse>(`${this.BASE}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/${id}`);
  }
}

// ── User / Employee Management ────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class UserManagementService {
  private http = inject(HttpClient);

  // Super Admin endpoints
  getAllUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${API}/super-admin/users`);
  }

  getUserById(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${API}/super-admin/users/${id}`);
  }

  getUsersByRole(role: UserRole): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${API}/super-admin/users/role/${role}`);
  }

  createUser(req: CreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${API}/super-admin/create/users`, req);
  }

  updateUser(id: number, req: UpdateUserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${API}/super-admin/users/${id}`, req);
  }

  updateRole(id: number, req: UpdateRoleRequest): Observable<UserResponse> {
    return this.http.patch<UserResponse>(`${API}/super-admin/users/${id}/role`, req);
  }

  activateUser(id: number): Observable<string> {
    return this.http.patch(`${API}/super-admin/users/${id}/activate`, {}, { responseType: 'text' });
  }

  deactivateUser(id: number): Observable<string> {
    return this.http.patch(`${API}/super-admin/users/${id}/deactivate`, {}, { responseType: 'text' });
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/super-admin/users/${id}`);
  }

  // Admin endpoints (employees only)
  getEmployees(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${API}/admin/employees`);
  }

  getEmployeeById(id : number): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${API}/admin/employees/${id}`);
  }

  createEmployee(req: CreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${API}/admin/employees`, req);
  }

  activateEmployee(id: number): Observable<string> {
    return this.http.patch(`${API}/admin/employees/${id}/activate`, {}, { responseType: 'text' });
  }

  deactivateEmployee(id: number): Observable<string> {
    return this.http.patch(`${API}/admin/employees/${id}/deactivate`, {}, { responseType: 'text' });
  }

  deleteEmployee(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/admin/employees/${id}`);
  }
}

// ── Profile Service ───────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private http = inject(HttpClient);

  getProfile(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${API}/profile`);
  }

  updateProfile(req: UpdateUserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${API}/profile`, req);
  }
}

// ── Report Service ────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class ReportService {
  private http = inject(HttpClient);
  private BASE = `${API}/reports`;

  getDashboard(): Observable<DashboardSummary>         { return this.http.get<DashboardSummary>(`${this.BASE}/dashboard`); }
  getWeeklyRevenue(): Observable<RevenueSummary>       { return this.http.get<RevenueSummary>(`${this.BASE}/revenue/weekly`); }
  getMonthlyRevenue(): Observable<RevenueSummary>      { return this.http.get<RevenueSummary>(`${this.BASE}/revenue/monthly`); }
  getWeeklyInventoryCost(): Observable<InventoryCostSummary> {return this.http.get<InventoryCostSummary>(`${this.BASE}/inventory/weekly`);}
  getMonthlyInventoryCost(): Observable<InventoryCostSummary> {return this.http.get<InventoryCostSummary>(`${this.BASE}/inventory/monthly`);}
}

// ── Activity Log Service ──────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class ActivityLogService {
  private http = inject(HttpClient);
  private BASE = `${API}/logs`;

  getAll(): Observable<ActivityLog[]>                       { return this.http.get<ActivityLog[]>(this.BASE); }
  getByActor(email: string): Observable<ActivityLog[]>      { return this.http.get<ActivityLog[]>(`${this.BASE}/actor/${email}`); }
  getByEntityType(type: string): Observable<ActivityLog[]>  { return this.http.get<ActivityLog[]>(`${this.BASE}/entity/${type}`); }

  getBetween(from: string, to: string): Observable<ActivityLog[]> {
    const params = new HttpParams()
      .set('from', from)
      .set('to', to);

    return this.http.get<ActivityLog[]>(`${this.BASE}/between`,{ params });
  }
}

// ── OTP Service ───────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class OtpService {
  private http = inject(HttpClient);

  sendOtp(email: string): Observable<string> {
    return this.http.post(`${API}/otp/send`, null, {
      params: { email },
      responseType: 'text'
    });
  }

  verifyOtp(email: string, otp: string): Observable<string> {
    return this.http.post(`${API}/otp/verify`, { email, otp }, { responseType: 'text' });
  }
}
// Need to implment S3 for the food and inventory images.

// const API =
//   `${environment.apiBaseUrl}/api`;



// /*
// |--------------------------------------------------------------------------
// | MENU
// |--------------------------------------------------------------------------
// */

// @Injectable({
//   providedIn: 'root'
// })
// export class MenuService {

//   private http = inject(HttpClient);

//   private BASE =
//     `${API}/menu`;


//   getAll():
//     Observable<MenuItemResponse[]> {

//     return this.http.get<MenuItemResponse[]>(
//       this.BASE
//     );
//   }


//   getById(
//     id: number
//   ):
//     Observable<MenuItemResponse> {

//     return this.http.get<MenuItemResponse>(
//       `${this.BASE}/${id}`
//     );
//   }


//   getAvailable():
//     Observable<MenuItemResponse[]> {

//     return this.http.get<MenuItemResponse[]>(
//       `${this.BASE}/available`
//     );
//   }


//   getByCategory(
//     cat: string
//   ):
//     Observable<MenuItemResponse[]> {

//     return this.http.get<MenuItemResponse[]>(
//       `${this.BASE}/category/${encodeURIComponent(cat)}`
//     );
//   }


//   create(
//     req: CreateMenuItemRequest
//   ):
//     Observable<MenuItemResponse> {

//     return this.http.post<MenuItemResponse>(
//       this.BASE,
//       req
//     );
//   }


//   update(
//     id: number,
//     req: UpdateMenuItemRequest
//   ):
//     Observable<MenuItemResponse> {

//     return this.http.put<MenuItemResponse>(
//       `${this.BASE}/${id}`,
//       req
//     );
//   }


//   toggleAvailability(
//     id: number
//   ):
//     Observable<MenuItemResponse> {

//     return this.http.patch<MenuItemResponse>(
//       `${this.BASE}/${id}/toggle-availability`,
//       {}
//     );
//   }


//   delete(
//     id: number
//   ):
//     Observable<void> {

//     return this.http.delete<void>(
//       `${this.BASE}/${id}`
//     );
//   }
// }



// /*
// |--------------------------------------------------------------------------
// | INVENTORY
// |--------------------------------------------------------------------------
// */

// @Injectable({
//   providedIn: 'root'
// })
// export class InventoryService {

//   private http = inject(HttpClient);

//   private BASE =
//     `${API}/inventory`;


//   getAll():
//     Observable<InventoryResponse[]> {

//     return this.http.get<InventoryResponse[]>(
//       this.BASE
//     );
//   }


//   getById(
//     id: number
//   ):
//     Observable<InventoryResponse> {

//     return this.http.get<InventoryResponse>(
//       `${this.BASE}/${id}`
//     );
//   }


//   getLowStock():
//     Observable<InventoryResponse[]> {

//     return this.http.get<InventoryResponse[]>(
//       `${this.BASE}/low-stock`
//     );
//   }


//   getWeeklyCost():
//     Observable<CostSummaryResponse> {

//     return this.http.get<CostSummaryResponse>(
//       `${this.BASE}/cost/weekly`
//     );
//   }


//   getMonthlyCost():
//     Observable<CostSummaryResponse> {

//     return this.http.get<CostSummaryResponse>(
//       `${this.BASE}/cost/monthly`
//     );
//   }


//   create(
//     req: CreateInventoryRequest
//   ):
//     Observable<InventoryResponse> {

//     return this.http.post<InventoryResponse>(
//       this.BASE,
//       req
//     );
//   }


//   update(
//     id: number,
//     req: UpdateInventoryRequest
//   ):
//     Observable<InventoryResponse> {

//     return this.http.put<InventoryResponse>(
//       `${this.BASE}/${id}`,
//       req
//     );
//   }


//   restock(
//     id: number,
//     req: RestockRequest
//   ):
//     Observable<InventoryResponse> {

//     return this.http.patch<InventoryResponse>(
//       `${this.BASE}/${id}/restock`,
//       req
//     );
//   }


//   delete(
//     id: number
//   ):
//     Observable<void> {

//     return this.http.delete<void>(
//       `${this.BASE}/${id}`
//     );
//   }
// }



// /*
// |--------------------------------------------------------------------------
// | ORDERS
// |--------------------------------------------------------------------------
// */

// @Injectable({
//   providedIn: 'root'
// })
// export class OrderService {

//   private http = inject(HttpClient);

//   private BASE =
//     `${API}/orders`;


//   getAll():
//     Observable<OrderResponse[]> {

//     return this.http.get<OrderResponse[]>(
//       `${this.BASE}/admin/all`
//     );
//   }


//   getById(
//     id: number
//   ):
//     Observable<OrderResponse> {

//     return this.http.get<OrderResponse>(
//       `${this.BASE}/${id}`
//     );
//   }


//   getMyOrders():
//     Observable<OrderResponse[]> {

//     return this.http.get<OrderResponse[]>(
//       `${this.BASE}/my`
//     );
//   }


//   getByStatus(
//     status: string
//   ):
//     Observable<OrderResponse[]> {

//     return this.http.get<OrderResponse[]>(
//       `${this.BASE}/status/${encodeURIComponent(status)}`
//     );
//   }


//   getByCustomer(
//     id: number
//   ):
//     Observable<OrderResponse[]> {

//     return this.http.get<OrderResponse[]>(
//       `${this.BASE}/customer/${id}`
//     );
//   }


//   create(
//     req: CreateOrderRequest
//   ):
//     Observable<OrderResponse> {

//     return this.http.post<OrderResponse>(
//       this.BASE,
//       req
//     );
//   }


//   updateStatus(
//     id: number,
//     req: UpdateStatusRequest
//   ):
//     Observable<OrderResponse> {

//     return this.http.patch<OrderResponse>(
//       `${this.BASE}/${id}/status`,
//       req
//     );
//   }


//   generateBill(
//     id: number
//   ):
//     Observable<BillResponse> {

//     return this.http.post<BillResponse>(
//       `${this.BASE}/${id}/bill`,
//       {}
//     );
//   }


//   cancel(
//     id: number
//   ):
//     Observable<void> {

//     return this.http.delete<void>(
//       `${this.BASE}/admin/${id}/cancel`
//     );
//   }
// }



// /*
// |--------------------------------------------------------------------------
// | CUSTOMERS
// |--------------------------------------------------------------------------
// */

// @Injectable({
//   providedIn: 'root'
// })
// export class CustomerService {

//   private http = inject(HttpClient);

//   private BASE =
//     `${API}/customers`;


//   getAll():
//     Observable<CustomerResponse[]> {

//     return this.http.get<CustomerResponse[]>(
//       this.BASE
//     );
//   }


//   getById(
//     id: number
//   ):
//     Observable<CustomerResponse> {

//     return this.http.get<CustomerResponse>(
//       `${this.BASE}/${id}`
//     );
//   }


//   getByType(
//     type: CustomerType
//   ):
//     Observable<CustomerResponse[]> {

//     return this.http.get<CustomerResponse[]>(
//       `${this.BASE}/type/${type}`
//     );
//   }


//   create(
//     req: CreateCustomerRequest
//   ):
//     Observable<CustomerResponse> {

//     return this.http.post<CustomerResponse>(
//       this.BASE,
//       req
//     );
//   }


//   update(
//     id: number,
//     req: UpdateCustomerRequest
//   ):
//     Observable<CustomerResponse> {

//     return this.http.put<CustomerResponse>(
//       `${this.BASE}/${id}`,
//       req
//     );
//   }


//   delete(
//     id: number
//   ):
//     Observable<void> {

//     return this.http.delete<void>(
//       `${this.BASE}/${id}`
//     );
//   }
// }



// /*
// |--------------------------------------------------------------------------
// | USER / EMPLOYEE MANAGEMENT
// |--------------------------------------------------------------------------
// */

// @Injectable({
//   providedIn: 'root'
// })
// export class UserManagementService {

//   private http = inject(HttpClient);


//   /*
//    * SUPER ADMIN
//    */

//   getAllUsers():
//     Observable<UserResponse[]> {

//     return this.http.get<UserResponse[]>(
//       `${API}/super-admin/users`
//     );
//   }


//   getUserById(
//     id: number
//   ):
//     Observable<UserResponse> {

//     return this.http.get<UserResponse>(
//       `${API}/super-admin/users/${id}`
//     );
//   }


//   getUsersByRole(
//     role: UserRole
//   ):
//     Observable<UserResponse[]> {

//     return this.http.get<UserResponse[]>(
//       `${API}/super-admin/users/role/${role}`
//     );
//   }


//   createUser(
//     req: CreateUserRequest
//   ):
//     Observable<UserResponse> {

//     return this.http.post<UserResponse>(
//       `${API}/super-admin/users`,
//       req
//     );
//   }


//   updateUser(
//     id: number,
//     req: UpdateUserRequest
//   ):
//     Observable<UserResponse> {

//     return this.http.put<UserResponse>(
//       `${API}/super-admin/users/${id}`,
//       req
//     );
//   }


//   updateRole(
//     id: number,
//     req: UpdateRoleRequest
//   ):
//     Observable<UserResponse> {

//     return this.http.patch<UserResponse>(
//       `${API}/super-admin/users/${id}/role`,
//       req
//     );
//   }


//   activateUser(
//     id: number
//   ):
//     Observable<string> {

//     return this.http.patch(
//       `${API}/super-admin/users/${id}/activate`,
//       {},
//       {
//         responseType: 'text'
//       }
//     );
//   }


//   deactivateUser(
//     id: number
//   ):
//     Observable<string> {

//     return this.http.patch(
//       `${API}/super-admin/users/${id}/deactivate`,
//       {},
//       {
//         responseType: 'text'
//       }
//     );
//   }


//   deleteUser(
//     id: number
//   ):
//     Observable<void> {

//     return this.http.delete<void>(
//       `${API}/super-admin/users/${id}`
//     );
//   }


//   /*
//    * ADMIN
//    */

//   getEmployees():
//     Observable<UserResponse[]> {

//     return this.http.get<UserResponse[]>(
//       `${API}/admin/employees`
//     );
//   }


//   createEmployee(
//     req: CreateUserRequest
//   ):
//     Observable<UserResponse> {

//     return this.http.post<UserResponse>(
//       `${API}/admin/employees`,
//       req
//     );
//   }


//   activateEmployee(
//     id: number
//   ):
//     Observable<string> {

//     return this.http.patch(
//       `${API}/admin/employees/${id}/activate`,
//       {},
//       {
//         responseType: 'text'
//       }
//     );
//   }


//   deactivateEmployee(
//     id: number
//   ):
//     Observable<string> {

//     return this.http.patch(
//       `${API}/admin/employees/${id}/deactivate`,
//       {},
//       {
//         responseType: 'text'
//       }
//     );
//   }


//   deleteEmployee(
//     id: number
//   ):
//     Observable<void> {

//     return this.http.delete<void>(
//       `${API}/admin/employees/${id}`
//     );
//   }
// }



// /*
// |--------------------------------------------------------------------------
// | PROFILE
// |--------------------------------------------------------------------------
// */

// @Injectable({
//   providedIn: 'root'
// })
// export class ProfileService {

//   private http = inject(HttpClient);


//   getProfile():
//     Observable<UserResponse> {

//     return this.http.get<UserResponse>(
//       `${API}/profile`
//     );
//   }


//   updateProfile(
//     req: UpdateUserRequest
//   ):
//     Observable<UserResponse> {

//     return this.http.put<UserResponse>(
//       `${API}/profile`,
//       req
//     );
//   }
// }



// /*
// |--------------------------------------------------------------------------
// | REPORTS
// |--------------------------------------------------------------------------
// */

// @Injectable({
//   providedIn: 'root'
// })
// export class ReportService {

//   private http = inject(HttpClient);

//   private BASE =
//     `${API}/reports`;


//   getDashboard():
//     Observable<DashboardSummary> {

//     return this.http.get<DashboardSummary>(
//       `${this.BASE}/dashboard`
//     );
//   }


//   getWeeklyRevenue():
//     Observable<RevenueSummary> {

//     return this.http.get<RevenueSummary>(
//       `${this.BASE}/revenue/weekly`
//     );
//   }


//   getMonthlyRevenue():
//     Observable<RevenueSummary> {

//     return this.http.get<RevenueSummary>(
//       `${this.BASE}/revenue/monthly`
//     );
//   }
// }



// /*
// |--------------------------------------------------------------------------
// | ACTIVITY LOGS
// |--------------------------------------------------------------------------
// */

// @Injectable({
//   providedIn: 'root'
// })
// export class ActivityLogService {

//   private http = inject(HttpClient);

//   private BASE =
//     `${API}/logs`;


//   getAll():
//     Observable<ActivityLog[]> {

//     return this.http.get<ActivityLog[]>(
//       this.BASE
//     );
//   }


//   getByActor(
//     email: string
//   ):
//     Observable<ActivityLog[]> {

//     return this.http.get<ActivityLog[]>(
//       `${this.BASE}/actor/${encodeURIComponent(email)}`
//     );
//   }


//   getByEntityType(
//     type: string
//   ):
//     Observable<ActivityLog[]> {

//     return this.http.get<ActivityLog[]>(
//       `${this.BASE}/entity/${encodeURIComponent(type)}`
//     );
//   }
// }



// /*
// |--------------------------------------------------------------------------
// | OTP
// |--------------------------------------------------------------------------
// */

// @Injectable({
//   providedIn: 'root'
// })
// export class OtpService {

//   private http = inject(HttpClient);


//   sendOtp(
//     email: string
//   ):
//     Observable<string> {

//     return this.http.post(
//       `${API}/otp/send`,
//       null,
//       {
//         params: {
//           email
//         },
//         responseType: 'text'
//       }
//     );
//   }


//   verifyOtp(
//     email: string,
//     otp: string
//   ):
//     Observable<string> {

//     return this.http.post(
//       `${API}/otp/verify`,
//       {
//         email,
//         otp
//       },
//       {
//         responseType: 'text'
//       }
//     );
//   }
// }