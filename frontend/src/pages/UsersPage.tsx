import React, { useEffect, useState } from 'react';
import { apiClient } from '../services/api';
import { ApiResponse, RoleName, User } from '../types';
import { Modal } from '../components/Modal';

const ALL_ROLES: RoleName[] = [
  'ADMIN',
  'PROCUREMENT_MANAGER',
  'PROCUREMENT_OFFICER',
  'INVENTORY_MANAGER',
  'APPROVER',
  'EMPLOYEE',
];

export const UsersPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  // Edit Roles Modal
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [userRoles, setUserRoles] = useState<RoleName[]>([]);
  const [modalError, setModalError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get<ApiResponse<User[]>>('/auth/users');
      if (res.data.success) {
        setUsers(res.data.data);
      }
    } catch (e) {
      console.error('Failed to load users', e);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenEditRoles = (u: User) => {
    setSelectedUser(u);
    setUserRoles([...u.roles]);
    setModalError(null);
  };

  const handleRoleToggle = (role: RoleName) => {
    if (userRoles.includes(role)) {
      if (userRoles.length === 1) {
        setModalError('A user must have at least one assigned role.');
        return;
      }
      setUserRoles(userRoles.filter((r) => r !== role));
    } else {
      setUserRoles([...userRoles, role]);
    }
    setModalError(null);
  };

  const handleSaveRoles = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedUser) return;
    setSubmitting(true);
    setModalError(null);

    try {
      await apiClient.put(`/auth/users/${selectedUser.id}/roles`, {
        roles: userRoles,
      });
      setSelectedUser(null);
      fetchUsers();
    } catch (err: any) {
      setModalError(err.response?.data?.message || 'Failed to update roles.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">User & Role Administration</h2>
          <p className="page-subtitle">RBAC configuration, employee profiles, and authority delegations</p>
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="spinner"></div>
        ) : users.length === 0 ? (
          <p style={{ color: '#64748b', fontSize: '13px' }}>No users found.</p>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Full Name</th>
                  <th>Email</th>
                  <th>Department</th>
                  <th>Assigned Roles</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td><strong>{u.username}</strong></td>
                    <td>{u.firstName} {u.lastName}</td>
                    <td><a href={`mailto:${u.email}`}>{u.email}</a></td>
                    <td>{u.department}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                        {u.roles.map((r) => (
                          <span key={r} className="status-badge badge-secondary" style={{ fontSize: '10px' }}>
                            {r}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td>
                      <span className={`status-badge ${u.active ? 'badge-success' : 'badge-danger'}`}>
                        {u.active ? 'ACTIVE' : 'LOCKED'}
                      </span>
                    </td>
                    <td>
                      <button
                        className="btn btn-secondary btn-sm"
                        onClick={() => handleOpenEditRoles(u)}
                      >
                        Edit Roles
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Edit Roles Modal */}
      <Modal
        isOpen={!!selectedUser}
        onClose={() => setSelectedUser(null)}
        title={`Edit Roles for: ${selectedUser?.username}`}
      >
        {modalError && <div className="alert alert-danger">{modalError}</div>}
        <form onSubmit={handleSaveRoles}>
          <div style={{ marginBottom: '16px' }}>
            <p style={{ fontSize: '13px', color: '#475569', marginBottom: '12px' }}>
              Select the system authority roles for <strong>{selectedUser?.firstName} {selectedUser?.lastName}</strong> ({selectedUser?.department}):
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {ALL_ROLES.map((role) => (
                <label
                  key={role}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px',
                    padding: '8px 12px',
                    borderRadius: '6px',
                    backgroundColor: userRoles.includes(role) ? '#eff6ff' : '#f8fafc',
                    border: `1px solid ${userRoles.includes(role) ? '#93c5fd' : '#e2e8f0'}`,
                    cursor: 'pointer',
                  }}
                >
                  <input
                    type="checkbox"
                    checked={userRoles.includes(role)}
                    onChange={() => handleRoleToggle(role)}
                  />
                  <div>
                    <strong style={{ fontSize: '13px' }}>{role}</strong>
                  </div>
                </label>
              ))}
            </div>
          </div>

          <div className="modal-footer" style={{ margin: '20px -24px -24px', padding: '16px 24px' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setSelectedUser(null)}
              disabled={submitting}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : 'Update Roles'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
