import React, { useEffect, useState } from 'react';
import { apiClient } from '../services/api';
import { ApiResponse, Product, PurchaseRequisition, RequisitionItem, RequisitionStatus } from '../types';
import { Modal } from '../components/Modal';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';

export const RequisitionsPage: React.FC = () => {
  const { user, hasRole } = useAuth();
  const [requisitions, setRequisitions] = useState<PurchaseRequisition[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string>('');

  // View Details Modal
  const [viewingReq, setViewingReq] = useState<PurchaseRequisition | null>(null);

  // Create Requisition Modal
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [department, setDepartment] = useState(user?.department || 'Engineering');
  const [justification, setJustification] = useState('');
  const [items, setItems] = useState<{ productId: number; quantity: number; estimatedUnitPrice: number }[]>([
    { productId: 0, quantity: 1, estimatedUnitPrice: 0 },
  ]);
  const [modalError, setModalError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchRequisitions();
    fetchProducts();
  }, [statusFilter]);

  const fetchProducts = async () => {
    try {
      const res = await apiClient.get<ApiResponse<{ content: Product[] }>>('/inventory/products?size=100');
      if (res.data.success) {
        setProducts(res.data.data.content);
        if (res.data.data.content.length > 0 && items[0].productId === 0) {
          setItems([{ productId: res.data.data.content[0].id, quantity: 1, estimatedUnitPrice: res.data.data.content[0].unitPrice }]);
        }
      }
    } catch (e) {
      console.error('Failed to load products', e);
    }
  };

  const fetchRequisitions = async () => {
    setLoading(true);
    try {
      let url = '/procurement/requisitions?size=100';
      if (statusFilter) url += `&status=${statusFilter}`;
      const res = await apiClient.get<ApiResponse<{ content: PurchaseRequisition[] }>>(url);
      if (res.data.success) {
        setRequisitions(res.data.data.content);
      }
    } catch (e) {
      console.error('Failed to load requisitions', e);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreateModal = () => {
    setDepartment(user?.department || 'Engineering');
    setJustification('');
    const defaultProduct = products[0];
    setItems([
      {
        productId: defaultProduct ? defaultProduct.id : 0,
        quantity: 1,
        estimatedUnitPrice: defaultProduct ? defaultProduct.unitPrice : 0,
      },
    ]);
    setModalError(null);
    setIsCreateModalOpen(true);
  };

  const handleAddItem = () => {
    const defaultProduct = products[0];
    setItems([
      ...items,
      {
        productId: defaultProduct ? defaultProduct.id : 0,
        quantity: 1,
        estimatedUnitPrice: defaultProduct ? defaultProduct.unitPrice : 0,
      },
    ]);
  };

  const handleRemoveItem = (index: number) => {
    if (items.length <= 1) return;
    setItems(items.filter((_, i) => i !== index));
  };

  const handleProductChange = (index: number, productId: number) => {
    const found = products.find((p) => p.id === productId);
    const newItems = [...items];
    newItems[index].productId = productId;
    if (found) {
      newItems[index].estimatedUnitPrice = found.unitPrice;
    }
    setItems(newItems);
  };

  const handleQuantityChange = (index: number, qty: number) => {
    const newItems = [...items];
    newItems[index].quantity = qty > 0 ? qty : 1;
    setItems(newItems);
  };

  const calculateTotal = () => {
    return items.reduce((acc, it) => acc + it.quantity * it.estimatedUnitPrice, 0);
  };

  const handleCreateRequisition = async (e: React.FormEvent) => {
    e.preventDefault();
    if (items.length === 0 || items.some((it) => it.productId === 0 || it.quantity <= 0)) {
      setModalError('Please ensure all items have a valid product and quantity.');
      return;
    }

    setSubmitting(true);
    setModalError(null);

    try {
      await apiClient.post('/procurement/requisitions', {
        department,
        businessJustification: justification,
        items: items.map((it) => ({
          productId: it.productId,
          quantity: it.quantity,
          estimatedUnitPrice: it.estimatedUnitPrice,
        })),
      });
      setIsCreateModalOpen(false);
      fetchRequisitions();
    } catch (err: any) {
      setModalError(err.response?.data?.message || 'Failed to create purchase requisition.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleSubmitRequisition = async (id: number) => {
    if (!window.confirm('Submit this purchase requisition for approval?')) return;
    try {
      await apiClient.post(`/procurement/requisitions/${id}/submit`);
      fetchRequisitions();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to submit requisition.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Purchase Requisitions</h2>
          <p className="page-subtitle">Draft, submit, and track departmental purchasing demands</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-primary" onClick={handleOpenCreateModal}>
            + New Requisition
          </button>
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
              <option value="">All Requisition Statuses</option>
              <option value="DRAFT">DRAFT</option>
              <option value="SUBMITTED">SUBMITTED (Pending Review)</option>
              <option value="APPROVED">APPROVED</option>
              <option value="REJECTED">REJECTED</option>
              <option value="CONVERTED_TO_ORDER">CONVERTED_TO_ORDER</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
          </div>
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="spinner"></div>
        ) : requisitions.length === 0 ? (
          <p style={{ color: '#64748b', fontSize: '13px' }}>No requisitions found.</p>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>PR Number</th>
                  <th>Requester</th>
                  <th>Department</th>
                  <th>Total Estimate</th>
                  <th>Status</th>
                  <th>Created Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {requisitions.map((req) => (
                  <tr key={req.id}>
                    <td><strong>{req.requisitionNumber}</strong></td>
                    <td>{req.requestingUser}</td>
                    <td>{req.department}</td>
                    <td>${req.totalEstimatedAmount.toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
                    <td><StatusBadge status={req.status} /></td>
                    <td>{new Date(req.createdAt).toLocaleDateString()}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '6px' }}>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => setViewingReq(req)}
                        >
                          View
                        </button>
                        {req.status === 'DRAFT' && req.requestingUser === user?.username && (
                          <button
                            className="btn btn-primary btn-sm"
                            onClick={() => handleSubmitRequisition(req.id)}
                          >
                            Submit
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

      {/* View Details Modal */}
      <Modal
        isOpen={!!viewingReq}
        onClose={() => setViewingReq(null)}
        title={`Requisition Details: ${viewingReq?.requisitionNumber}`}
        maxWidth="750px"
      >
        {viewingReq && (
          <div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
              <div>
                <strong>Requesting User:</strong> {viewingReq.requestingUser}
              </div>
              <div>
                <strong>Department:</strong> {viewingReq.department}
              </div>
              <div>
                <strong>Status:</strong> <StatusBadge status={viewingReq.status} />
              </div>
              <div>
                <strong>Total Amount:</strong> ${viewingReq.totalEstimatedAmount.toFixed(2)}
              </div>
            </div>

            <div style={{ marginBottom: '16px' }}>
              <strong>Business Justification:</strong>
              <p style={{ marginTop: '4px', color: '#475569' }}>{viewingReq.businessJustification}</p>
            </div>

            {viewingReq.approverUser && (
              <div style={{ padding: '12px', backgroundColor: '#f8fafc', borderRadius: '6px', marginBottom: '16px' }}>
                <div><strong>Approver:</strong> {viewingReq.approverUser} ({viewingReq.approvalDate})</div>
                {viewingReq.approvalNotes && (
                  <div style={{ marginTop: '4px' }}><strong>Notes:</strong> {viewingReq.approvalNotes}</div>
                )}
              </div>
            )}

            <h4 style={{ fontSize: '14px', marginBottom: '10px' }}>Requested Line Items</h4>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Product SKU</th>
                  <th>Product Name</th>
                  <th>Qty</th>
                  <th>Unit Price</th>
                  <th>Line Total</th>
                </tr>
              </thead>
              <tbody>
                {viewingReq.items.map((it, idx) => (
                  <tr key={idx}>
                    <td><code>{it.productSku}</code></td>
                    <td>{it.productName}</td>
                    <td>{it.quantity}</td>
                    <td>${it.estimatedUnitPrice.toFixed(2)}</td>
                    <td>${it.lineTotal.toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="modal-footer" style={{ margin: '20px -24px -24px', padding: '16px 24px' }}>
              <button className="btn btn-secondary" onClick={() => setViewingReq(null)}>
                Close
              </button>
            </div>
          </div>
        )}
      </Modal>

      {/* Create Requisition Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Create Purchase Requisition"
        maxWidth="800px"
      >
        {modalError && <div className="alert alert-danger">{modalError}</div>}
        <form onSubmit={handleCreateRequisition}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label">Department *</label>
              <input
                type="text"
                className="form-input"
                value={department}
                onChange={(e) => setDepartment(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Estimated Total Amount</label>
              <div style={{ padding: '8px 12px', fontSize: '15px', fontWeight: 'bold', color: '#047857' }}>
                ${calculateTotal().toFixed(2)}
              </div>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Business Justification *</label>
            <textarea
              className="form-textarea"
              rows={2}
              value={justification}
              onChange={(e) => setJustification(e.target.value)}
              placeholder="State the commercial or operational rationale for this procurement..."
              required
            />
          </div>

          <div style={{ marginBottom: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <label className="form-label" style={{ margin: 0 }}>Line Items *</label>
              <button type="button" className="btn btn-secondary btn-sm" onClick={handleAddItem}>
                + Add Item
              </button>
            </div>

            {items.map((it, idx) => (
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
                    onChange={(e) => handleProductChange(idx, Number(e.target.value))}
                    required
                  >
                    {products.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.sku} - {p.name} (${p.unitPrice})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <input
                    type="number"
                    min="1"
                    className="form-input"
                    value={it.quantity}
                    onChange={(e) => handleQuantityChange(idx, parseInt(e.target.value) || 1)}
                    placeholder="Qty"
                    required
                  />
                </div>
                <div style={{ fontSize: '13px', color: '#475569', textAlign: 'right' }}>
                  ${it.estimatedUnitPrice.toFixed(2)}
                </div>
                <div style={{ fontSize: '13px', fontWeight: 'bold', textAlign: 'right' }}>
                  ${(it.quantity * it.estimatedUnitPrice).toFixed(2)}
                </div>
                <div>
                  <button
                    type="button"
                    className="btn btn-danger btn-sm"
                    onClick={() => handleRemoveItem(idx)}
                    disabled={items.length <= 1}
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
              {submitting ? 'Saving Draft...' : 'Save Requisition'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
