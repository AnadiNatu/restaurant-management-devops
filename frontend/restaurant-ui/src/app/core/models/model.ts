// ── Auth ───────────────────────────────────────────────────

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fname: string;
  lname: string;
  email: string;
  password: string;
  phoneNumber?: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  email: string;
  fullName: string;
  role: UserRole;
  userId: number;
}

export interface ForgotPasswordRequest { email: string; }
export interface ResetPasswordRequest  { token: string; newPassword: string; }
export interface ChangePasswordRequest { currentPassword: string; newPassword: string; }

// ── User / Profile ─────────────────────────────────────────

export type UserRole = 'SUPER_ADMIN' | 'ADMIN' | 'EMPLOYEE';

export interface UserResponse {
  id: number;
  fname: string;
  lname: string;
  email: string;
  phoneNumber?: string;
  role: UserRole;
  isActive: boolean;
  profilePicture?: string;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateUserRequest {
  fname: string;
  lname: string;
  email: string;
  password: string;
  phoneNumber?: string;
  role: UserRole;
}

export interface UpdateUserRequest {
  fname?: string;
  lname?: string;
  phoneNumber?: string;
  isActive?: boolean;
}

export interface UpdateRoleRequest { role: UserRole; }

// ── Menu ──────────────────────────────────────────────────

export interface MenuItemResponse {
  id: number;
  name: string;
  description?: string;
  price: number;
  category: string;
  isAvailable: boolean;
  imageUrl?: string;
  taxCategory?: string;
  taxRate: number;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface CreateMenuItemRequest {
  name: string;
  description?: string;
  price: number;
  category: string;
  isAvailable?: boolean;
  imageUrl?: string;
  taxCategory?: string;
  taxRate?: number;
}

export interface UpdateMenuItemRequest extends Partial<CreateMenuItemRequest> {}

// ── Inventory ─────────────────────────────────────────────

export interface InventoryResponse {
  id: number;
  name: string;
  unit: string;
  quantity: number;
  minThreshold: number;
  costPerUnit: number;
  totalValue: number;
  category?: string;
  supplier?: string;
  isLowStock: boolean;
  lastRestockedAt?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface CreateInventoryRequest {
  name: string;
  unit: string;
  quantity: number;
  minThreshold: number;
  costPerUnit: number;
  category?: string;
  supplier?: string;
}

export interface UpdateInventoryRequest extends Partial<CreateInventoryRequest> {}

export interface RestockRequest {
  quantity: number;
  supplier?: string;
}

export interface CostSummaryResponse {
  totalCost: number;
  from: string;
  to: string;
  period: string;
  totalItems?: number;
  lowStockCount?: number;
}

// ── Orders ────────────────────────────────────────────────

export type OrderStatus =
  'PENDING' | 'CONFIRMED' | 'PREPARING' | 'READY' | 'SERVED' | 'BILLED' | 'CANCELLED';

export interface OrderItemRequest {
  menuItemId: number;
  quantity: number;
}

export interface CreateOrderRequest {
  customerId?: number;
  customerName?: string;
  customerEmail?: string;
  tableNumber?: string;
  items: OrderItemRequest[];
  discountAmount?: number;
  notes?: string;
}

export interface UpdateStatusRequest {
  status: OrderStatus;
  notes?: string;
}

export interface OrderItemResponse {
  id: number;
  menuItemId: number;
  menuItemName: string;
  quantity: number;
  unitPrice: number;
  taxRate: number;
  subtotal: number;
}

export interface OrderResponse {
  id: number;
  customerId?: number;
  customerName?: string;
  customerEmail?: string;
  tableNumber?: string;
  status: OrderStatus;
  items: OrderItemResponse[];
  subtotal: number;
  taxAmount: number;
  discountAmount: number;
  totalAmount: number;
  notes?: string;
  handledBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface BillResponse {
  orderId: number;
  customerName?: string;
  customerEmail?: string;
  tableNumber?: string;
  items: OrderItemResponse[];
  subtotal: number;
  taxAmount: number;
  discountAmount: number;
  totalAmount: number;
  handledBy?: string;
  billedAt: string;
  invoiceNumber: string;
}

// ── Customers ─────────────────────────────────────────────

export type CustomerType = 'ORDER' | 'WALK_IN' | 'DINE_IN';

export interface CustomerResponse {
  id: number;
  name: string;
  email?: string;
  phone?: string;
  customerType: CustomerType;
  address?: string;
  gstNumber?: string;
  totalVisits: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateCustomerRequest {
  name: string;
  email?: string;
  phone?: string;
  customerType?: CustomerType;
  address?: string;
  gstNumber?: string;
}

export interface UpdateCustomerRequest extends Partial<CreateCustomerRequest> {}

// ── Reports ───────────────────────────────────────────────

export interface DashboardSummary {
  todayRevenue: number;
  todayOrders: number;
  pendingOrders: number;
  totalCustomers: number;
  lowStockItems: number;
  activeMenuItems: number;
  totalEmployees: number;
}

export interface RevenueSummary {
  totalRevenue: number;
  grossSales:number;
  totalDiscount?: number;
  taxableSales:number;
  totalTax?: number;
  totalCgst?:number;
  totalSgst?:number;
  totalIgst?:number;
  totalInvoiceValue?:number;
  totalOrders: number;
  from: string;
  to: string;
  period: string;
}

export interface InventoryCostSummary {
  totalCost: number;
  totalItems: number;
  lowStockCount: number;
  from: string;
  to: string;
  period: string;
}

// ── Activity Logs ─────────────────────────────────────────

export interface ActivityLog {
  id: number;
  actorEmail: string;
  actorRole: UserRole;
  action: string;
  entityType?: string;
  entityId?: string;
  description?: string;
  ipAddress?: string;
  timestamp: string;
}

// ── Generic ───────────────────────────────────────────────

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

export interface PageMeta {
  page: number;
  size: number;
  total: number;
}