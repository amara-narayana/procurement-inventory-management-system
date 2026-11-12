import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../services/api';
import { ApiResponse, AuthResponse } from '../types';

export const LoginPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) {
      setError('Please provide both username and password.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', {
        username,
        password,
      });

      if (response.data.success && response.data.data) {
        login(response.data.data);
        navigate('/');
      } else {
        setError(response.data.message || 'Login failed.');
      }
    } catch (err: any) {
      setError(
        err.response?.data?.message || 'Authentication failed. Please verify your credentials.'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleSelectDemo = (u: string, p: string) => {
    setUsername(u);
    setPassword(p);
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <div style={{ fontSize: '40px', marginBottom: '8px' }}>📦</div>
          <h1>PIMS Enterprise</h1>
          <p>Procurement & Inventory Management System</p>
        </div>

        {error && <div className="alert alert-danger">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="username">
              Username
            </label>
            <input
              id="username"
              type="text"
              className="form-input"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="e.g. admin or employee1"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">
              Password
            </label>
            <input
              id="password"
              type="password"
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', justifyContent: 'center', marginTop: '8px' }}
            disabled={loading}
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <div className="login-demo-accounts">
          <strong>Demo Seed Accounts (Password: password123):</strong>
          <div className="demo-account-list">
            <button
              type="button"
              className="demo-btn"
              onClick={() => handleSelectDemo('admin', 'password123')}
            >
              <span><strong>admin</strong> (System Admin)</span>
              <span>Select →</span>
            </button>
            <button
              type="button"
              className="demo-btn"
              onClick={() => handleSelectDemo('manager.proc', 'password123')}
            >
              <span><strong>manager.proc</strong> (Procurement Manager)</span>
              <span>Select →</span>
            </button>
            <button
              type="button"
              className="demo-btn"
              onClick={() => handleSelectDemo('officer.proc', 'password123')}
            >
              <span><strong>officer.proc</strong> (Procurement Officer)</span>
              <span>Select →</span>
            </button>
            <button
              type="button"
              className="demo-btn"
              onClick={() => handleSelectDemo('manager.inv', 'password123')}
            >
              <span><strong>manager.inv</strong> (Inventory Manager)</span>
              <span>Select →</span>
            </button>
            <button
              type="button"
              className="demo-btn"
              onClick={() => handleSelectDemo('approver1', 'password123')}
            >
              <span><strong>approver1</strong> (Approver)</span>
              <span>Select →</span>
            </button>
            <button
              type="button"
              className="demo-btn"
              onClick={() => handleSelectDemo('employee1', 'password123')}
            >
              <span><strong>employee1</strong> (Employee)</span>
              <span>Select →</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
