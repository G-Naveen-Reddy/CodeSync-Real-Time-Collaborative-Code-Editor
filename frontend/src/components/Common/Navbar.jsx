import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';

const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/">
          <span className="navbar-logo">{'{ }'}</span>
          <span className="navbar-title">CodeSync</span>
        </Link>
      </div>

      <div className="navbar-menu">
        {isAuthenticated ? (
          <>
            <Link to="/dashboard" className="nav-link">Dashboard</Link>
            <Link to="/rooms" className="nav-link">Rooms</Link>
            <div className="navbar-user">
              <span className="user-avatar">
                {user?.username?.charAt(0).toUpperCase()}
              </span>
              <span className="user-name">{user?.username}</span>
              <button className="btn btn-logout" onClick={handleLogout}>
                Logout
              </button>
            </div>
          </>
        ) : (
          <>
            <Link to="/login" className="nav-link">Login</Link>
            <Link to="/register" className="nav-link btn btn-primary">
              Sign Up
            </Link>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
