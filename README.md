# CodeSync - Real-Time Collaborative Code Editor

<div align="center">
  <img src="https://img.shields.io/badge/Java-17-%23ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.2-%236DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot 3.2"/>
  <img src="https://img.shields.io/badge/React-18-%2361DAFB?style=for-the-badge&logo=react&logoColor=white" alt="React 18"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-%234479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL 8.0"/>
  <img src="https://img.shields.io/badge/Redis-7-%23DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis 7"/>
  <img src="https://img.shields.io/badge/Docker-%232496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/AWS-%23FF9900?style=for-the-badge&logo=amazon-aws&logoColor=white" alt="AWS"/>
</div>

---

## 🚀 Overview

**CodeSync** is a real-time collaborative code editor that allows multiple developers to write, edit, and review code together in real-time. Similar to Google Docs but purpose-built for code, it features syntax highlighting, cursor presence, and conflict-free editing through Operational Transformation (OT).

### ✨ Key Features

| Feature | Description |
|---------|-------------|
| 🔄 **Real-Time Collaboration** | Multiple users edit the same document simultaneously |
| 🎨 **Syntax Highlighting** | Powered by Monaco Editor (same engine as VS Code) |
| 👥 **Live Cursor Presence** | See where others are typing with colored cursors |
| ⚡ **Operational Transformation** | Conflict-free concurrent edits with OT algorithm |
| 🔐 **JWT Authentication** | Secure user registration and login |
| 📁 **Room Management** | Create, join, and manage collaboration sessions |
| 🎯 **Multi-Language Support** | Java, Python, JavaScript, C++, HTML, CSS, and more |
| 🐳 **Docker Support** | One-command deployment with docker-compose |
| 🔄 **CI/CD Pipeline** | Automated build, test, and deployment |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (React + Monaco)                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────────┐  │
│  │ Code      │  │ Cursor   │  │ User     │  │ Version    │  │
│  │ Editor    │  │ Presence │  │ Auth     │  │ History    │  │
│  └──────────┘  └──────────┘  └──────────┘  └────────────┘  │
└───────────────────────┬─────────────────────────────────────┘
                        │ WebSocket + REST API
                        ▼
┌─────────────────────────────────────────────────────────────┐
│              Backend (Java Spring Boot)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────────┐  │
│  │ WebSocket│  │ Auth     │  │ Room     │  │ OT/CRDT    │  │
│  │ Handler  │  │ Service  │  │ Manager  │  │ Engine     │  │
│  └──────────┘  └──────────┘  └──────────┘  └────────────┘  │
└───────┬──────────────┬──────────────────────┬───────────────┘
        │              │                      │
        ▼              ▼                      ▼
┌──────────────┐ ┌──────────┐ ┌──────────────────────┐
│   MySQL      │ │  Redis   │ │   AWS S3 (optional)   │
│  (Users,     │ │ (Sessions,│ │   (File Storage)     │
│   Projects)  │ │  Room    │ │                      │
│              │ │  State)  │ │                      │
└──────────────┘ └──────────┘ └──────────────────────┘
```

---

## 🛠️ Tech Stack

### Backend
| Technology | Purpose |
|-----------|---------|
| **Java 17** | Core programming language |
| **Spring Boot 3.2** | Application framework |
| **Spring WebSocket** | Real-time bidirectional communication |
| **Spring Security + JWT** | Authentication & authorization |
| **Spring Data JPA (Hibernate)** | ORM for database operations |
| **MySQL 8.0** | Primary database for users, documents, rooms |
| **Redis 7** | Caching, session management, room state |
| **Maven** | Build and dependency management |

### Frontend
| Technology | Purpose |
|-----------|---------|
| **React 18** | UI framework |
| **Monaco Editor** | Code editor (VS Code engine) |
| **STOMP.js + SockJS** | WebSocket client |
| **React Router** | Client-side routing |
| **Axios** | HTTP client for REST API |

### DevOps
| Technology | Purpose |
|-----------|---------|
| **Docker** | Containerization |
| **Docker Compose** | Multi-container orchestration |
| **GitHub Actions** | CI/CD pipeline |
| **Nginx** | Production web server for frontend |

---

## 📁 Project Structure

```
CodeSync-Editor/
├── backend/                          # Java Spring Boot
│   ├── src/main/java/com/codesync/
│   │   ├── CodeSyncApplication.java
│   │   ├── config/                   # WebSocket, Security, Redis configs
│   │   ├── controller/               # REST API controllers
│   │   ├── dto/                      # Data Transfer Objects
│   │   ├── model/                    # JPA entities
│   │   ├── repository/               # JPA repositories
│   │   ├── service/                  # Business logic
│   │   ├── websocket/                # WebSocket handlers
│   │   └── util/                     # Utilities (JWT, OT)
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                         # React + Monaco Editor
│   ├── public/
│   ├── src/
│   │   ├── components/               # UI components
│   │   ├── context/                  # React contexts
│   │   ├── services/                 # API and WebSocket services
│   │   └── pages/                    # Page components
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── docker-compose.yml                # Multi-container setup
├── .github/workflows/
│   └── ci-cd.yml
├── PROJECT_PLAN.md
├── TODO.md
└── README.md
```

---

## 🚦 Getting Started

### Prerequisites
- **Java 17+** (JDK)
- **Node.js 18+**
- **Docker & Docker Compose** (recommended)
- **MySQL 8.0** (if running without Docker)
- **Redis 7** (if running without Docker)

### Option 1: Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/yourusername/codesync-editor.git
cd codesync-editor

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Services:
# - Frontend: http://localhost:3000
# - Backend:  http://localhost:8080
# - MySQL:    localhost:3306
# - Redis:    localhost:6379
```

### Option 2: Local Development

#### Backend Setup
```bash
# Navigate to backend
cd backend

# Build with Maven
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# The backend starts at http://localhost:8080
```

#### Frontend Setup
```bash
# Navigate to frontend
cd frontend

# Install dependencies
npm install

# Start development server
npm start

# The frontend starts at http://localhost:3000
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/codesync` | MySQL connection URL |
| `SPRING_DATASOURCE_USERNAME` | `root` | MySQL username |
| `SPRING_DATASOURCE_PASSWORD` | `root` | MySQL password |
| `SPRING_REDIS_HOST` | `localhost` | Redis host |
| `SPRING_REDIS_PORT` | `6379` | Redis port |
| `JWT_SECRET` | *(configured)* | JWT signing secret |
| `REACT_APP_API_URL` | `http://localhost:8080/api` | Backend API URL |
| `REACT_APP_WS_URL` | `ws://localhost:8080/ws` | WebSocket URL |

---

## 🎯 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login user |
| GET | `/api/auth/health` | Health check |

### Documents
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/documents` | Create a document |
| GET | `/api/documents/{id}` | Get document by ID |
| GET | `/api/documents/my` | Get user's documents |
| PUT | `/api/documents/{id}` | Update document |
| DELETE | `/api/documents/{id}` | Delete document |

### Rooms
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/rooms` | Create a room |
| GET | `/api/rooms` | List active rooms |
| GET | `/api/rooms/{id}` | Get room by ID |
| GET | `/api/rooms/my` | Get user's rooms |
| POST | `/api/rooms/{id}/close` | Close a room |

### WebSocket Endpoints
| Endpoint | Purpose |
|----------|---------|
| `/ws/editor` | Real-time editing operations |
| `/ws/presence` | User presence and heartbeat |

---

## 🧠 Why This Project is Impressive

### For Java Roles:
- **Spring Boot mastery** - REST APIs, Security, WebSocket, JPA
- **Algorithm implementation** - Operational Transformation
- **Design patterns** - Service layer, Repository pattern, DTOs

### For Full-Stack Roles:
- **Complete application** - From DB to UI
- **Modern frontend** - React hooks, context, functional components
- **Real-time systems** - WebSocket communication

### For System Design Roles:
- **Distributed collaboration** - Conflict resolution
- **Database design** - Relational (MySQL) + Cache (Redis)
- **Scalable architecture** - Microservices-ready

### For DevOps Roles:
- **Containerization** - Multi-stage Docker builds
- **Orchestration** - Docker Compose
- **CI/CD** - GitHub Actions pipeline

---

## 📄 License

This project is licensed under the MIT License.

---

## 🙏 Acknowledgments

- [Monaco Editor](https://microsoft.github.io/monaco-editor/) - VS Code's editor engine
- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [Catppuccin](https://github.com/catppuccin/catppuccin) - Color theme inspiration

---

<div align="center">
  <sub>Built with ❤️ for collaborative coding</sub>
</div>
