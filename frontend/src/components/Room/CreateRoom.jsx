import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import toast from 'react-hot-toast';

const LANGUAGES = [
  { value: 'javascript', label: 'JavaScript' },
  { value: 'python', label: 'Python' },
  { value: 'java', label: 'Java' },
  { value: 'cpp', label: 'C++' },
  { value: 'c', label: 'C' },
  { value: 'html', label: 'HTML' },
  { value: 'css', label: 'CSS' },
  { value: 'typescript', label: 'TypeScript' },
];

const TEMPLATES = {
  javascript: '// Start coding in JavaScript\nconsole.log("Hello, CodeSync!");\n',
  python: '# Start coding in Python\nprint("Hello, CodeSync!")\n',
  java: 'public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello, CodeSync!");\n    }\n}\n',
  cpp: '#include <iostream>\nusing namespace std;\n\nint main() {\n    cout << "Hello, CodeSync!" << endl;\n    return 0;\n}\n',
  html: '<!DOCTYPE html>\n<html>\n<head>\n    <title>CodeSync</title>\n</head>\n<body>\n    <h1>Hello, CodeSync!</h1>\n</body>\n</html>\n',
  css: '/* Start coding in CSS */\nbody {\n  font-family: Arial, sans-serif;\n  background-color: #f5f5f5;\n}\n',
  typescript: '// Start coding in TypeScript\nconst message: string = "Hello, CodeSync!";\nconsole.log(message);\n',
};

const CreateRoom = () => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    language: 'javascript',
  });
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      toast.error('Room name is required');
      return;
    }

    setIsLoading(true);

    try {
      const response = await api.post('/rooms', {
        name: formData.name,
        description: formData.description,
        language: formData.language,
      });

      if (!response.data?.id) {
        toast.error('Unexpected response from server');
        return;
      }

      toast.success('Room created successfully!');
      navigate(`/editor/${response.data.id}`);
    } catch (error) {
      const message = error.response?.data?.error || 'Failed to create room';
      toast.error(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="create-room-container">
      <div className="create-room-card">
        <h2>Create a New Room</h2>
        <p className="subtitle">Start a collaborative coding session</p>

        <form onSubmit={handleSubmit} className="create-room-form">
          <div className="form-group">
            <label htmlFor="name">Room Name *</label>
            <input
              id="name"
              name="name"
              type="text"
              placeholder="e.g., Project-Alpha, Bug-Fix-Session"
              value={formData.name}
              onChange={handleChange}
              required
              disabled={isLoading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea
              id="description"
              name="description"
              placeholder="What are you working on?"
              value={formData.description}
              onChange={handleChange}
              rows={3}
              disabled={isLoading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="language">Language</label>
            <select
              id="language"
              name="language"
              value={formData.language}
              onChange={handleChange}
              disabled={isLoading}
            >
              {LANGUAGES.map((lang) => (
                <option key={lang.value} value={lang.value}>
                  {lang.label}
                </option>
              ))}
            </select>
          </div>

          <div className="form-actions">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => navigate('/dashboard')}
              disabled={isLoading}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={isLoading}>
              {isLoading ? 'Creating...' : 'Create Room'}
            </button>
          </div>
        </form>
      </div>

      <div className="create-room-preview">
        <h3>Template Preview</h3>
        <pre className="code-preview">
          <code>{TEMPLATES[formData.language]}</code>
        </pre>
      </div>
    </div>
  );
};

export default CreateRoom;
