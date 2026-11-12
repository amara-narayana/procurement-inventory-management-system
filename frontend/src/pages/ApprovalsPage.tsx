import React, { useEffect, useState } from 'react';
import { apiClient } from '../services/api';
import { ApiResponse, PurchaseRequisition } from '../types';
import { Modal } from '../components/Modal';
import { useAuth } from '../context/AuthContext';

export const ApprovalsPage: React.FC = () => {
  const { user } = useAuth();
  const [pendingRequisitions, setPendingRequisitions] = useState<PurchaseRequisition[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedReq, setSelectedReq] = useState<PurchaseRequisition | null>(null);
  const [approvalNotes, setApprovalNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  useEffect(() => {
    fetchPendingRequisitions();
  }, []);

  const fetchPendingRequisitions = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get<ApiResponse<{ content: PurchaseRequisition[] }>>(
        '/procurement/requisitions?status=SUBMITTED&size=100'
      );
      if (res.data.success) {
        setPendingRequisitions(res.data.data.content);
      }
    } catch (e) {
      console.error('Failed to load pending requisitions', e);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenReview = (req: PurchaseRequisition) => {
    setSelectedReq(req);
    setApprovalNotes('');
    setActionError(null);
  };

  const handleApprove = async () => {
    if (!selectedReq) return;
    setSubmitting(true);
    setActionError(null);

    try {
      await apiClient.post(`/procurement/requisitions/${selectedReq.id}/approve`, {
        notes: approvalNotes,
      });
      setSelectedReq(null);
      fetchPendingRequisitions();
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to approve requisition.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleReject = async () => {
    if (!selectedReq) return;
    if (!approvalNotes.trim()) {
      setActionError('A rejection note or reason is required.');
      return;
    }

    setSubmitting(true);
    setActionError(null);

    try {
      await apiClient.post(`/procurement/requisitions/${selectedReq.id}/reject`, {
        notes: approvalNotes,
      });
      setSelectedReq(null);
      fetchPendingRequisitions();
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to reject requisition.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Requisition Approval Queue</h2>
          <p className="page-subtitle">Governance review and financial authorization for departmental purchase demands</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-secondary" onClick={fetchPendingRequisitions}>
            🔄 Refresh Queue
          </button>
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="spinner"></div>
        ) : pendingRequisitions.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px 20px', color: '#64748b' }}>
            <div style={{ fontSize: '32px', marginBottom: '8px' }}>🎉</div>
            <p><strong>Your approval queue is clear.</strong></p>
            <p style={{ fontSize: '13px', marginTop: '4px' }}>There are no submitted purchase requisitions awaiting authorization.</p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Requisition No.</th>
                  <th>Requester</th>
                  <th>Department</th>
                  <th>Estimated Total</th>
                  <th>Submitted Date</th>
                  <th>Segregation Check</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {pendingRequisitions.map((req) => {
                  const isOwnRequisition = req.requestingUser === user?.username;
                  return (
                    <tr key={req.id}>
                      <td><strong>{req.requisitionNumber}</strong></td>
                      <td>{req.requestingUser}</td>
                      <td>{req.department}</td>
                      <td>
                        <strong>${req.totalEstimatedAmount.toLocaleString('en-US', { minimumFractionDigits: 2 })}</strong>
                      </td>
                      <td>{req.submissionDate ? new Date(req.submissionDate).toLocaleString() : '-'}</td>
                      <td>
                        {isOwnRequisition ? (
                          <span className="status-badge badge-warning">SELF-REQUISITION</span>
                        ) : (
                          <span className="status-badge badge-success">READY FOR REVIEW</span>
                        )}
                      </td>
                      <td>
                        <button
                          className="btn btn-primary btn-sm"
                          onClick={() => handleOpenReview(req)}
                        >
                          Review & Decide
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Review Modal */}
      <Modal
        isOpen={!!selectedReq}
        onClose={() => setSelectedReq(null)}
        title={`Review Requisition: ${selectedReq?.requisitionNumber}`}
        maxWidth="750px"
      >
        {selectedReq && (
          <div>
            {actionError && <div className="alert alert-danger">{actionError}</div>}

            {selectedReq.requestingUser === user?.username && (
              <div className="alert alert-warning">
                <strong>Policy Notice:</strong> You submitted this requisition. Enterprise policy prohibits approving your own procurement requests.
              </div>
            )}

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
              <div>
                <strong>Requester:</strong> {selectedReq.requestingUser}
              </div>
              <div>
                <strong>Department:</strong> {selectedReq.department}
              </div>
              <div>
                <strong>Total Estimate:</strong> ${selectedReq.totalEstimatedAmount.toFixed(2)}
              </div>
              <div>
                <strong>Submitted At:</strong> {selectedReq.submissionDate ? new Date(selectedReq.submissionDate).toLocaleString() : '-'}
              </div>
            </div>

            <div style={{ marginBottom: '16px' }}>
              <strong>Business Justification:</strong>
              <p style={{ marginTop: '4px', color: '#475569' }}>{selectedReq.businessJustification}</p>
            </div>

            <h4 style={{ fontSize: '14px', marginBottom: '8px' }}>Line Items</h4>
            <table className="data-table" style={{ marginBottom: '16px' }}>
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Quantity</th>
                  <th>Estimated Price</th>
                  <th>Line Total</th>
                </tr>
              </thead>
              <tbody>
                {selectedReq.items.map((it, idx) => (
                  <tr key={idx}>
                    <td>
                      <code>{it.productSku}</code> - {it.productName}
                    </td>
                    <td>{it.quantity}</td>
                    <td>${it.estimatedUnitPrice.toFixed(2)}</td>
                    <td>${it.lineTotal.toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="form-group">
              <label className="form-label">Review / Decision Notes</label>
              <textarea
                className="form-textarea"
                rows={3}
                value={approvalNotes}
                onChange={(e) => setApprovalNotes(e.target.value)}
                placeholder="Required for rejection, optional for approval..."
              />
            </div>

            <div className="modal-footer" style={{ margin: '20px -24px -24px', padding: '16px 24px', justifyContent: 'space-between' }}>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setSelectedReq(null)}
                disabled={submitting}
              >
                Close
              </button>
              <div style={{ display: 'flex', gap: '8px' }}>
                <button
                  type="button"
                  className="btn btn-danger"
                  onClick={handleReject}
                  disabled={submitting}
                >
                  ✕ Reject Requisition
                </button>
                <button
                  type="button"
                  className="btn btn-success"
                  onClick={handleApprove}
                  disabled={submitting || selectedReq.requestingUser === user?.username}
                >
                  ✓ Approve Requisition
                </button>
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};
