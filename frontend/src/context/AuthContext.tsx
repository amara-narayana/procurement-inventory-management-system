import React, { createContext, useContext, useState, useEffect } from 'react';
import { AuthResponse, RoleName } from '../types';
import { apiClient } from '../services/api';

interface AuthContextType {
  user: AuthResponse | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (data: AuthResponse) => void;
  logout: () => void;
  hasRole: (roles: RoleName | RoleName[]) => boolean;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AuthResponse | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedToken = localStorage.getItem('pims_token');
    const savedUser = localStorage.getItem('pims_user');

    if (savedToken && savedUser) {
      try {
        const parsedUser = JSON.parse(savedUser) as AuthResponse;
        setUser(parsedUser);
        setToken(savedToken);
      } catch (e) {
        localStorage.removeItem('pims_token');
        localStorage.removeItem('pims_user');
      }
    }
    setLoading(false);
  }, []);

  const login = (data: AuthResponse) => {
    localStorage.setItem('pims_token', data.token);
    localStorage.setItem('pims_user', JSON.stringify(data));
    setToken(data.token);
    setUser(data);
  };

  const logout = () => {
    localStorage.removeItem('pims_token');
    localStorage.removeItem('pims_user');
    setToken(null);
    setUser(null);
  };

  const hasRole = (roles: RoleName | RoleName[]): boolean => {
    if (!user || !user.roles) return false;
    const requiredRoles = Array.isArray(roles) ? roles : [roles];
    return user.roles.some((r) => requiredRoles.includes(r as RoleName));
  };

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated: !!token, login, logout, hasRole, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
