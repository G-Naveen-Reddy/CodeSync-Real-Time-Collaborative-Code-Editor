import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import webSocketService from '../services/websocketService';

const WebSocketContext = createContext(null);

export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket must be used within a WebSocketProvider');
  }
  return context;
};

export const WebSocketProvider = ({ children, roomId, userId, username }) => {
  const [connected, setConnected] = useState(false);
  const [remoteCursors, setRemoteCursors] = useState({});
  const [remoteUsers, setRemoteUsers] = useState([]);
  const [documentContent, setDocumentContent] = useState('');

  useEffect(() => {
    if (roomId && userId && username) {
      webSocketService.connect(roomId, userId, username);

      // Listen for document updates
      const unsubDocument = webSocketService.on('DOCUMENT_UPDATE', (data) => {
        setDocumentContent(data.content);
      });

      // Listen for cursor updates from other users
      const unsubCursor = webSocketService.on('CURSOR_UPDATE', (data) => {
        setRemoteCursors((prev) => ({
          ...prev,
          [data.userId]: data,
        }));
      });

      // Listen for user join/leave
      const unsubUserJoin = webSocketService.on('USER_JOINED', (data) => {
        setRemoteUsers((prev) => [...prev, data]);
      });

      const unsubUserLeave = webSocketService.on('USER_LEFT', (data) => {
        setRemoteUsers((prev) => prev.filter((u) => u.userId !== data.userId));
        setRemoteCursors((prev) => {
          const newCursors = { ...prev };
          delete newCursors[data.userId];
          return newCursors;
        });
      });

      // Listen for initial room state
      const unsubRoomState = webSocketService.on('ROOM_STATE', (data) => {
        if (data.users) setRemoteUsers(data.users);
        if (data.content) setDocumentContent(data.content);
      });

      setConnected(true);

      return () => {
        unsubDocument();
        unsubCursor();
        unsubUserJoin();
        unsubUserLeave();
        unsubRoomState();
        webSocketService.disconnect();
        setConnected(false);
      };
    }
  }, [roomId, userId, username]);

  const sendEditOperation = useCallback((operation) => {
    webSocketService.sendEditOperation(operation);
  }, []);

  const updateCursor = useCallback((cursorPosition) => {
    webSocketService.updateCursor(cursorPosition);
  }, []);

  const value = {
    connected,
    remoteCursors,
    remoteUsers,
    documentContent,
    setDocumentContent,
    sendEditOperation,
    updateCursor,
  };

  return (
    <WebSocketContext.Provider value={value}>
      {children}
    </WebSocketContext.Provider>
  );
};

export default WebSocketContext;
