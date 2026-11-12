export type RoleName = 
  | 'ADMIN' 
  | 'PROCUREMENT_MANAGER' 
  | 'PROCUREMENT_OFFICER' 
  | 'INVENTORY_MANAGER' 
  | 'APPROVER' 
  | 'EMPLOYEE';

export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  department: string;
  roles: RoleName[];
  active: boolean;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  tokenType: string;
  userId: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  department?: string;
  roles: RoleName[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface Category {
  id: number;
  name: string;
  description: string;
  active: boolean;
}

export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string;
  categoryId: number;
  categoryName: string;
  unitOfMeasurement: string;
  unitPrice: number;
  reorderThreshold: number;
  active: boolean;
  totalStock: number;
  createdAt: string;
  updatedAt: string;
}

export interface Warehouse {
  id: number;
  name: string;
  location: string;
  description: string;
  active: boolean;
  createdAt: string;
}

export interface StockItem {
  id: number;
  productId: number;
  productSku: string;
  productName: string;
  categoryName: string;
  unitOfMeasurement: string;
  unitPrice: number;
  warehouseId: number;
  warehouseName: string;
  warehouseLocation: string;
  quantity: number;
  totalValue: number;
  lowStock: boolean;
  updatedAt: string;
}

export type StockMovementType = 
  | 'INBOUND_RECEIPT' 
  | 'OUTBOUND_DISPATCH' 
  | 'ADJUSTMENT_ADD' 
  | 'ADJUSTMENT_SUBTRACT' 
  | 'INITIAL_STOCK';

export interface StockMovement {
  id: number;
  productId: number;
  productSku: string;
  productName: string;
  warehouseId: number;
  warehouseName: string;
  movementType: StockMovementType;
  quantity: number;
  previousQuantity: number;
  newQuantity: number;
  referenceNumber: string;
  reason: string;
  createdBy: string;
  createdAt: string;
}

export type SupplierStatus = 'ACTIVE' | 'INACTIVE' | 'BLACKLISTED';

export interface Supplier {
  id: number;
  name: string;
  contactPerson: string;
  email: string;
  phone: string;
  address: string;
  taxId: string;
  status: SupplierStatus;
  createdAt: string;
  updatedAt: string;
}

export type RequisitionStatus = 
  | 'DRAFT' 
  | 'SUBMITTED' 
  | 'APPROVED' 
  | 'REJECTED' 
  | 'CONVERTED_TO_ORDER' 
  | 'CANCELLED';

export interface RequisitionItem {
  id?: number;
  productId: number;
  productSku: string;
  productName: string;
  quantity: number;
  estimatedUnitPrice: number;
  lineTotal: number;
}

export interface PurchaseRequisition {
  id: number;
  requisitionNumber: string;
  requestingUser: string;
  department: string;
  businessJustification: string;
  totalEstimatedAmount: number;
  status: RequisitionStatus;
  submissionDate?: string;
  approverUser?: string;
  approvalDate?: string;
  approvalNotes?: string;
  items: RequisitionItem[];
  createdAt: string;
  updatedAt: string;
}

export type PurchaseOrderStatus = 
  | 'DRAFT' 
  | 'APPROVED' 
  | 'SENT' 
  | 'PARTIALLY_RECEIVED' 
  | 'RECEIVED' 
  | 'CANCELLED';

export interface PurchaseOrderItem {
  id: number;
  productId: number;
  productSku: string;
  productName: string;
  quantityOrdered: number;
  quantityReceived: number;
  unitPrice: number;
  lineTotal: number;
}

export interface PurchaseOrder {
  id: number;
  poNumber: string;
  requisitionId?: number;
  requisitionNumber?: string;
  supplierId: number;
  supplierName: string;
  totalAmount: number;
  status: PurchaseOrderStatus;
  orderDate: string;
  expectedDeliveryDate?: string;
  notes?: string;
  createdBy: string;
  items: PurchaseOrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface GoodsReceiptItem {
  id: number;
  poItemId: number;
  productId: number;
  productSku: string;
  quantityReceived: number;
  notes?: string;
}

export interface GoodsReceipt {
  id: number;
  receiptNumber: string;
  purchaseOrderId: number;
  poNumber: string;
  warehouseId: number;
  receivedDate: string;
  receivedBy: string;
  notes?: string;
  items: GoodsReceiptItem[];
  createdAt: string;
}

export interface DashboardStats {
  totalSuppliers: number;
  pendingRequisitions: number;
  openPurchaseOrders: number;
  totalSpendAmount: number;
  requisitionsByStatus: Record<string, number>;
  ordersByStatus: Record<string, number>;
  spendBySupplier: Record<string, number>;
}
