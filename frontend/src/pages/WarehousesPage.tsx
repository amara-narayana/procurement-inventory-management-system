import React, { useEffect, useState } from 'react';
import { apiClient } from '../services/api';
import { ApiResponse, Warehouse } from '../types';
import { Modal } from '../components/Modal';
import { useAuth } from '../context/AuthContext';

export const WarehousesPage: React.FC = () => {
  const { hasRole } = useAuth();
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingWarehouse, setEditingWarehouse] = useState<Warehouse | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    location: '',
    description: '',
    active: true,
  });
  const [modalError, setModalError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchWarehouses();
  }, []);

  const fetchWarehouses = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get<ApiResponse<Warehouse[]>>('/inventory/warehouses');
      if (res.data.success) {
        setWarehouses(res.data.data);
      }
    } catch (e) {
      console.error('Failed to load warehouses', e);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreateModal = () => {
    setEditingWarehouse(null);
    setFormData({
      name: '',
      location: '',
      description: '',
      active: true,
    });
    setModalError(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (w: Warehouse) => {
    setEditingWarehouse(w);
    setFormData({
      name: w.name,
      location: w.location,
      description: w.description || '',
      active: w.active,
    });
    setModalError(null);
    setIsModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setModalError(null);

    try {
      if (editingWarehouse) {
        await apiClient.put(`/inventory/warehouses/${editingWarehouse.id}`, formData);
      } else {
        await apiClient.post('/inventory/warehouses', formData);
      }
      setIsModalOpen(false);
      fetchWarehouses();
    } catch (err: any) {
      setModalError(err.response?.data?.message || 'Failed to save warehouse.');
    } finally {
      setSubmitting(false);
    }
  };

  const canManage = hasRole(['ADMIN', 'INVENTORY_MANAGER']);

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Warehouse Management</h2>
          <p className="page-subtitle">Configure storage distribution centers, plant sites, and fulfillment depots</p>
        </div>
        <div className="page-actions">
          {canManage && (
            <button className="btn btn-primary" onClick={handleOpenCreateModal}>
              + Add Warehouse
            </button>
          )}
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="spinner"></div>
        ) : warehouses.length === 0 ? (
          <p style={{ color: '#64748b', fontSize: '13px' }}>No warehouses configured yet.</p>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Warehouse Name</th>
                  <th>Location</th>
                  <th>Description</th>
                  <th>Status</th>
                  {canManage && <th>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {warehouses.map((w) => (
                  <tr key={w.id}>
                    <td>#{w.id}</td>
                    <td><strong>{w.name}</strong></td>
                    <td>{w.location}</td>
                    <td>{w.description || '-'}</td>
                    <td>
                      <span className={`status-badge ${w.active ? 'badge-success' : 'badge-secondary'}`}>
                        {w.active ? 'ACTIVE' : 'INACTIVE'}
                      </span>
                    </td>
                    {canManage && (
                      <td>
                        <button className="btn btn-secondary btn-sm" onClick={() => handleOpenEditModal(w)}>
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
        title={editingWarehouse ? `Edit Warehouse #${editingWarehouse.id}` : 'Create Warehouse'}
      >
        {modalError && <div className="alert alert-danger">{modalError}</div>}
        <form onSubmit={handleSave}>
          <div className="form-group">
            <label className="form-label">Warehouse Name *</label>
            <input
              type="text"
              className="form-input"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="e.g. Central Logistics Hub"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Location / Address *</label>
            <input
              type="text"
              className="form-input"
              value={formData.location}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
              placeholder="e.g. Building 4, North Cargo Sector"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea
              className="form-textarea"
              rows={2}
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Storage capacity, docking notes, etc."
            />
          </div>

          <div className="form-group" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <input
              type="checkbox"
              id="activeWarehouse"
              checked={formData.active}
              onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
            />
            <label htmlFor="activeWarehouse" style={{ fontSize: '13px', cursor: 'pointer' }}>
              Active Warehouse (Eligible for goods receipt and stock allocation)
            </label>
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
              {submitting ? 'Saving...' : editingWarehouse ? 'Update Warehouse' : 'Create Warehouse'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
