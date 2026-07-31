const WS_BASE_URL = process.env.REACT_APP_WS_URL || 'ws://localhost:8080/ws';

class WebSocketService {
  constructor() {
    this.editorWs = null;
    this.presenceWs = null;
    this.listeners = new Map();
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 2000;
    this.isConnected = false;
  }

  connect(roomId, userId, username) {
    this.roomId = roomId;
    this.userId = userId;
    this.username = username;

    this.connectEditor();
    this.connectPresence();
  }

  connectEditor() {
    const wsUrl = `${WS_BASE_URL}/editor`;
    this.editorWs = new WebSocket(wsUrl);

    this.editorWs.onopen = () => {
      console.log('Editor WebSocket connected');
      this.reconnectAttempts = 0;
      this.isConnected = true;

      // Send join room message
      this.sendEditorMessage('JOIN_ROOM', {
        roomId: this.roomId,
        userId: this.userId,
        username: this.username,
      });
    };

    this.editorWs.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data);
        this.handleMessage(message);
      } catch (error) {
        console.error('Error parsing WebSocket message:', error);
      }
    };

    this.editorWs.onclose = (event) => {
      console.log('Editor WebSocket disconnected:', event.code);
      this.isConnected = false;
      this.attemptReconnect();
    };

    this.editorWs.onerror = (error) => {
      console.error('Editor WebSocket error:', error);
    };
  }

  connectPresence() {
    const wsUrl = `${WS_BASE_URL}/presence`;
    this.presenceWs = new WebSocket(wsUrl);

    this.presenceWs.onopen = () => {
      console.log('Presence WebSocket connected');
      // Start heartbeat
      this.startHeartbeat();
    };

    this.presenceWs.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data);
        this.handleMessage(message);
      } catch (error) {
        console.error('Error parsing presence message:', error);
      }
    };

    this.presenceWs.onclose = () => {
      console.log('Presence WebSocket disconnected');
    };
  }

  startHeartbeat() {
    this.heartbeatInterval = setInterval(() => {
      if (this.presenceWs && this.presenceWs.readyState === WebSocket.OPEN) {
        this.presenceWs.send(JSON.stringify({ type: 'HEARTBEAT', data: {} }));
      }
    }, 30000); // Send heartbeat every 30 seconds
  }

  sendEditorMessage(type, data) {
    if (this.editorWs && this.editorWs.readyState === WebSocket.OPEN) {
      this.editorWs.send(JSON.stringify({ type, data }));
    } else {
      console.warn('Editor WebSocket not connected. Message not sent:', type);
    }
  }

  sendPresenceMessage(type, data) {
    if (this.presenceWs && this.presenceWs.readyState === WebSocket.OPEN) {
      this.presenceWs.send(JSON.stringify({ type, data }));
    }
  }

  // Public methods for components

  sendEditOperation(operation) {
    this.sendEditorMessage('EDIT_OPERATION', operation);
  }

  updateCursor(cursorPosition) {
    this.sendEditorMessage('CURSOR_UPDATE', cursorPosition);
  }

  sendTypingIndicator(roomId, username) {
    this.sendPresenceMessage('TYPING', { roomId, username });
  }

  // Event listener system

  on(eventType, callback) {
    if (!this.listeners.has(eventType)) {
      this.listeners.set(eventType, new Set());
    }
    this.listeners.get(eventType).add(callback);
    return () => this.listeners.get(eventType)?.delete(callback);
  }

  handleMessage(message) {
    const { type, data } = message;
    const callbacks = this.listeners.get(type);
    if (callbacks) {
      callbacks.forEach((callback) => callback(data));
    }
  }

  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(
        `Attempting reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`
      );
      setTimeout(() => {
        this.connectEditor();
      }, this.reconnectDelay * this.reconnectAttempts);
    } else {
      console.error('Max reconnect attempts reached');
    }
  }

  disconnect() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
    }
    if (this.editorWs) {
      this.sendEditorMessage('LEAVE_ROOM', {});
      this.editorWs.close();
    }
    if (this.presenceWs) {
      this.presenceWs.close();
    }
    this.listeners.clear();
    this.isConnected = false;
  }
}

const webSocketService = new WebSocketService();
export default webSocketService;
