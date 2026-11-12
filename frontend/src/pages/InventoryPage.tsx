import React, { useEffect, useState } from 'react';
import { apiClient } from '../services/api';
import { ApiResponse, Product, StockItem, StockMovement, Warehouse } from '../types';
import { Modal } from '../components/Modal';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';

export const InventoryPage: React.FC = () => {
  const { hasRole } = useAuth();
  const [activeTab, setActiveTab] = useState<'stock' | 'movements'>('stock');
  const [stockList, setStockList] = useState<StockItem[]>([]);
  const [movements, setMovements] = useState<StockMovement[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);

  // Stock Adjustment Modal
  const [isAdjustModalOpen, setIsAdjustModalOpen] = useState(false);
  const [adjustData, setAdjustData] = useState({
    productId: 0,
    warehouseId: 0,
    adjustmentType: 'ADD' as 'ADD' | 'SUBTRACT',
    quantity: 1,
    reason: 'CYCLE_COUNT',
  });
  const [modalError, setModalError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchStock();
    fetchWarehouses();
    fetchProducts();
    fetchMovements();
  }, []);

  const fetchWarehouses = async () => {
    try {
      const res = await apiClient.get<ApiResponse<Warehouse[]>>('/inventory/warehouses');
      if (res.data.success) setWarehouses(res.data.data);
    } catch (e) {
      console.error('Failed to load warehouses', e);
    }
  };

  const fetchProducts = async () => {
    try {
      const res = await apiClient.get<ApiResponse<{ content: Product[] }>>('/inventory/products?size=100');
      if (res.data.success) setProducts(res.data.data.content);
    } catch (e) {
      console.error('Failed to load products', e);
    }
  };

  const fetchStock = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get<ApiResponse<StockItem[]>>('/inventory/stock');
      if (res.data.success) setStockList(res.data.data);
    } catch (e) {
      console.error('Failed to load stock', e);
    } finally {
      setLoading(false);
    }
  };

  const fetchMovements = async () => {
    try {
      const res = await apiClient.get<ApiResponse<StockMovement[]>>('/inventory/stock/movements?page=0&size=50');
      if (res.data.success) setMovements(res.data.data);
    } catch (e) {
      console.error('Failed to load movements', e);
    }
  };

  const handleOpenAdjustModal = (item?: StockItem) => {
    setAdjustData({
      productId: item?.productId || (products[0]?.id || 0),
      warehouseId: item?.warehouseId || (warehouses[0]?.id || 0),
      adjustmentType: 'ADD',
      quantity: 1,
      reason: 'Physical inventory audit',
    });
    setModalError(null);
    setIsAdjustModalOpen(true);
  };

  const handleSaveAdjustment = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setModalError(null);

    try {
      await apiClient.post('/inventory/stock/adjustments', adjustData);
      setIsAdjustModalOpen(false);
      fetchStock();
      fetchMovements();
    } catch (err: any) {
      setModalError(err.response?.data?.message || 'Failed to apply inventory adjustment.');
    } finally {
      setSubmitting(false);
    }
  };

  const canAdjustStock = hasRole(['ADMIN', 'INVENTORY_MANAGER']);

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Inventory & Stock Movements</h2>
          <p className="page-subtitle">Track physical quantities, conduct cycle counts, and audit inventory transactions</p>
        </div>
        <div className="page-actions">
          {canAdjustStock && (
            <button className="btn btn-primary" onClick={() => handleOpenAdjustModal()}>
              ⚙️ Stock Adjustment
            </button>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '8px', marginBottom: '20px' }}>
        <button
          className={`btn ${activeTab === 'stock' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveTab('stock')}
        >
          📦 Warehouse Stock
        </button>
        <button
          className={`btn ${activeTab === 'movements' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setActiveTab('movements')}
        >
          📜 Movement Audit History
        </button>
      </div>

      {/* Stock List Tab */}
      {activeTab === 'stock' && (
        <div className="card">
          {loading ? (
            <div className="spinner"></div>
          ) : stockList.length === 0 ? (
            <p style={{ color: '#64748b', fontSize: '13px' }}>No stock records found.</p>
          ) : (
            <div className="table-responsive">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>Warehouse</th>
                    <th>Available Quantity</th>
                    <th>Unit Price</th>
                    <th>Valuation</th>
                    <th>Health</th>
                    {canAdjustStock && <th>Actions</th>}
                  </tr>
                </thead>
                <tbody>
                  {stockList.map((item) => (
                    <tr key={item.id}>
                      <td>
                        <strong>{item.productSku}</strong>
                        <div>{item.productName}</div>
                      </td>
                      <td>
                        <strong>{item.warehouseName}</strong>
                        <div style={{ fontSize: '11px', color: '#64748b' }}>{item.warehouseLocation}</div>
                      </td>
                      <td>
                        <strong style={{ fontSize: '14px', color: item.lowStock ? '#ef4444' : '#0f172a' }}>
                          {item.quantity} {item.unitOfMeasurement}
                        </strong>
                      </td>
                      <td>${item.unitPrice.toFixed(2)}</td>
                      <td>${item.totalValue.toFixed(2)}</td>
                      <td>
                        {item.lowStock ? (
                          <span className="status-badge badge-danger">LOW STOCK</span>
                        ) : (
                          <span className="status-badge badge-success">HEALTHY</span>
                        )}
                      </td>
                      {canAdjustStock && (
                        <td>
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => handleOpenAdjustModal(item)}
                          >
                            Adjust
                          </button>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Movement History Tab */}
      {activeTab === 'movements' && (
        <div className="card">
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>Product</th>
                  <th>Warehouse</th>
                  <th>Type</th>
                  <th>Delta Qty</th>
                  <th>Prev Qty</th>
                  <th>New Qty</th>
                  <th>Reference</th>
                  <th>Reason</th>
                  <th>Actor</th>
                </tr>
              </thead>
              <tbody>
                {movements.map((m) => (
                  <tr key={m.id}>
                    <td>{new Date(m.createdAt).toLocaleString()}</td>
                    <td>
                      <code>{m.productSku}</code> - {m.productName}
                    </td>
                    <td>{m.warehouseName}</td>
                    <td>
                      <StatusBadge status={m.movementType} />
                    </td>
                    <td style={{ fontWeight: 'bold' }}>
                      {m.quantity > 0 ? `+${m.quantity}` : m.quantity}
                    </td>
                    <td>{m.previousQuantity}</td>
                    <td>{m.newQuantity}</td>
                    <td><code>{m.referenceNumber || '-'}</code></td>
                    <td>{m.reason || '-'}</td>
                    <td>{m.createdBy}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Stock Adjustment Modal */}
      <Modal
        isOpen={isAdjustModalOpen}
        onClose={() => setIsAdjustModalOpen(false)}
        title="Conduct Inventory Adjustment"
      >
        {modalError && <div className="alert alert-danger">{modalError}</div>}
        <form onSubmit={handleSaveAdjustment}>
          <div className="form-group">
            <label className="form-label">Product *</label>
            <select
              className="form-select"
              value={adjustData.productId}
              onChange={(e) => setAdjustData({ ...adjustData, productId: Number(e.target.value) })}
              required
            >
              {products.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.sku} - {p.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Warehouse *</label>
            <select
              className="form-select"
              value={adjustData.warehouseId}
              onChange={(e) => setAdjustData({ ...adjustData, warehouseId: Number(e.target.value) })}
              required
            >
              {warehouses.map((w) => (
                <option key={w.id} value={w.id}>
                  {w.name} ({w.location})
                </option>
              ))}
            </select>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label">Adjustment Type *</label>
              <select
                className="form-select"
                value={adjustData.adjustmentType}
                onChange={(e) =>
                  setAdjustData({ ...adjustData, adjustmentType: e.target.value as 'ADD' | 'SUBTRACT' })
                }
              >
                <option value="ADD">ADD (Stock Inflow)</option>
                <option value="SUBTRACT">SUBTRACT (Stock Outflow / Shrinkage)</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Quantity (Positive) *</label>
              <input
                type="number"
                min="1"
                className="form-input"
                value={adjustData.quantity}
                onChange={(e) => setAdjustData({ ...adjustData, quantity: parseInt(e.target.value) || 1 })}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Reason / Justification *</label>
            <input
              type="text"
              className="form-input"
              value={adjustData.reason}
              onChange={(e) => setAdjustData({ ...adjustData, reason: e.target.value })}
              placeholder="e.g. Cycle count variance, Damaged stock write-off"
              required
            />
          </div>

          <div className="modal-footer" style={{ margin: '20px -24px -24px', padding: '16px 24px' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setIsAdjustModalOpen(false)}
              disabled={submitting}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Applying...' : 'Apply Adjustment'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
