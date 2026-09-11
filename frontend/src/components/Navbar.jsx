import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function Navbar() {
  const { isAuthenticated, role, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <Link to="/" className="brand">RentalPlatform</Link>
      <div className="nav-links">
        {!isAuthenticated && (
          <>
            <Link to="/login">Log in</Link>
            <Link to="/register">Register</Link>
          </>
        )}
        {isAuthenticated && role === 'OWNER' && <Link to="/owner">Dashboard</Link>}
        {isAuthenticated && role === 'CLIENT' && (
          <>
            <Link to="/client">Search</Link>
            <Link to="/client/bookings">My bookings</Link>
          </>
        )}
        {isAuthenticated && <button onClick={handleLogout}>Log out</button>}
      </div>
    </nav>
  );
}
