import React, { useEffect, useState } from 'react';
import { apiClient } from '../services/api';
import {
  ApiResponse,
  Product,
  PurchaseOrder,
  PurchaseOrderStatus,
  PurchaseRequisition,
  Supplier,
} from '../types';
import { Modal } from '../components/Modal';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';

export const OrdersPage: React.FC = () => {
  const { hasRole } = useAuth();
  const [orders, setOrders] = useState<PurchaseOrder[]>([]);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [approvedRequisitions, setApprovedRequisitions] = useState<PurchaseRequisition[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string>('');

  // View Modal
  const [viewingOrder, setViewingOrder] = useState<PurchaseOrder | null>(null);

  // Convert Requisition Modal
  const [isConvertModalOpen, setIsConvertModalOpen] = useState(false);
  const [selectedReqId, setSelectedReqId] = useState<number>(0);
  const [convertSupplierId, setConvertSupplierId] = useState<number>(0);

  // Create Standalone PO Modal
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [newPoSupplierId, setNewPoSupplierId] = useState<number>(0);
  const [deliveryDate, setDeliveryDate] = useState('');
  const [poNotes, setPoNotes] = useState('');
  const [poItems, setPoItems] = useState<{ productId: number; quantityOrdered: number; unitPrice: number }[]>([
    { productId: 0, quantityOrdered: 1, unitPrice: 0 },
  ]);

  const [modalError, setModalError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchOrders();
    fetchSuppliers();
    fetchProducts();
    fetchApprovedRequisitions();
  }, [statusFilter]);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      let url = '/procurement/orders?size=100';
      if (statusFilter) url += `&status=${statusFilter}`;
      const res = await apiClient.get<ApiResponse<{ content: PurchaseOrder[] }>>(url);
      if (res.data.success) {
        setOrders(res.data.data.content);
      }
    } catch (e) {
      console.error('Failed to load orders', e);
    } finally {
      setLoading(false);
    }
  };

  const fetchSuppliers = async () => {
    try {
      const res = await apiClient.get<ApiResponse<{ content: Supplier[] }>>('/procurement/suppliers?status=ACTIVE&size=100');
      if (res.data.success) {
        setSuppliers(res.data.data.content);
        if (res.data.data.content.length > 0) {
          setConvertSupplierId(res.data.data.content[0].id);
          setNewPoSupplierId(res.data.data.content[0].id);
        }
      }
    } catch (e) {
      console.error('Failed to load suppliers', e);
    }
  };

  const fetchProducts = async () => {
    try {
      const res = await apiClient.get<ApiResponse<{ content: Product[] }>>('/inventory/products?size=100');
      if (res.data.success) {
        setProducts(res.data.data.content);
        if (res.data.data.content.length > 0 && poItems[0].productId === 0) {
          setPoItems([{ productId: res.data.data.content[0].id, quantityOrdered: 1, unitPrice: res.data.data.content[0].unitPrice }]);
        }
      }
    } catch (e) {
      console.error('Failed to load products', e);
    }
  };

  const fetchApprovedRequisitions = async () => {
    try {
      const res = await apiClient.get<ApiResponse<{ content: PurchaseRequisition[] }>>(
        '/procurement/requisitions?status=APPROVED&size=100'
      );
      if (res.data.success) {
        setApprovedRequisitions(res.data.data.content);
        if (res.data.data.content.length > 0) {
          setSelectedReqId(res.data.data.content[0].id);
        }
      }
    } catch (e) {
      console.error('Failed to load approved requisitions', e);
    }
  };

  const handleConvertRequisition = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedReqId || !convertSupplierId) {
      setModalError('Please select both an approved requisition and a supplier.');
      return;
    }

    setSubmitting(true);
    setModalError(null);

    try {
      await apiClient.post(
        `/procurement/orders/from-requisition/${selectedReqId}?supplierId=${convertSupplierId}`
      );
      setIsConvertModalOpen(false);
      fetchOrders();
      fetchApprovedRequisitions();
    } catch (err: any) {
      setModalError(err.response?.data?.message || 'Failed to convert requisition to purchase order.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCreateStandalonePo = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newPoSupplierId || poItems.some((it) => it.productId === 0 || it.quantityOrdered <= 0)) {
      setModalError('Please ensure all fields and line items are properly specified.');
      return;
    }

    setSubmitting(true);
    setModalError(null);

    try {
      await apiClient.post('/procurement/orders', {
        supplierId: newPoSupplierId,
        expectedDeliveryDate: deliveryDate ? new Date(deliveryDate).toISOString() : null,
        notes: poNotes,
        items: poItems,
      });
      setIsCreateModalOpen(false);
      fetchOrders();
    } catch (err: any) {
      setModalError(err.response?.data?.message || 'Failed to create purchase order.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleApprovePo = async (id: number) => {
    if (!window.confirm('Authorize and approve this purchase order?')) return;
    try {
      await apiClient.post(`/procurement/orders/${id}/approve`);
      fetchOrders();
      if (viewingOrder?.id === id) setViewingOrder(null);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to approve order.');
    }
  };

  const handleSendPo = async (id: number) => {
    if (!window.confirm('Issue and dispatch purchase order to the vendor?')) return;
    try {
      await apiClient.post(`/procurement/orders/${id}/send`);
      fetchOrders();
      if (viewingOrder?.id === id) setViewingOrder(null);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to send order.');
    }
  };

  const handleCancelPo = async (id: number) => {
    if (!window.confirm('Are you sure you want to cancel this purchase order?')) return;
    try {
      await apiClient.post(`/procurement/orders/${id}/cancel`);
      fetchOrders();
      if (viewingOrder?.id === id) setViewingOrder(null);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to cancel order.');
    }
  };

  const canManageOrders = hasRole(['ADMIN', 'PROCUREMENT_MANAGER', 'PROCUREMENT_OFFICER']);

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Purchase Orders</h2>
          <p className="page-subtitle">Commercial binding contracts issued to external suppliers</p>
        </div>
        <div className="page-actions">
          {canManageOrders && (
            <>
              {approvedRequisitions.length > 0 && (
                <button
                  className="btn btn-secondary"
                  onClick={() => {
                    setModalError(null);
                    setIsConvertModalOpen(true);
                  }}
                >
                  ⚡ Convert PR to PO ({approvedRequisitions.length})
                </button>
              )}
              <button
                className="btn btn-primary"
                onClick={() => {
                  setModalError(null);
                  setIsCreateModalOpen(true);
                }}
              >
                + New Purchase Order
              </button>
            </>
          )}
        </div>
      </div>

      <div className="card" style={{ padding: '16px', marginBottom: '16px' }}>
        <div className="filter-bar" style={{ margin: 0 }}>
          <div style={{ width: '220px' }}>
            <select
              className="form-select"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All PO Statuses</option>
              <option value="DRAFT">DRAFT</option>
              <option value="APPROVED">APPROVED</option>
              <option value="SENT">SENT (Vendor Issued)</option>
              <option value="PARTIALLY_RECEIVED">PARTIALLY_RECEIVED</option>
              <option value="RECEIVED">RECEIVED (Completed)</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
          </div>
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="spinner"></div>
        ) : orders.length === 0 ? (
          <p style={{ color: '#64748b', fontSize: '13px' }}>No purchase orders found.</p>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>PO Number</th>
                  <th>Supplier</th>
                  <th>PR Reference</th>
                  <th>Total Amount</th>
                  <th>Status</th>
                  <th>Order Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((po) => (
                  <tr key={po.id}>
                    <td><strong>{po.poNumber}</strong></td>
                    <td>{po.supplierName}</td>
                    <td>{po.requisitionNumber ? <code>{po.requisitionNumber}</code> : '-'}</td>
                    <td><strong>${po.totalAmount.toLocaleString('en-US', { minimumFractionDigits: 2 })}</strong></td>
                    <td><StatusBadge status={po.status} /></td>
                    <td>{new Date(po.orderDate).toLocaleDateString()}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '6px' }}>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => setViewingOrder(po)}
                        >
                          View
                        </button>
                        {po.status === 'DRAFT' && canManageOrders && (
                          <button
                            className="btn btn-success btn-sm"
                            onClick={() => handleApprovePo(po.id)}
                          >
                            Approve
                          </button>
                        )}
                        {po.status === 'APPROVED' && canManageOrders && (
                          <button
                            className="btn btn-primary btn-sm"
                            onClick={() => handleSendPo(po.id)}
                          >
                            Send PO
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* View PO Modal */}
      <Modal
        isOpen={!!viewingOrder}
        onClose={() => setViewingOrder(null)}
        title={`Purchase Order: ${viewingOrder?.poNumber}`}
        maxWidth="800px"
      >
        {viewingOrder && (
          <div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
              <div>
                <strong>Supplier:</strong> {viewingOrder.supplierName}
              </div>
              <div>
                <strong>Status:</strong> <StatusBadge status={viewingOrder.status} />
              </div>
              <div>
                <strong>Order Date:</strong> {new Date(viewingOrder.orderDate).toLocaleString()}
              </div>
              <div>
                <strong>Total Amount:</strong> ${viewingOrder.totalAmount.toFixed(2)}
              </div>
            </div>

            {viewingOrder.requisitionNumber && (
              <div style={{ marginBottom: '12px', fontSize: '13px' }}>
                <strong>Origin Requisition:</strong> <code>{viewingOrder.requisitionNumber}</code>
              </div>
            )}

            {viewingOrder.notes && (
              <div style={{ marginBottom: '16px', fontSize: '13px', color: '#475569' }}>
                <strong>Notes:</strong> {viewingOrder.notes}
              </div>
            )}

            <h4 style={{ fontSize: '14px', marginBottom: '10px' }}>Order Fulfillment Tracking</h4>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Product SKU</th>
                  <th>Product Name</th>
                  <th>Ordered</th>
                  <th>Received</th>
                  <th>Pending</th>
                  <th>Unit Price</th>
                  <th>Total</th>
                </tr>
              </thead>
              <tbody>
                {viewingOrder.items.map((it, idx) => {
                  const pending = it.quantityOrdered - it.quantityReceived;
                  return (
                    <tr key={idx}>
                      <td><code>{it.productSku}</code></td>
                      <td>{it.productName}</td>
                      <td>{it.quantityOrdered}</td>
                      <td style={{ color: it.quantityReceived > 0 ? '#10b981' : '#64748b' }}>
                        {it.quantityReceived}
                      </td>
                      <td style={{ fontWeight: 'bold', color: pending > 0 ? '#f59e0b' : '#10b981' }}>
                        {pending}
                      </td>
                      <td>${it.unitPrice.toFixed(2)}</td>
                      <td>${it.lineTotal.toFixed(2)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>

            <div className="modal-footer" style={{ margin: '20px -24px -24px', padding: '16px 24px', justifyContent: 'space-between' }}>
              <button className="btn btn-secondary" onClick={() => setViewingOrder(null)}>
                Close
              </button>
              <div style={{ display: 'flex', gap: '8px' }}>
                {viewingOrder.status === 'DRAFT' && canManageOrders && (
                  <button className="btn btn-success" onClick={() => handleApprovePo(viewingOrder.id)}>
                    ✓ Approve Order
                  </button>
                )}
                {viewingOrder.status === 'APPROVED' && canManageOrders && (
                  <button className="btn btn-primary" onClick={() => handleSendPo(viewingOrder.id)}>
                    ✉ Issue to Vendor
                  </button>
                )}
                {['DRAFT', 'APPROVED', 'SENT'].includes(viewingOrder.status) && canManageOrders && (
                  <button className="btn btn-danger" onClick={() => handleCancelPo(viewingOrder.id)}>
                    Cancel PO
                  </button>
                )}
              </div>
            </div>
          </div>
        )}
      </Modal>

      {/* Convert Requisition Modal */}
      <Modal
        isOpen={isConvertModalOpen}
        onClose={() => setIsConvertModalOpen(false)}
        title="Convert Approved Requisition into Purchase Order"
      >
        {modalError && <div className="alert alert-danger">{modalError}</div>}
        <form onSubmit={handleConvertRequisition}>
          <div className="form-group">
            <label className="form-label">Select Approved Requisition *</label>
            <select
              className="form-select"
              value={selectedReqId}
              onChange={(e) => setSelectedReqId(Number(e.target.value))}
              required
            >
              {approvedRequisitions.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.requisitionNumber} - {r.department} (${r.totalEstimatedAmount.toFixed(2)})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Assign Fulfilling Vendor / Supplier *</label>
            <select
              className="form-select"
              value={convertSupplierId}
              onChange={(e) => setConvertSupplierId(Number(e.target.value))}
              required
            >
              {suppliers.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name} ({s.contactPerson})
                </option>
              ))}
            </select>
          </div>

          <div className="modal-footer" style={{ margin: '20px -24px -24px', padding: '16px 24px' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setIsConvertModalOpen(false)}
              disabled={submitting}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Generating PO...' : 'Convert to PO'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Create Standalone PO Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Create Direct Purchase Order"
        maxWidth="800px"
      >
        {modalError && <div className="alert alert-danger">{modalError}</div>}
        <form onSubmit={handleCreateStandalonePo}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label">Supplier *</label>
              <select
                className="form-select"
                value={newPoSupplierId}
                onChange={(e) => setNewPoSupplierId(Number(e.target.value))}
                required
              >
                {suppliers.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Expected Delivery Date</label>
              <input
                type="date"
                className="form-input"
                value={deliveryDate}
                onChange={(e) => setDeliveryDate(e.target.value)}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Procurement Notes</label>
            <input
              type="text"
              className="form-input"
              value={poNotes}
              onChange={(e) => setPoNotes(e.target.value)}
              placeholder="e.g. FOB Destination, Net 30 payment terms"
            />
          </div>

          <div style={{ marginBottom: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <label className="form-label" style={{ margin: 0 }}>Order Line Items</label>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => {
                  const p = products[0];
                  setPoItems([...poItems, { productId: p ? p.id : 0, quantityOrdered: 1, unitPrice: p ? p.unitPrice : 0 }]);
                }}
              >
                + Add Item
              </button>
            </div>

            {poItems.map((it, idx) => (
              <div
                key={idx}
                style={{
                  display: 'grid',
                  gridTemplateColumns: '3fr 1fr 1fr 1fr auto',
                  gap: '10px',
                  alignItems: 'center',
                  marginBottom: '10px',
                }}
              >
                <div>
                  <select
                    className="form-select"
                    value={it.productId}
                    onChange={(e) => {
                      const prodId = Number(e.target.value);
                      const prod = products.find((p) => p.id === prodId);
                      const updated = [...poItems];
                      updated[idx].productId = prodId;
                      if (prod) updated[idx].unitPrice = prod.unitPrice;
                      setPoItems(updated);
                    }}
                    required
                  >
                    {products.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.sku} - {p.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <input
                    type="number"
                    min="1"
                    className="form-input"
                    value={it.quantityOrdered}
                    onChange={(e) => {
                      const updated = [...poItems];
                      updated[idx].quantityOrdered = parseInt(e.target.value) || 1;
                      setPoItems(updated);
                    }}
                    required
                  />
                </div>
                <div>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    className="form-input"
                    value={it.unitPrice}
                    onChange={(e) => {
                      const updated = [...poItems];
                      updated[idx].unitPrice = parseFloat(e.target.value) || 0;
                      setPoItems(updated);
                    }}
                    required
                  />
                </div>
                <div style={{ textAlign: 'right', fontWeight: 'bold' }}>
                  ${(it.quantityOrdered * it.unitPrice).toFixed(2)}
                </div>
                <div>
                  <button
                    type="button"
                    className="btn btn-danger btn-sm"
                    onClick={() => {
                      if (poItems.length > 1) {
                        setPoItems(poItems.filter((_, i) => i !== idx));
                      }
                    }}
                    disabled={poItems.length <= 1}
                  >
                    ✕
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div className="modal-footer" style={{ margin: '20px -24px -24px', padding: '16px 24px' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setIsCreateModalOpen(false)}
              disabled={submitting}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Creating PO...' : 'Create Purchase Order'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
