import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

const DashboardPage = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    totalRooms: 0,
    myRooms: 0,
    activeRooms: 0,
  });
  const [recentRooms, setRecentRooms] = useState([]);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const [roomsResponse, myRoomsResponse] = await Promise.all([
        api.get('/rooms'),
        api.get('/rooms/my'),
      ]);

      const allRooms = roomsResponse.data;
      const myRooms = myRoomsResponse.data;

      setStats({
        totalRooms: allRooms.length,
        myRooms: myRooms.length,
        activeRooms: allRooms.filter((r) => r.isActive).length,
      });

      setRecentRooms(allRooms.slice(0, 4));
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    }
  };

  return (
    <div className="page">
      <div className="dashboard-container">
        <div className="dashboard-header">
          <div>
            <h1>Welcome back, {user?.username || 'User'}!</h1>
            <p className="subtitle">Ready to collaborate? Here's your overview.</p>
          </div>
          <Link to="/rooms/create" className="btn btn-primary">
            + New Room
          </Link>
        </div>

        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon total">📊</div>
            <div className="stat-info">
              <span className="stat-value">{stats.totalRooms}</span>
              <span className="stat-label">Total Rooms</span>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon mine">👤</div>
            <div className="stat-info">
              <span className="stat-value">{stats.myRooms}</span>
              <span className="stat-label">My Rooms</span>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon active">🔥</div>
            <div className="stat-info">
              <span className="stat-value">{stats.activeRooms}</span>
              <span className="stat-label">Active Now</span>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon languages">🌐</div>
            <div className="stat-info">
              <span className="stat-value">10+</span>
              <span className="stat-label">Languages</span>
            </div>
          </div>
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <h2>Recent Rooms</h2>
            <Link to="/rooms" className="view-all">View All</Link>
          </div>

          {recentRooms.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📝</div>
              <h3>No rooms yet</h3>
              <p>Create your first collaborative coding room!</p>
              <Link to="/rooms/create" className="btn btn-primary">
                Create Room
              </Link>
            </div>
          ) : (
            <div className="recent-rooms-grid">
              {recentRooms.map((room) => (
                <Link to={`/editor/${room.id}`} key={room.id} className="room-card-mini">
                  <div className="room-card-mini-header">
                    <span className="language-tag">{room.document?.language || 'N/A'}</span>
                    <span className={`status-dot ${room.isActive ? 'online' : 'offline'}`}></span>
                  </div>
                  <h4>{room.name}</h4>
                  <p>{room.description || 'No description'}</p>
                  <span className="room-meta">
                    {room.createdBy?.username} • {new Date(room.createdAt).toLocaleDateString()}
                  </span>
                </Link>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
