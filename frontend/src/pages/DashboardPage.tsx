import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiClient } from '../services/api';
import { ApiResponse, DashboardStats, StockItem, StockMovement } from '../types';
import { StatusBadge } from '../components/StatusBadge';

export const DashboardPage: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [lowStockItems, setLowStockItems] = useState<StockItem[]>([]);
  const [recentMovements, setRecentMovements] = useState<StockMovement[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [statsRes, lowStockRes, movementsRes] = await Promise.all([
        apiClient.get<ApiResponse<DashboardStats>>('/procurement/reports/dashboard'),
        apiClient.get<ApiResponse<StockItem[]>>('/inventory/stock/low-stock'),
        apiClient.get<ApiResponse<StockMovement[]>>('/inventory/stock/movements?page=0&size=5'),
      ]);

      if (statsRes.data.success) setStats(statsRes.data.data);
      if (lowStockRes.data.success) setLowStockItems(lowStockRes.data.data);
      if (movementsRes.data.success) setRecentMovements(movementsRes.data.data);
    } catch (err: any) {
      console.error('Failed to load dashboard data:', err);
      setError('Unable to load dashboard data. Please ensure backend services are running.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Loading enterprise dashboard metrics...</p>
      </div>
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Operational Overview</h2>
          <p className="page-subtitle">Real-time procurement KPIs, stock thresholds, and logistics updates</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-secondary" onClick={fetchDashboardData}>
            🔄 Refresh Data
          </button>
          <Link to="/requisitions" className="btn btn-primary">
            + New Requisition
          </Link>
        </div>
      </div>

      {error && <div className="alert alert-warning">{error}</div>}

      {/* KPI Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon blue">📝</div>
          <div>
            <div className="stat-value">{stats?.pendingRequisitions ?? 0}</div>
            <div className="stat-label">Pending Requisitions</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon purple">📋</div>
          <div>
            <div className="stat-value">{stats?.openPurchaseOrders ?? 0}</div>
            <div className="stat-label">Open Purchase Orders</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon green">🤝</div>
          <div>
            <div className="stat-value">{stats?.totalSuppliers ?? 0}</div>
            <div className="stat-label">Active Suppliers</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon orange">⚠️</div>
          <div>
            <div className="stat-value">{lowStockItems.length}</div>
            <div className="stat-label">Low Stock Alerts</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon green">💰</div>
          <div>
            <div className="stat-value">
              ${stats?.totalSpendAmount ? stats.totalSpendAmount.toLocaleString('en-US', { minimumFractionDigits: 2 }) : '0.00'}
            </div>
            <div className="stat-label">Total Spend (Approved POs)</div>
          </div>
        </div>
      </div>

      {/* Low Stock Alerts */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h3 className="card-title" style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span>⚠️</span> Low Stock Warnings
          </h3>
          <Link to="/inventory" className="btn btn-secondary btn-sm">
            View All Stock →
          </Link>
        </div>

        {lowStockItems.length === 0 ? (
          <p style={{ color: '#64748b', fontSize: '13px' }}>All product inventory levels are currently above reorder thresholds.</p>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>SKU</th>
                  <th>Product Name</th>
                  <th>Warehouse</th>
                  <th>Current Quantity</th>
                  <th>Unit Price</th>
                  <th>Total Value</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {lowStockItems.map((item) => (
                  <tr key={item.id}>
                    <td><strong>{item.productSku}</strong></td>
                    <td>{item.productName}</td>
                    <td>{item.warehouseName}</td>
                    <td style={{ color: '#ef4444', fontWeight: 'bold' }}>
                      {item.quantity} {item.unitOfMeasurement}
                    </td>
                    <td>${item.unitPrice.toFixed(2)}</td>
                    <td>${item.totalValue.toFixed(2)}</td>
                    <td>
                      <span className="status-badge badge-danger">LOW STOCK</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Recent Stock Movements */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h3 className="card-title" style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span>📦</span> Recent Stock Movements
          </h3>
          <Link to="/inventory" className="btn btn-secondary btn-sm">
            View Audit Log →
          </Link>
        </div>

        {recentMovements.length === 0 ? (
          <p style={{ color: '#64748b', fontSize: '13px' }}>No stock movements recorded yet.</p>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>Product</th>
                  <th>Warehouse</th>
                  <th>Movement Type</th>
                  <th>Delta Qty</th>
                  <th>New Qty</th>
                  <th>Reference</th>
                  <th>Created By</th>
                </tr>
              </thead>
              <tbody>
                {recentMovements.map((m) => (
                  <tr key={m.id}>
                    <td>{new Date(m.createdAt).toLocaleString()}</td>
                    <td>
                      <strong>{m.productSku}</strong> - {m.productName}
                    </td>
                    <td>{m.warehouseName}</td>
                    <td>
                      <StatusBadge status={m.movementType} />
                    </td>
                    <td>{m.quantity > 0 ? `+${m.quantity}` : m.quantity}</td>
                    <td>{m.newQuantity}</td>
                    <td>{m.referenceNumber || '-'}</td>
                    <td>{m.createdBy}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
