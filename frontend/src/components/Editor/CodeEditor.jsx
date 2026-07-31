import React, { useRef, useCallback } from 'react';
import Editor from '@monaco-editor/react';
import { useWebSocket } from '../../context/WebSocketContext';
import { useAuth } from '../../context/AuthContext';

const LANGUAGE_MAP = {
  javascript: 'javascript',
  python: 'python',
  java: 'java',
  cpp: 'cpp',
  c: 'c',
  html: 'html',
  css: 'css',
  typescript: 'typescript',
  go: 'go',
  rust: 'rust',
};

const CodeEditor = ({ language = 'javascript', readOnly = false }) => {
  const editorRef = useRef(null);
  const { remoteCursors, sendEditOperation, updateCursor, documentContent, setDocumentContent } = useWebSocket();
  const { user } = useAuth();
  const lastSentContentRef = useRef('');

  const handleEditorDidMount = useCallback((editor, monaco) => {
    editorRef.current = editor;

    // Configure editor settings
    editor.updateOptions({
      minimap: { enabled: true },
      fontSize: 14,
      lineNumbers: 'on',
      roundedSelection: false,
      scrollBeyondLastLine: false,
      automaticLayout: true,
      cursorBlinking: 'smooth',
      cursorSmoothCaretAnimation: 'on',
      smoothScrolling: true,
      renderWhitespace: 'selection',
      bracketPairColorization: { enabled: true },
      fontFamily: "'Fira Code', 'Cascadia Code', 'JetBrains Mono', Consolas, monospace",
      fontLigatures: true,
    });

    // Track cursor position changes
    editor.onDidChangeCursorPosition((e) => {
      updateCursor({
        userId: user?.userId,
        username: user?.username,
        line: e.position.lineNumber,
        column: e.position.column,
      });
    });
  }, [user, updateCursor]);

  const handleChange = useCallback((value) => {
    if (value !== undefined) {
      setDocumentContent(value);

      // Send edit operation (debounced via content comparison)
      if (value !== lastSentContentRef.current) {
        lastSentContentRef.current = value;
        sendEditOperation({
          type: 'INSERT',
          content: value,
          userId: user?.userId,
          username: user?.username,
          timestamp: Date.now(),
        });
      }
    }
  }, [setDocumentContent, sendEditOperation, user]);

  // Render remote cursor decorations
  const renderRemoteCursors = useCallback(() => {
    if (!editorRef.current || !remoteCursors) return;

    const monaco = window.monaco;
    if (!monaco) return;

    const decorations = Object.values(remoteCursors).map((cursor) => {
      return {
        range: new monaco.Range(cursor.line, cursor.column, cursor.line, cursor.column + 1),
        options: {
          className: 'remote-cursor',
          beforeContentClassName: 'remote-cursor-before',
          hoverMessage: { value: `${cursor.username} is here` },
        },
      };
    });

    editorRef.current.deltaDecorations([], decorations);
  }, [remoteCursors]);

  return (
    <div className="code-editor-container">
      <Editor
        height="100%"
        language={LANGUAGE_MAP[language] || 'javascript'}
        value={documentContent || '// Start coding collaboratively...'}
        onChange={handleChange}
        onMount={handleEditorDidMount}
        theme="vs-dark"
        options={{
          readOnly: readOnly,
          wordWrap: 'on',
          tabSize: 2,
          insertSpaces: true,
        }}
        loading={
          <div className="editor-loading">
            <div className="spinner"></div>
            <p>Loading Code Editor...</p>
          </div>
        }
      />
    </div>
  );
};

export default CodeEditor;
