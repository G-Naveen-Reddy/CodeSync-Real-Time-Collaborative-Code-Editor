import React from 'react';

const USER_COLORS = [
  '#ff6b6b', '#ffd93d', '#6bcb77', '#4d96ff',
  '#ff6b9d', '#c44dff', '#ff9f43', '#00d2d3',
];

const UserList = ({ users = [], currentUserId }) => {
  return (
    <div className="user-list-container">
      <div className="user-list-header">
        <h3>Connected Users</h3>
        <span className="user-count-badge">{users.length}</span>
      </div>

      <div className="user-list">
        {users.length === 0 ? (
          <div className="no-users">No other users connected</div>
        ) : (
          users.map((user, index) => {
            const isCurrentUser = user.userId === currentUserId;
            const color = USER_COLORS[index % USER_COLORS.length];

            return (
              <div
                key={user.userId || index}
                className={`user-list-item ${isCurrentUser ? 'current-user' : ''}`}
              >
                <div
                  className="user-avatar-small"
                  style={{ backgroundColor: color }}
                >
                  {user.username?.charAt(0).toUpperCase() || '?'}
                </div>
                <div className="user-info">
                  <span className="user-list-name">
                    {user.username || 'Anonymous'}
                    {isCurrentUser && <span className="you-badge">(you)</span>}
                  </span>
                  <span className="user-status">
                    {user.isTyping ? (
                      <span className="typing-indicator">Typing...</span>
                    ) : (
                      <span className="online-indicator">Online</span>
                    )}
                  </span>
                </div>
                <div
                  className="user-color-indicator"
                  style={{ backgroundColor: color }}
                />
              </div>
            );
          })
        )}
      </div>
    </div> 
  );
};

export default UserList;
