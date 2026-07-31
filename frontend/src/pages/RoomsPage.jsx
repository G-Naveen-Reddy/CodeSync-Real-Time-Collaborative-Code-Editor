import React from 'react';
import { Link, Outlet } from 'react-router-dom';

const RoomsPage = () => {
  return (
    <div className="page">
      <div className="rooms-page-container">
        <div className="rooms-page-header">
          <h1>Collaboration Rooms</h1>
          <p className="subtitle">Join an existing room or create a new one</p>
          <div className="rooms-actions">
            <Link to="/rooms" className="btn btn-secondary">Browse Rooms</Link>
            <Link to="/rooms/create" className="btn btn-primary">+ Create Room</Link>
          </div>
        </div> {/* ✅ closes rooms-page-header */}

        <Outlet />
      </div> {/* ✅ closes rooms-page-container */}
    </div> 
  );
};

export default RoomsPage;
