import React from 'react';

const CURSOR_COLORS = [
  '#ff6b6b', '#ffd93d', '#6bcb77', '#4d96ff',
  '#ff6b9d', '#c44dff', '#ff9f43', '#00d2d3',
  '#f368e0', '#54a0ff', '#5f27cd', '#01a3a4',
];

const CursorOverlay = ({ cursors, currentUserId }) => {
  if (!cursors || Object.keys(cursors).length === 0) return null;

  return (
    <div className="cursor-overlay">
      {Object.entries(cursors).map(([userId, cursor], index) => {
        if (userId === String(currentUserId)) return null;

        const color = CURSOR_COLORS[index % CURSOR_COLORS.length];

        return (
          <div
            key={userId}
            className="remote-cursor-label"
            style={{
              top: `${(cursor.line - 1) * 18}px`,
              left: `${cursor.column * 8.5}px`,
              backgroundColor: color,
            }}
          >
            <span className="cursor-label-text">{cursor.username}</span>
          </div>
        );
      })}
    </div>
  );
};

export default CursorOverlay;
