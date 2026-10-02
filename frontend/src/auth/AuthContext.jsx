import { createContext, useContext, useMemo, useState } from 'react';
import { authApi } from '../api/auth';
import { setTokens, clearTokens, getTokens } from '../api/client';

const AuthContext = createContext(null);

function decodeRole() {
  const { accessToken } = getTokens();
  if (!accessToken) return null;
  // Role isn't embedded in the JWT claims in this backend — it's returned
  // alongside the tokens on login/register and cached separately here.
  return localStorage.getItem('role');
}

export function AuthProvider({ children }) {
  const [role, setRole] = useState(decodeRole());
  const [user, setUser] = useState(null);

  const login = async (email, password) => {
    const data = await authApi.login({ email, password });
    setTokens(data);
    localStorage.setItem('role', data.role);
    setRole(data.role);
    setUser(data);
    return data;
  };

  const register = async (payload) => {
    const data = await authApi.register(payload);
    setTokens(data);
    localStorage.setItem('role', data.role);
    setRole(data.role);
    setUser(data);
    return data;
  };

  const logout = () => {
    clearTokens();
    setRole(null);
    setUser(null);
  };

  const value = useMemo(
    () => ({ role, user, isAuthenticated: !!getTokens().accessToken, login, register, logout }),
    [role, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
