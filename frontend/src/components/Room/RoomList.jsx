import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import toast from 'react-hot-toast';

const RoomList = () => {
  const [rooms, setRooms] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [filter, setFilter] = useState('all');
  const navigate = useNavigate();

  useEffect(() => {
    fetchRooms();
  }, []);

  const fetchRooms = async () => {
    try {
      setIsLoading(true);
      const response = await api.get('/rooms');
      setRooms(response.data);
    } catch (error) {
      toast.error('Failed to fetch rooms');
      console.error('Error fetching rooms:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleJoinRoom = (roomId) => {
    navigate(`/editor/${roomId}`);
  };

  const filteredRooms =
    filter === 'all'
      ? rooms
      : rooms.filter((room) => room.createdBy?.username === filter);

  if (isLoading) {
    return (
      <div className="rooms-loading">
        <div className="spinner"></div>
        <p>Loading rooms...</p>
      </div>
    );
  }

  return (
    <div className="room-list-container">
      <div className="room-list-header">
        <h2>Active Rooms</h2>
        <div className="room-filters">
          <button
            className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
            onClick={() => setFilter('all')}
          >
            All Rooms
          </button>
          <button
            className={`filter-btn ${filter === 'my' ? 'active' : ''}`}
            onClick={() => setFilter('my')}
          >
            My Rooms
          </button>
        </div>
      </div>

      {filteredRooms.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📝</div>
          <h3>No rooms available</h3>
          <p>Create a new room to start collaborating</p>
        </div>
      ) : (
        <div className="room-grid">
          {filteredRooms.map((room) => (
            <div
              key={room.id}
              className="room-card"
              onClick={() => handleJoinRoom(room.id)}
            >
              <div className="room-card-header">
                <span className="room-language-badge">
                  {room.document?.language || 'javascript'}
                </span>
                <span
                  className={`room-status ${room.isActive ? 'active' : 'closed'}`}
                >
                  {room.isActive ? 'Live' : 'Closed'}
                </span>
              </div>
              <div className="room-card-body">
                <h3 className="room-name">{room.name}</h3>
                <p className="room-description">
                  {room.description || 'No description'}
                </p>
              </div>
              <div className="room-card-footer">
                <span className="room-author">
                  by {room.createdBy?.username || 'Unknown'}
                </span>
                <span className="room-date">
                  {new Date(room.createdAt).toLocaleDateString()}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default RoomList;
