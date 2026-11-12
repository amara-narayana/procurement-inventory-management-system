import React, { useEffect, useState } from 'react';
import { apiClient } from '../services/api';
import { ApiResponse, GoodsReceipt, PurchaseOrder, Warehouse } from '../types';
import { Modal } from '../components/Modal';
import { useAuth } from '../context/AuthContext';

export const ReceiptsPage: React.FC = () => {
  const { hasRole } = useAuth();
  const [receipts, setReceipts] = useState<GoodsReceipt[]>([]);
  const [openOrders, setOpenOrders] = useState<PurchaseOrder[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [loading, setLoading] = useState(true);

  // View Receipt Modal
  const [viewingReceipt, setViewingReceipt] = useState<GoodsReceipt | null>(null);

  // Process Receipt Modal
  const [isProcessModalOpen, setIsProcessModalOpen] = useState(false);
  const [selectedPo, setSelectedPo] = useState<PurchaseOrder | null>(null);
  const [warehouseId, setWarehouseId] = useState<number>(0);
  const [receiptNotes, setReceiptNotes] = useState('');
  const [receiptItems, setReceiptItems] = useState<
    { poItemId: number; productId: number; productSku: string; productName: string; maxAllowed: number; quantityReceived: number; notes: string }[]
  >([]);
  const [modalError, setModalError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchReceipts();
    fetchOpenOrders();
    fetchWarehouses();
  }, []);

  const fetchReceipts = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get<ApiResponse<GoodsReceipt[]>>('/procurement/orders/receipts/all');
      if (res.data.success) {
        setReceipts(res.data.data);
      }
    } catch (e) {
      console.error('Failed to load receipts', e);
    } finally {
      setLoading(false);
    }
  };

  const fetchOpenOrders = async () => {
    try {
      const [sentRes, partRes, appRes] = await Promise.all([
        apiClient.get<ApiResponse<{ content: PurchaseOrder[] }>>('/procurement/orders?status=SENT&size=50'),
        apiClient.get<ApiResponse<{ content: PurchaseOrder[] }>>('/procurement/orders?status=PARTIALLY_RECEIVED&size=50'),
        apiClient.get<ApiResponse<{ content: PurchaseOrder[] }>>('/procurement/orders?status=APPROVED&size=50'),
      ]);

      const combined = [
        ...(sentRes.data.data?.content || []),
        ...(partRes.data.data?.content || []),
        ...(appRes.data.data?.content || []),
      ];
      setOpenOrders(combined);
    } catch (e) {
      console.error('Failed to load eligible orders', e);
    }
  };

  const fetchWarehouses = async () => {
    try {
      const res = await apiClient.get<ApiResponse<Warehouse[]>>('/inventory/warehouses');
      if (res.data.success) {
        setWarehouses(res.data.data);
        if (res.data.data.length > 0) setWarehouseId(res.data.data[0].id);
      }
    } catch (e) {
      console.error('Failed to load warehouses', e);
    }
  };

  const handleSelectPo = (po: PurchaseOrder) => {
    setSelectedPo(po);
    const initialItems = po.items
      .map((it) => {
        const remaining = it.quantityOrdered - it.quantityReceived;
        return {
          poItemId: it.id,
          productId: it.productId,
          productSku: it.productSku,
          productName: it.productName,
          maxAllowed: remaining,
          quantityReceived: remaining > 0 ? remaining : 0,
          notes: '',
        };
      })
      .filter((it) => it.maxAllowed > 0);

    setReceiptItems(initialItems);
    setReceiptNotes('');
    setModalError(null);
    setIsProcessModalOpen(true);
  };

  const handleItemQtyChange = (index: number, val: number) => {
    const updated = [...receiptItems];
    const max = updated[index].maxAllowed;
    const clamped = Math.max(0, Math.min(val, max));
    updated[index].quantityReceived = clamped;
    setReceiptItems(updated);
  };

  const handleSubmitReceipt = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedPo) return;

    const itemsToSubmit = receiptItems
      .filter((it) => it.quantityReceived > 0)
      .map((it) => ({
        poItemId: it.poItemId,
        productId: it.productId,
        quantityReceived: it.quantityReceived,
        notes: it.notes,
      }));

    if (itemsToSubmit.length === 0) {
      setModalError('Please enter at least one item with a received quantity greater than 0.');
      return;
    }

    setSubmitting(true);
    setModalError(null);

    try {
      await apiClient.post(`/procurement/orders/${selectedPo.id}/receipts`, {
        warehouseId,
        notes: receiptNotes,
        items: itemsToSubmit,
      });

      setIsProcessModalOpen(false);
      fetchReceipts();
      fetchOpenOrders();
    } catch (err: any) {
      setModalError(err.response?.data?.message || 'Failed to record goods receipt.');
    } finally {
      setSubmitting(false);
    }
  };

  const canReceiveGoods = hasRole(['ADMIN', 'INVENTORY_MANAGER', 'PROCUREMENT_OFFICER', 'PROCUREMENT_MANAGER']);

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Goods Receipt Processing</h2>
          <p className="page-subtitle">Verify supplier shipments, record partial receipts, and ingest into warehouse stock</p>
        </div>
      </div>

      {/* Eligible POs for Receiving */}
      {canReceiveGoods && openOrders.length > 0 && (
        <div className="card">
          <h3 className="card-title">Pending Orders Awaiting Delivery ({openOrders.length})</h3>
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>PO Number</th>
                  <th>Supplier</th>
                  <th>Status</th>
                  <th>Total Amount</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {openOrders.map((po) => (
                  <tr key={po.id}>
                    <td><strong>{po.poNumber}</strong></td>
                    <td>{po.supplierName}</td>
                    <td><span className="status-badge badge-warning">{po.status}</span></td>
                    <td>${po.totalAmount.toFixed(2)}</td>
                    <td>
                      <button
                        className="btn btn-primary btn-sm"
                        onClick={() => handleSelectPo(po)}
                      >
                        📥 Ingest Receipt
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Processed Receipts List */}
      <div className="card">
        <h3 className="card-title">Completed Goods Receipts History</h3>
        {loading ? (
          <div className="spinner"></div>
        ) : receipts.length === 0 ? (
          <p style={{ color: '#64748b', fontSize: '13px' }}>No goods receipts logged yet.</p>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Receipt Number</th>
                  <th>PO Reference</th>
                  <th>Warehouse ID</th>
                  <th>Received Date</th>
                  <th>Received By</th>
                  <th>Items Count</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {receipts.map((gr) => (
                  <tr key={gr.id}>
                    <td><strong>{gr.receiptNumber}</strong></td>
                    <td><code>{gr.poNumber}</code></td>
                    <td>Warehouse #{gr.warehouseId}</td>
                    <td>{new Date(gr.receivedDate).toLocaleString()}</td>
                    <td>{gr.receivedBy}</td>
                    <td>{gr.items?.length || 0} line(s)</td>
                    <td>
                      <button
                        className="btn btn-secondary btn-sm"
                        onClick={() => setViewingReceipt(gr)}
                      >
                        View Items
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* View Receipt Modal */}
      <Modal
        isOpen={!!viewingReceipt}
        onClose={() => setViewingReceipt(null)}
        title={`Goods Receipt: ${viewingReceipt?.receiptNumber}`}
        maxWidth="700px"
      >
        {viewingReceipt && (
          <div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
              <div><strong>PO Ref:</strong> <code>{viewingReceipt.poNumber}</code></div>
              <div><strong>Receiving Warehouse:</strong> #{viewingReceipt.warehouseId}</div>
              <div><strong>Received Date:</strong> {new Date(viewingReceipt.receivedDate).toLocaleString()}</div>
              <div><strong>Received By:</strong> {viewingReceipt.receivedBy}</div>
            </div>

            {viewingReceipt.notes && (
              <div style={{ marginBottom: '16px', color: '#475569' }}>
                <strong>Notes / Carrier Ref:</strong> {viewingReceipt.notes}
              </div>
            )}

            <h4 style={{ fontSize: '14px', marginBottom: '8px' }}>Received Items</h4>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Product SKU</th>
                  <th>Quantity Ingested</th>
                  <th>Notes</th>
                </tr>
              </thead>
              <tbody>
                {viewingReceipt.items?.map((it, idx) => (
                  <tr key={idx}>
                    <td><code>{it.productSku}</code></td>
                    <td><strong style={{ color: '#10b981' }}>+{it.quantityReceived}</strong></td>
                    <td>{it.notes || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="modal-footer" style={{ margin: '20px -24px -24px', padding: '16px 24px' }}>
              <button className="btn btn-secondary" onClick={() => setViewingReceipt(null)}>
                Close
              </button>
            </div>
          </div>
        )}
      </Modal>

      {/* Process Inbound Receipt Modal */}
      <Modal
        isOpen={isProcessModalOpen}
        onClose={() => setIsProcessModalOpen(false)}
        title={`Record Inbound Goods for PO: ${selectedPo?.poNumber}`}
        maxWidth="800px"
      >
        {modalError && <div className="alert alert-danger">{modalError}</div>}
        <form onSubmit={handleSubmitReceipt}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label">Destination Warehouse *</label>
              <select
                className="form-select"
                value={warehouseId}
                onChange={(e) => setWarehouseId(Number(e.target.value))}
                required
              >
                {warehouses.map((w) => (
                  <option key={w.id} value={w.id}>
                    {w.name} ({w.location})
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Carrier Tracking / Packing Slip No.</label>
              <input
                type="text"
                className="form-input"
                value={receiptNotes}
                onChange={(e) => setReceiptNotes(e.target.value)}
                placeholder="e.g. Waybill #FEDEX-9921"
              />
            </div>
          </div>

          <div style={{ marginBottom: '16px' }}>
            <label className="form-label">Inspect & Record Received Quantities *</label>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Product SKU</th>
                  <th>Product Name</th>
                  <th>Remaining</th>
                  <th style={{ width: '130px' }}>Qty Receiving</th>
                  <th>Line Notes</th>
                </tr>
              </thead>
              <tbody>
                {receiptItems.map((it, idx) => (
                  <tr key={it.poItemId}>
                    <td><code>{it.productSku}</code></td>
                    <td>{it.productName}</td>
                    <td><strong>{it.maxAllowed}</strong></td>
                    <td>
                      <input
                        type="number"
                        min="0"
                        max={it.maxAllowed}
                        className="form-input"
                        value={it.quantityReceived}
                        onChange={(e) => handleItemQtyChange(idx, parseInt(e.target.value) || 0)}
                        required
                      />
                    </td>
                    <td>
                      <input
                        type="text"
                        className="form-input"
                        placeholder="Condition / lot #"
                        value={it.notes}
                        onChange={(e) => {
                          const updated = [...receiptItems];
                          updated[idx].notes = e.target.value;
                          setReceiptItems(updated);
                        }}
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="modal-footer" style={{ margin: '20px -24px -24px', padding: '16px 24px' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setIsProcessModalOpen(false)}
              disabled={submitting}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-success" disabled={submitting}>
              {submitting ? 'Updating Inventory...' : 'Confirm Receipt & Ingest Stock'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
