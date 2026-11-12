import React, { useEffect, useState } from 'react';
import { apiClient } from '../services/api';
import { ApiResponse, Supplier, SupplierStatus } from '../types';
import { Modal } from '../components/Modal';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';

export const SuppliersPage: React.FC = () => {
  const { hasRole } = useAuth();
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string>('');
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingSupplier, setEditingSupplier] = useState<Supplier | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    contactPerson: '',
    email: '',
    phone: '',
    address: '',
    taxId: '',
    status: 'ACTIVE' as SupplierStatus,
  });
  const [modalError, setModalError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchSuppliers();
  }, [statusFilter]);

  const fetchSuppliers = async () => {
    setLoading(true);
    try {
      let url = '/procurement/suppliers?size=100';
      if (statusFilter) url += `&status=${statusFilter}`;
      const res = await apiClient.get<ApiResponse<{ content: Supplier[] }>>(url);
      if (res.data.success) {
        setSuppliers(res.data.data.content);
      }
    } catch (e) {
      console.error('Failed to load suppliers', e);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreateModal = () => {
    setEditingSupplier(null);
    setFormData({
      name: '',
      contactPerson: '',
      email: '',
      phone: '',
      address: '',
      taxId: '',
      status: 'ACTIVE',
    });
    setModalError(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (s: Supplier) => {
    setEditingSupplier(s);
    setFormData({
      name: s.name,
      contactPerson: s.contactPerson || '',
      email: s.email,
      phone: s.phone || '',
      address: s.address || '',
      taxId: s.taxId || '',
      status: s.status,
    });
    setModalError(null);
    setIsModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setModalError(null);

    try {
      if (editingSupplier) {
        await apiClient.put(`/procurement/suppliers/${editingSupplier.id}`, formData);
      } else {
        await apiClient.post('/procurement/suppliers', formData);
      }
      setIsModalOpen(false);
      fetchSuppliers();
    } catch (err: any) {
      setModalError(err.response?.data?.message || 'Failed to save supplier details.');
    } finally {
      setSubmitting(false);
    }
  };

  const canManage = hasRole(['ADMIN', 'PROCUREMENT_MANAGER', 'PROCUREMENT_OFFICER']);

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Vendor & Supplier Directory</h2>
          <p className="page-subtitle">Maintain accredited vendor records, tax identifiers, and contact details</p>
        </div>
        <div className="page-actions">
          {canManage && (
            <button className="btn btn-primary" onClick={handleOpenCreateModal}>
              + Add Supplier
            </button>
          )}
        </div>
      </div>

      <div className="card" style={{ padding: '16px', marginBottom: '16px' }}>
        <div className="filter-bar" style={{ margin: 0 }}>
          <div style={{ width: '200px' }}>
            <select
              className="form-select"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
              <option value="BLACKLISTED">BLACKLISTED</option>
            </select>
          </div>
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="spinner"></div>
        ) : suppliers.length === 0 ? (
          <p style={{ color: '#64748b', fontSize: '13px' }}>No suppliers found.</p>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Vendor Name</th>
                  <th>Contact Person</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Tax ID</th>
                  <th>Status</th>
                  {canManage && <th>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {suppliers.map((s) => (
                  <tr key={s.id}>
                    <td>
                      <strong>{s.name}</strong>
                      {s.address && (
                        <div style={{ fontSize: '11px', color: '#64748b' }}>{s.address}</div>
                      )}
                    </td>
                    <td>{s.contactPerson || '-'}</td>
                    <td><a href={`mailto:${s.email}`}>{s.email}</a></td>
                    <td>{s.phone || '-'}</td>
                    <td><code>{s.taxId || '-'}</code></td>
                    <td><StatusBadge status={s.status} /></td>
                    {canManage && (
                      <td>
                        <button className="btn btn-secondary btn-sm" onClick={() => handleOpenEditModal(s)}>
                          Edit
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

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingSupplier ? `Edit Supplier: ${editingSupplier.name}` : 'Onboard New Supplier'}
      >
        {modalError && <div className="alert alert-danger">{modalError}</div>}
        <form onSubmit={handleSave}>
          <div className="form-group">
            <label className="form-label">Company / Vendor Name *</label>
            <input
              type="text"
              className="form-input"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="e.g. Dell Technologies Inc."
              required
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label">Contact Person</label>
              <input
                type="text"
                className="form-input"
                value={formData.contactPerson}
                onChange={(e) => setFormData({ ...formData, contactPerson: e.target.value })}
                placeholder="e.g. John Doe"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Email *</label>
              <input
                type="email"
                className="form-input"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                placeholder="sales@vendor.com"
                required
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label">Phone</label>
              <input
                type="text"
                className="form-input"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                placeholder="+1 555-0199"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Tax ID / VAT No.</label>
              <input
                type="text"
                className="form-input"
                value={formData.taxId}
                onChange={(e) => setFormData({ ...formData, taxId: e.target.value })}
                placeholder="US-EIN-99214"
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Business Address</label>
            <input
              type="text"
              className="form-input"
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              placeholder="123 Tech Way, Austin, TX"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Accreditation Status *</label>
            <select
              className="form-select"
              value={formData.status}
              onChange={(e) => setFormData({ ...formData, status: e.target.value as SupplierStatus })}
            >
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
              <option value="BLACKLISTED">BLACKLISTED</option>
            </select>
          </div>

          <div className="modal-footer" style={{ margin: '20px -24px -24px', padding: '16px 24px' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setIsModalOpen(false)}
              disabled={submitting}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : editingSupplier ? 'Update Supplier' : 'Register Supplier'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
