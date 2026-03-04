# AI Java Review Assistant

AI Java Review Assistant is a full-stack application that analyzes Java code, detects issues, and generates optimized versions using locally running LLM models through Ollama.

The project combines static analysis techniques with AI reasoning to help developers quickly identify bugs, inefficiencies, and code smells.

---

# Architecture Overview

The system uses a three-layer architecture:

User → React Frontend → Spring Boot Backend → Ollama (Local LLM Models)

**Frontend (React + Vite)**

* Provides the user interface
* Allows users to paste Java code and optional context
* Displays analysis results and optimized code

**Backend (Spring Boot + WebFlux)**

* Receives requests from the frontend
* Performs syntax validation using JavaParser
* Builds prompts and communicates with LLM models through Ollama
* Categorizes issues (BUG / INEFFICIENCY / SMELL / CLEAN)
* Returns structured responses to the frontend

**AI Models (Ollama)**

* deepseek-r1:7b → Code reasoning and analysis
* qwen2.5-coder:14b → Code optimization and rewriting

These models run locally on the machine via Ollama.

---

# Repository Structure

```
project-root
│
├── ai-review-assistant
│   └── ai-review-assistant
│       └── Spring Boot backend
│
├── frontend
│   └── React + Vite frontend
│
└── README.md
```

---

# Requirements

Before running the project, install:

Java 21
Node.js (v18+)
Git
Ollama

---

# Step 1 — Install Ollama

Download and install Ollama:

https://ollama.com

Start the Ollama server:

```
ollama serve
```

Pull the required models:

```
ollama pull deepseek-r1:7b
ollama pull qwen2.5-coder:14b
```

Verify installation:

```
ollama list
```

---

# Step 2 — Run the Spring Boot Backend

Navigate to the backend project:

```
cd ai-review-assistant/ai-review-assistant
```

Run the backend using Maven:

```
./mvnw spring-boot:run
```

Or if Maven is installed globally:

```
mvn spring-boot:run
```

The backend will start at:

```
http://localhost:8080
```

---

# Step 3 — Run the React Frontend

Navigate to the frontend folder:

```
cd frontend
```

Install dependencies:

```
npm install
```

Start the development server:

```
npm run dev
```

The frontend will start at:

```
http://localhost:5173
```

---

# Step 4 — Using the Application

1. Open the frontend in your browser
2. Paste Java code into the editor
3. Optionally provide context for the code
4. Click **Analyze Code**
5. Review the detected issues and complexity analysis
6. Click **Optimize Code** to generate an improved version

---

# Analysis Categories

The analyzer classifies code into four categories:

BUG
Logical errors or runtime risks (infinite loops, boundary issues)

INEFFICIENCY
Avoidable high complexity or repeated computation

SMELL
Poor practices or style problems

CLEAN
Code appears correct and reasonably efficient

---

# AI Models Used

| Task              | Model             |
| ----------------- | ----------------- |
| Code Analysis     | deepseek-r1:7b    |
| Code Optimization | qwen2.5-coder:14b |

The models are served locally using Ollama.

---

# Example Workflow

User Input → Backend Validation → LLM Analysis → Issue Categorization → Optimized Code Generation → Frontend Display

---

# Future Improvements

Better static bug detection
Improved prompt engineering
Caching for faster responses
Containerized deployment (Docker)
Benchmark testing across models

---

# Author

Mohit Balodi

Computer Science Engineering Student

---

# License

This project is intended for educational and experimental use.
