import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Layout } from './components/Layout';

// Pages
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { ProductsPage } from './pages/ProductsPage';
import { InventoryPage } from './pages/InventoryPage';
import { WarehousesPage } from './pages/WarehousesPage';
import { SuppliersPage } from './pages/SuppliersPage';
import { RequisitionsPage } from './pages/RequisitionsPage';
import { ApprovalsPage } from './pages/ApprovalsPage';
import { OrdersPage } from './pages/OrdersPage';
import { ReceiptsPage } from './pages/ReceiptsPage';
import { ReportsPage } from './pages/ReportsPage';
import { UsersPage } from './pages/UsersPage';

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<LoginPage />} />

          {/* Authenticated Routes wrapped in enterprise layout */}
          <Route element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              <Route path="/" element={<DashboardPage />} />
              <Route path="/products" element={<ProductsPage />} />
              <Route path="/inventory" element={<InventoryPage />} />
              <Route path="/warehouses" element={<WarehousesPage />} />
              <Route path="/suppliers" element={<SuppliersPage />} />
              <Route path="/requisitions" element={<RequisitionsPage />} />

              {/* Role Restricted Routes */}
              <Route
                element={
                  <ProtectedRoute
                    allowedRoles={['ADMIN', 'APPROVER', 'PROCUREMENT_MANAGER']}
                  />
                }
              >
                <Route path="/approvals" element={<ApprovalsPage />} />
              </Route>

              <Route
                element={
                  <ProtectedRoute
                    allowedRoles={['ADMIN', 'PROCUREMENT_MANAGER', 'PROCUREMENT_OFFICER']}
                  />
                }
              >
                <Route path="/orders" element={<OrdersPage />} />
              </Route>

              <Route
                element={
                  <ProtectedRoute
                    allowedRoles={[
                      'ADMIN',
                      'INVENTORY_MANAGER',
                      'PROCUREMENT_OFFICER',
                      'PROCUREMENT_MANAGER',
                    ]}
                  />
                }
              >
                <Route path="/receipts" element={<ReceiptsPage />} />
              </Route>

              <Route path="/reports" element={<ReportsPage />} />

              <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                <Route path="/users" element={<UsersPage />} />
              </Route>
            </Route>
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
