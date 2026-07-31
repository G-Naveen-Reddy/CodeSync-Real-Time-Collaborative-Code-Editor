# CodeSync - Real-Time Collaborative Code Editor

## 🎯 Project Overview
A Google-Docs-for-Code editor that allows multiple users to edit code in real-time with syntax highlighting, cursor presence, and version history. Built with Java Spring Boot backend and modern frontend technologies.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (React + Monaco)                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────────┐  │
│  │ Code      │  │ Cursor   │  │ User     │  │ Version    │  │
│  │ Editor    │  │ Presence │  │ Auth     │  │ History    │  │
│  └──────────┘  └──────────┘  └──────────┘  └────────────┘  │
└───────────────────────┬─────────────────────────────────────┘
                        │ WebSocket (STOMP) + REST API
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

## 🛠️ Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Backend** | Java 17, Spring Boot 3 | REST APIs & WebSocket server |
| **Real-time** | Spring WebSocket + STOMP | Bidirectional communication |
| **Auth** | Spring Security + JWT | Authentication & authorization |
| **ORM** | Spring Data JPA / Hibernate | Database operations |
| **DB** | MySQL | Users, projects, documents |
| **Cache** | Redis (Lettuce) | Session management, room state |
| **Frontend** | React 18 + Monaco Editor | Code editor UI |
| **Conflict Resolution** | Operational Transformation (OT) | Handle concurrent edits |
| **Build** | Maven | Dependency management |
| **Container** | Docker | Containerization |
| **CI/CD** | GitHub Actions | Automated builds & tests |
| **Cloud** | AWS EC2 or ECS | Deployment |

## 📁 Project Structure

```
CodeSync-Editor/
├── backend/                          # Java Spring Boot
│   ├── src/main/java/com/codesync/
│   │   ├── CodeSyncApplication.java
│   │   ├── config/
│   │   │   ├── WebSocketConfig.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── RedisConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── DocumentController.java
│   │   │   └── RoomController.java
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── DocumentDTO.java
│   │   │   └── CursorPosition.java
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   ├── Document.java
│   │   │   ├── Room.java
│   │   │   └── Operation.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── DocumentRepository.java
│   │   │   └── RoomRepository.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── DocumentService.java
│   │   │   ├── RoomService.java
│   │   │   ├── OTService.java         # Operational Transformation
│   │   │   └── WebSocketService.java
│   │   ├── websocket/
│   │   │   ├── EditorWebSocketHandler.java
│   │   │   └── PresenceWebSocketHandler.java
│   │   └── util/
│   │       ├── JwtUtil.java
│   │       └── OTAlgorithm.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                         # React + Monaco Editor
│   ├── public/
│   │   └── index.html
│   ├── src/
│   │   ├── components/
│   │   │   ├── Editor/
│   │   │   │   ├── CodeEditor.jsx
│   │   │   │   ├── EditorToolbar.jsx
│   │   │   │   └── CursorOverlay.jsx
│   │   │   ├── Auth/
│   │   │   │   ├── LoginForm.jsx
│   │   │   │   └── RegisterForm.jsx
│   │   │   ├── Room/
│   │   │   │   ├── RoomList.jsx
│   │   │   │   ├── CreateRoom.jsx
│   │   │   │   └── UserList.jsx
│   │   │   └── Common/
│   │   │       └── Navbar.jsx
│   │   ├── services/
│   │   │   ├── api.js
│   │   │   ├── authService.js
│   │   │   └── websocketService.js
│   │   ├── context/
│   │   │   ├── AuthContext.jsx
│   │   │   └── WebSocketContext.jsx
│   │   ├── App.jsx
│   │   ├── App.css
│   │   └── index.js
│   ├── package.json
│   └── Dockerfile
│
├── docker-compose.yml                # Multi-container setup
├── .github/workflows/
│   └── ci-cd.yml
├── terraform/                        # IaC (bonus)
│   ├── main.tf
│   └── variables.tf
└── README.md
```

## ✅ Implementation Plan (Step-by-Step)

### Phase 1: Backend Foundation (Java Spring Boot)
- [ ] Initialize Spring Boot project with Maven
- [ ] Setup MySQL database schema (Users, Documents, Rooms)
- [ ] Implement User Authentication (JWT-based register/login)
- [ ] Create REST APIs for document CRUD operations
- [ ] Setup Redis for caching

### Phase 2: Real-Time Collaboration
- [ ] Configure Spring WebSocket with STOMP protocol
- [ ] Implement Operational Transformation (OT) algorithm
- [ ] Handle real-time document sync
- [ ] Implement cursor presence broadcasting
- [ ] Handle user join/leave events

### Phase 3: Frontend (React + Monaco)
- [ ] Initialize React project
- [ ] Build authentication pages (Login/Register)
- [ ] Integrate Monaco Editor with syntax highlighting
- [ ] Implement WebSocket client for real-time sync
- [ ] Build collaborative features (cursor overlays, user list)

### Phase 4: DevOps & Deployment
- [ ] Dockerize backend and frontend
- [ ] Setup docker-compose for local development
- [ ] Configure GitHub Actions CI/CD pipeline
- [ ] Deploy to AWS EC2/ECS

### Phase 5: Polish & Documentation
- [ ] Write comprehensive README
- [ ] Add error handling and edge cases
- [ ] Performance optimization
- [ ] Write unit and integration tests

## 🎯 Why This Project is Impressive for Freshers

1. **Full-Stack Coverage:** Shows proficiency in Java, React, databases, and real-time systems
2. **Real-Time Systems:** Implements WebSocket and OT algorithm - advanced CS concept
3. **System Design:** Distributed architecture with proper separation of concerns
4. **Cloud & DevOps:** Docker, GitHub Actions, AWS deployment
5. **Problem Solving:** Handles concurrent editing conflicts - demonstrates algorithmic thinking
6. **Modern Stack:** Uses industry-standard tools (Spring Boot, Redis, React, Docker)
7. **Portfolio Ready:** A working collaborative editor is highly shareable and demo-worthy

