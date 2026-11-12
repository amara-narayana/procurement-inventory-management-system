import React, { useEffect, useState } from 'react';
import { apiClient } from '../services/api';
import { ApiResponse, DashboardStats, StockItem } from '../types';

export const ReportsPage: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [stock, setStock] = useState<StockItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchReportData();
  }, []);

  const fetchReportData = async () => {
    setLoading(true);
    try {
      const [statsRes, stockRes] = await Promise.all([
        apiClient.get<ApiResponse<DashboardStats>>('/procurement/reports/dashboard'),
        apiClient.get<ApiResponse<StockItem[]>>('/inventory/stock'),
      ]);

      if (statsRes.data.success) setStats(statsRes.data.data);
      if (stockRes.data.success) setStock(stockRes.data.data);
    } catch (e) {
      console.error('Failed to load reports', e);
    } finally {
      setLoading(false);
    }
  };

  const totalInventoryValuation = stock.reduce((acc, it) => acc + (it.totalValue || 0), 0);

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Compiling enterprise analytical reports...</p>
      </div>
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Procurement & Inventory Analytics</h2>
          <p className="page-subtitle">Commercial spend breakdown, supplier allocations, and asset valuations</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-secondary" onClick={fetchReportData}>
            🔄 Refresh Metrics
          </button>
        </div>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon green">💵</div>
          <div>
            <div className="stat-value">
              ${stats?.totalSpendAmount ? stats.totalSpendAmount.toLocaleString('en-US', { minimumFractionDigits: 2 }) : '0.00'}
            </div>
            <div className="stat-label">Total Procurement Spend (Approved POs)</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon blue">🏢</div>
          <div>
            <div className="stat-value">
              ${totalInventoryValuation.toLocaleString('en-US', { minimumFractionDigits: 2 })}
            </div>
            <div className="stat-label">Total Current Inventory Valuation</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon purple">📦</div>
          <div>
            <div className="stat-value">{stock.length}</div>
            <div className="stat-label">SKU-Warehouse Allocations</div>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))', gap: '20px' }}>
        {/* Spend by Supplier */}
        <div className="card">
          <h3 className="card-title">Commercial Spend by Vendor</h3>
          {stats?.spendBySupplier && Object.keys(stats.spendBySupplier).length > 0 ? (
            <div className="table-responsive">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Vendor Name</th>
                    <th>Spend ($)</th>
                  </tr>
                </thead>
                <tbody>
                  {Object.entries(stats.spendBySupplier).map(([supplier, amount]) => (
                    <tr key={supplier}>
                      <td><strong>{supplier}</strong></td>
                      <td>${amount.toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p style={{ color: '#64748b', fontSize: '13px' }}>No supplier spend recorded yet.</p>
          )}
        </div>

        {/* PO Breakdown by Status */}
        <div className="card">
          <h3 className="card-title">Purchase Orders by Status</h3>
          {stats?.ordersByStatus && Object.keys(stats.ordersByStatus).length > 0 ? (
            <div className="table-responsive">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Order Status</th>
                    <th>Order Count</th>
                  </tr>
                </thead>
                <tbody>
                  {Object.entries(stats.ordersByStatus).map(([status, count]) => (
                    <tr key={status}>
                      <td><code>{status}</code></td>
                      <td><strong>{count}</strong></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p style={{ color: '#64748b', fontSize: '13px' }}>No orders found.</p>
          )}
        </div>

        {/* Requisitions Breakdown by Status */}
        <div className="card">
          <h3 className="card-title">Requisitions by Status</h3>
          {stats?.requisitionsByStatus && Object.keys(stats.requisitionsByStatus).length > 0 ? (
            <div className="table-responsive">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Requisition Status</th>
                    <th>Requisition Count</th>
                  </tr>
                </thead>
                <tbody>
                  {Object.entries(stats.requisitionsByStatus).map(([status, count]) => (
                    <tr key={status}>
                      <td><code>{status}</code></td>
                      <td><strong>{count}</strong></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p style={{ color: '#64748b', fontSize: '13px' }}>No requisitions found.</p>
          )}
        </div>
      </div>
    </div>
  );
};
