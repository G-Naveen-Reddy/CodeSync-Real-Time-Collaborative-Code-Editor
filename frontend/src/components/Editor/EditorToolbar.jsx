import React from 'react';

const LANGUAGES = [
  { value: 'javascript', label: 'JavaScript' },
  { value: 'python', label: 'Python' },
  { value: 'java', label: 'Java' },
  { value: 'cpp', label: 'C++' },
  { value: 'c', label: 'C' },
  { value: 'html', label: 'HTML' },
  { value: 'css', label: 'CSS' },
  { value: 'typescript', label: 'TypeScript' },
  { value: 'go', label: 'Go' },
  { value: 'rust', label: 'Rust' },
];

const EditorToolbar = ({
  title,
  language,
  onLanguageChange,
  onSave,
  onRun,
  connected,
  userCount,
  onCopyRoomLink,
  onLeaveRoom,
}) => {
  return (
    <div className="editor-toolbar">
      <div className="toolbar-left">
        <div className="toolbar-title">
          <span className="file-icon"></span>
          <span className="file-name">{title || 'untitled'}</span>
        </div>

        <select
          className="language-select"
          value={language}
          onChange={(e) => onLanguageChange(e.target.value)}
        >
          {LANGUAGES.map((lang) => (
            <option key={lang.value} value={lang.value}>
              {lang.label}
            </option>
          ))}
        </select>

        <div className={`connection-status ${connected ? 'connected' : 'disconnected'}`}>
          <span className="status-dot"></span>
          <span className="status-text">{connected ? 'Connected' : 'Disconnected'}</span>
        </div>
      </div> {/* ✅ closes toolbar-left */}

      <div className="toolbar-right">
        <div className="user-badge" title="Connected users">
          <span className="user-icon">👥</span>
          <span className="user-count">{userCount || 1}</span>
        </div>

        <button className="btn btn-icon" onClick={onCopyRoomLink} title="Copy room link">
          🔗
        </button>

        <button className="btn btn-secondary" onClick={onSave} title="Save (Ctrl+S)">
          💾 Save
        </button>

        <button className="btn btn-primary" onClick={onRun} title="Run code">
          ▶ Run
        </button>

        <button className="btn btn-danger" onClick={onLeaveRoom} title="Leave room">
          ✕ Leave
        </button>
      </div>
    </div>
  );
};

export default EditorToolbar;
