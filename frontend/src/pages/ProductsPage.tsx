import React, { useEffect, useState } from 'react';
import { apiClient } from '../services/api';
import { ApiResponse, Category, Product } from '../types';
import { Modal } from '../components/Modal';
import { useAuth } from '../context/AuthContext';

export const ProductsPage: React.FC = () => {
  const { hasRole } = useAuth();
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  
  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [formData, setFormData] = useState({
    sku: '',
    name: '',
    description: '',
    categoryId: 0,
    unitOfMeasurement: 'PCS',
    unitPrice: 0,
    reorderThreshold: 10,
    active: true,
  });
  const [modalError, setModalError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchProducts();
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      const res = await apiClient.get<ApiResponse<Category[]>>('/inventory/categories');
      if (res.data.success) {
        setCategories(res.data.data);
      }
    } catch (e) {
      console.error('Failed to load categories', e);
    }
  };

  const fetchProducts = async () => {
    setLoading(true);
    try {
      let url = '/inventory/products?activeOnly=false';
      if (search) url += `&keyword=${encodeURIComponent(search)}`;
      if (selectedCategory) url += `&categoryId=${selectedCategory}`;

      const res = await apiClient.get<ApiResponse<{ content: Product[] }>>(url);
      if (res.data.success) {
        setProducts(res.data.data.content);
      }
    } catch (e) {
      console.error('Failed to load products', e);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreateModal = () => {
    setEditingProduct(null);
    setFormData({
      sku: '',
      name: '',
      description: '',
      categoryId: categories[0]?.id || 1,
      unitOfMeasurement: 'PCS',
      unitPrice: 0.0,
      reorderThreshold: 10,
      active: true,
    });
    setModalError(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (product: Product) => {
    setEditingProduct(product);
    setFormData({
      sku: product.sku,
      name: product.name,
      description: product.description || '',
      categoryId: product.categoryId,
      unitOfMeasurement: product.unitOfMeasurement,
      unitPrice: product.unitPrice,
      reorderThreshold: product.reorderThreshold,
      active: product.active,
    });
    setModalError(null);
    setIsModalOpen(true);
  };

  const handleSaveProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setModalError(null);

    try {
      if (editingProduct) {
        // Update product
        await apiClient.put(`/inventory/products/${editingProduct.id}`, formData);
      } else {
        // Create product
        await apiClient.post('/inventory/products', formData);
      }
      setIsModalOpen(false);
      fetchProducts();
    } catch (err: any) {
      setModalError(err.response?.data?.message || 'Failed to save product.');
    } finally {
      setSubmitting(false);
    }
  };

  const canManageProducts = hasRole(['ADMIN', 'INVENTORY_MANAGER']);

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Product Catalog</h2>
          <p className="page-subtitle">Master records of organizational assets, SKUs, and reorder levels</p>
        </div>
        <div className="page-actions">
          {canManageProducts && (
            <button className="btn btn-primary" onClick={handleOpenCreateModal}>
              + Create Product
            </button>
          )}
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="card" style={{ padding: '16px', marginBottom: '16px' }}>
        <div className="filter-bar" style={{ margin: 0 }}>
          <div style={{ flex: '1 1 250px' }}>
            <input
              type="text"
              className="form-input"
              placeholder="Search by SKU or Product Name..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchProducts()}
            />
          </div>
          <div style={{ width: '200px' }}>
            <select
              className="form-select"
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
            >
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>
          <button className="btn btn-secondary" onClick={fetchProducts}>
            Filter
          </button>
        </div>
      </div>

      {/* Products Table */}
      <div className="card">
        {loading ? (
          <div className="spinner"></div>
        ) : products.length === 0 ? (
          <p style={{ color: '#64748b', fontSize: '13px' }}>No products found matching the criteria.</p>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>SKU</th>
                  <th>Product Name</th>
                  <th>Category</th>
                  <th>Unit of Measure</th>
                  <th>Unit Price</th>
                  <th>Total Stock</th>
                  <th>Threshold</th>
                  <th>Status</th>
                  {canManageProducts && <th>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {products.map((p) => (
                  <tr key={p.id}>
                    <td><code>{p.sku}</code></td>
                    <td>
                      <strong>{p.name}</strong>
                      {p.description && (
                        <div style={{ fontSize: '11px', color: '#64748b' }}>{p.description}</div>
                      )}
                    </td>
                    <td>{p.categoryName || 'General'}</td>
                    <td>{p.unitOfMeasurement}</td>
                    <td>${p.unitPrice.toFixed(2)}</td>
                    <td>
                      <span
                        style={{
                          fontWeight: 'bold',
                          color: p.totalStock <= p.reorderThreshold ? '#ef4444' : '#10b981',
                        }}
                      >
                        {p.totalStock}
                      </span>
                    </td>
                    <td>{p.reorderThreshold}</td>
                    <td>
                      <span className={`status-badge ${p.active ? 'badge-success' : 'badge-secondary'}`}>
                        {p.active ? 'ACTIVE' : 'INACTIVE'}
                      </span>
                    </td>
                    {canManageProducts && (
                      <td>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => handleOpenEditModal(p)}
                        >
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

      {/* Create / Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingProduct ? `Edit Product: ${editingProduct.sku}` : 'Create New Product'}
      >
        {modalError && <div className="alert alert-danger">{modalError}</div>}
        <form onSubmit={handleSaveProduct}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label">SKU *</label>
              <input
                type="text"
                className="form-input"
                value={formData.sku}
                onChange={(e) => setFormData({ ...formData, sku: e.target.value })}
                placeholder="e.g. IT-LAP-001"
                required
                disabled={!!editingProduct}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Product Name *</label>
              <input
                type="text"
                className="form-input"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="e.g. Enterprise Laptop"
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea
              className="form-textarea"
              rows={2}
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Product specifications..."
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label">Category *</label>
              <select
                className="form-select"
                value={formData.categoryId}
                onChange={(e) => setFormData({ ...formData, categoryId: Number(e.target.value) })}
                required
              >
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Unit of Measurement</label>
              <input
                type="text"
                className="form-input"
                value={formData.unitOfMeasurement}
                onChange={(e) => setFormData({ ...formData, unitOfMeasurement: e.target.value })}
                placeholder="e.g. PCS, BOX, KG"
                required
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label">Unit Price ($) *</label>
              <input
                type="number"
                step="0.01"
                min="0"
                className="form-input"
                value={formData.unitPrice}
                onChange={(e) => setFormData({ ...formData, unitPrice: parseFloat(e.target.value) || 0 })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Reorder Threshold *</label>
              <input
                type="number"
                min="0"
                className="form-input"
                value={formData.reorderThreshold}
                onChange={(e) => setFormData({ ...formData, reorderThreshold: parseInt(e.target.value) || 0 })}
                required
              />
            </div>
          </div>

          <div className="form-group" style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '8px' }}>
            <input
              type="checkbox"
              id="activeCheckbox"
              checked={formData.active}
              onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
            />
            <label htmlFor="activeCheckbox" style={{ fontSize: '13px', cursor: 'pointer' }}>
              Active Product (Available for ordering)
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
              {submitting ? 'Saving...' : editingProduct ? 'Update Product' : 'Create Product'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
