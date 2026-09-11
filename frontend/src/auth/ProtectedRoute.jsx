import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

/**
 * UX-level gate only. The backend's @PreAuthorize checks are the real enforcement —
 * this just avoids flashing an owner/client screen at someone who shouldn't see it.
 */
export function ProtectedRoute({ role, children }) {
  const { isAuthenticated, role: currentRole } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (role && currentRole !== role) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
}
