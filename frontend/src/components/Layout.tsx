import React from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const Layout: React.FC = () => {
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-layout">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="brand-icon">📦</div>
          <div className="brand-text">
            <h2>PIMS Enterprise</h2>
            <span className="brand-subtitle">Procurement & Inventory</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          <div className="nav-group-title">MAIN</div>
          <NavLink to="/" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`} end>
            <span className="nav-icon">📊</span>
            <span>Dashboard</span>
          </NavLink>

          <div className="nav-group-title">INVENTORY</div>
          <NavLink to="/products" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            <span className="nav-icon">🏷️</span>
            <span>Products</span>
          </NavLink>
          <NavLink to="/inventory" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            <span className="nav-icon">📈</span>
            <span>Stock & Movements</span>
          </NavLink>
          <NavLink to="/warehouses" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            <span className="nav-icon">🏭</span>
            <span>Warehouses</span>
          </NavLink>

          <div className="nav-group-title">PROCUREMENT</div>
          <NavLink to="/suppliers" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            <span className="nav-icon">🤝</span>
            <span>Suppliers</span>
          </NavLink>
          <NavLink to="/requisitions" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            <span className="nav-icon">📝</span>
            <span>Requisitions</span>
          </NavLink>
          {hasRole(['ADMIN', 'APPROVER', 'PROCUREMENT_MANAGER']) && (
            <NavLink to="/approvals" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <span className="nav-icon">✅</span>
              <span>Approvals Queue</span>
            </NavLink>
          )}
          {hasRole(['ADMIN', 'PROCUREMENT_MANAGER', 'PROCUREMENT_OFFICER']) && (
            <NavLink to="/orders" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <span className="nav-icon">📋</span>
              <span>Purchase Orders</span>
            </NavLink>
          )}
          {hasRole(['ADMIN', 'INVENTORY_MANAGER', 'PROCUREMENT_OFFICER', 'PROCUREMENT_MANAGER']) && (
            <NavLink to="/receipts" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <span className="nav-icon">📥</span>
              <span>Goods Receipt</span>
            </NavLink>
          )}

          <div className="nav-group-title">ANALYTICS & ADMIN</div>
          <NavLink to="/reports" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            <span className="nav-icon">📑</span>
            <span>Reports & Spend</span>
          </NavLink>
          {hasRole(['ADMIN']) && (
            <NavLink to="/users" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <span className="nav-icon">👥</span>
              <span>User Management</span>
            </NavLink>
          )}
        </nav>

        <div className="sidebar-footer">
          <div className="system-version">v1.0.0 • Spring Boot + React</div>
        </div>
      </aside>

      {/* Main Container */}
      <div className="main-wrapper">
        <header className="topbar">
          <div className="topbar-left">
            <h1 className="page-heading">Procurement & Inventory Management</h1>
          </div>
          <div className="topbar-right">
            <div className="user-profile">
              <div className="user-avatar">
                {user?.firstName?.[0] || 'U'}
                {user?.lastName?.[0] || ''}
              </div>
              <div className="user-info">
                <span className="user-name">
                  {user?.firstName} {user?.lastName}
                </span>
                <span className="user-role-badge">
                  {user?.roles?.[0] || 'USER'}
                </span>
              </div>
            </div>
            <button className="btn-logout" onClick={handleLogout} title="Sign out">
              <span>Sign Out</span>
            </button>
          </div>
        </header>

        <main className="content-container">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
