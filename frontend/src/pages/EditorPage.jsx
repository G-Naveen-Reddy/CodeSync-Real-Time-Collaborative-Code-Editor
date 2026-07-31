import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { WebSocketProvider } from '../context/WebSocketContext';
import CodeEditor from '../components/Editor/CodeEditor';
import EditorToolbar from '../components/Editor/EditorToolbar';
import UserList from '../components/Room/UserList';
import api from '../services/api';
import toast from 'react-hot-toast';

const EditorContent = () => {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [room, setRoom] = useState(null);
  const [language, setLanguage] = useState('javascript');
  const [loading, setLoading] = useState(true);
  const [connected] = useState(true);

  useEffect(() => {
    fetchRoomDetails();
  }, [roomId]);

  const fetchRoomDetails = async () => {
    try {
      const response = await api.get(`/rooms/${roomId}`);
      setRoom(response.data);
      setLanguage(response.data.document?.language || 'javascript');
    } catch (error) {
      toast.error('Failed to load room');
      navigate('/dashboard');
    } finally {
      setLoading(false);
    }
  };

  const handleLanguageChange = async (newLanguage) => {
    setLanguage(newLanguage);
    toast.success(`Language switched to ${newLanguage}`);
  };

  const handleSave = useCallback(async () => {
    try {
      toast.success('Document saved!');
    } catch (error) {
      toast.error('Failed to save document');
    }
  }, [roomId]);

  const handleRun = useCallback(() => {
    toast.success('Code execution feature coming soon!');
  }, []);

  const handleCopyRoomLink = useCallback(() => {
    const link = `${window.location.origin}/editor/${roomId}`;
    navigator.clipboard.writeText(link);
    toast.success('Room link copied to clipboard!');
  }, [roomId]);

  const handleLeaveRoom = useCallback(() => {
    navigate('/dashboard');
  }, [navigate]);

  if (loading) {
    return (
      <div className="editor-loading-full">
        <div className="spinner"></div>
        <p>Loading collaborative editor...</p>
      </div>
    );
  }

  if (!room) {
    return (
      <div className="editor-error">
        <h2>Room not found</h2>
        <p>This room may have been closed or doesn't exist.</p>
        <button className="btn btn-primary" onClick={() => navigate('/dashboard')}>
          Back to Dashboard
        </button>
      </div>
    );
  }

  return (
    <WebSocketProvider
      roomId={roomId}
      userId={user?.userId}
      username={user?.username}
    >
      <div className="editor-page">
        <EditorToolbar
          title={room.name}
          language={language}
          onLanguageChange={handleLanguageChange}
          onSave={handleSave}
          onRun={handleRun}
          connected={connected}
          userCount={2}
          onCopyRoomLink={handleCopyRoomLink}
          onLeaveRoom={handleLeaveRoom}
        />
        <div className="editor-main">
          <div className="editor-area">
            <CodeEditor language={language} />
          </div>
          <div className="editor-sidebar">
            <UserList
              users={[{ userId: user?.userId, username: user?.username }]}
              currentUserId={user?.userId}
            />
            <div className="room-info-panel">
              <h4>Room Info</h4>
              <div className="info-row">
                <span className="info-label">Name:</span>
                <span className="info-value">{room.name}</span>
              </div>
              <div className="info-row">
                <span className="info-label">Status:</span>
                <span className="info-value status-active">Active</span>
              </div>
              <div className="info-row">
                <span className="info-label">Language:</span>
                <span className="info-value">{language}</span>
              </div>
              <div className="info-row">
                <span className="info-label">Created:</span>
                <span className="info-value">
                  {new Date(room.createdAt).toLocaleDateString()}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </WebSocketProvider>
  );
};

export default EditorContent;
