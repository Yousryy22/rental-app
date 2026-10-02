import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from '../auth/ProtectedRoute';
import { useAuth } from '../auth/AuthContext';

vi.mock('../auth/AuthContext', () => ({
  useAuth: vi.fn(),
}));

function renderWithRoute(initialPath) {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <Routes>
        <Route path="/login" element={<div>Login page</div>} />
        <Route path="/unauthorized" element={<div>Unauthorized page</div>} />
        <Route
          path="/owner"
          element={
            <ProtectedRoute role="OWNER">
              <div>Owner dashboard</div>
            </ProtectedRoute>
          }
        />
      </Routes>
    </MemoryRouter>
  );
}

describe('ProtectedRoute', () => {
  it('redirects unauthenticated users to /login', () => {
    useAuth.mockReturnValue({ isAuthenticated: false, role: null });
    renderWithRoute('/owner');
    expect(screen.getByText('Login page')).toBeInTheDocument();
  });

  it('redirects wrong-role users to /unauthorized', () => {
    useAuth.mockReturnValue({ isAuthenticated: true, role: 'CLIENT' });
    renderWithRoute('/owner');
    expect(screen.getByText('Unauthorized page')).toBeInTheDocument();
  });

  it('renders the protected content for the correct role', () => {
    useAuth.mockReturnValue({ isAuthenticated: true, role: 'OWNER' });
    renderWithRoute('/owner');
    expect(screen.getByText('Owner dashboard')).toBeInTheDocument();
  });
});
