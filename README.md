# Docu-Leader Backend

## 📋 Overview

Docu-Leader Backend is a **Domain-Aware Document Intelligence Engine** that powers the AI capabilities of the Docu-Leader platform. It combines the functionality of a smart librarian (semantic document search) with an accountant (invoice processing and payment tracking) into a single, cohesive system.

Built with **Java 21** and **Spring Boot 3.x**, it provides RESTful APIs for document processing, semantic search, invoice extraction, and payment tracking, all powered by **Google's Gemini AI models**.

### 🎯 Core Purpose

The backend serves as the brain of the application, handling:

- **Document Intelligence**: Upload, process, and semantically search documents using RAG (Retrieval Augmented Generation)
- **Invoice Automation**: Automatically extract and track invoice data with AI
- **AI-Powered Q&A**: Answer questions grounded in your document context (no hallucinations)
- **Payment Management**: Track payments and generate smart reminders for overdue invoices
- **User Management**: Secure authentication and authorization using JWT

---

## 🏛️ Architecture

The application follows a modern, scalable architecture centered around Spring Boot with clean separation of concerns:

```
┌─────────────────┐     ┌─────────────────────────────────────┐     ┌─────────────────┐
│                 │     │            Spring Boot              │     │                 │
│    Frontend     │────▶│         Application Layer           │────▶│   PostgreSQL    │
│   (React App)   │     │                                     │     │   + pgvector    │
│                 │◀────│  Controllers → Services → Repos    │◀────│                 │
└─────────────────┘     └─────────────────────────────────────┘     └─────────────────┘
                                      │
                                      ▼
                         ┌─────────────────────────┐
                         │      Google Gemini       │
                         │   (AI & Embedding APIs)  │
                         └─────────────────────────┘
```

### Layered Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Controller Layer                         │
│  (REST APIs, Request/Response Handling, Validation)         │
├─────────────────────────────────────────────────────────────┤
│                     Service Layer                             │
│  (Business Logic, AI Integration, Transaction Management)   │
├─────────────────────────────────────────────────────────────┤
│                     Repository Layer                          │
│  (Database Access, JPA Repositories, Custom Queries)        │
├─────────────────────────────────────────────────────────────┤
│                     Entity Layer                              │
│  (JPA Entities, Domain Models, Enums)                       │
└─────────────────────────────────────────────────────────────┘
```

---

## ✨ Key Features

### 1. **Document Intelligence (RAG Pipeline)**

```
Upload → Text Extraction → Chunking → Embedding Generation → Vector Storage → Semantic Search
```

- **Multi-format Support**: PDF, Images (PNG, JPEG), Word documents, Spreadsheets
- **Text Extraction**: Uses Apache Tika for robust text extraction from various formats
- **Intelligent Chunking**: Splits documents into overlapping chunks (1000 chars with 200 overlap)
- **Vector Embeddings**: Generates embeddings using Google Gemini's embedding models
- **Similarity Search**: Finds relevant document chunks using cosine similarity with pgvector
- **Grounded Answers**: LLM generates answers based ONLY on retrieved context (no hallucinations)

### 2. **Invoice Intelligence**

```
Upload → Document Classification → AI Extraction → Structured Data → Payment Tracking
```

- **Automatic Detection**: Classifies documents as invoices using AI
- **Structured Extraction**: Extracts vendor name, invoice number, dates, amounts
- **Status Tracking**: Manages invoice lifecycle (DRAFT → UNPAID → PAID → OVERDUE)
- **Smart Reminders**: Generates polite, context-aware payment reminders
- **Overdue Detection**: Automatically identifies and flags overdue invoices

### 3. **Semantic Search & Q&A**

```json
POST /api/documents/query
{
  "query": "What are the payment terms in the latest contract?"
}

Response: {
  "answer": "According to the service agreement dated March 1, 2026...",
  "sources": ["service-agreement-2026.pdf", "contract-amendment.pdf"]
}
```

### 4. **Payment Tracking**

- **Dashboard**: Overview of all invoices with status indicators
- **Filtering**: View by status (UNPAID, PAID, OVERDUE)
- **Due Date Monitoring**: Automatic detection of upcoming and overdue payments
- **Reminder Generation**: AI-powered professional reminders

### 5. **User Management**

- **JWT Authentication**: Secure token-based authentication
- **Registration/Login**: User account management
- **Protected Routes**: All endpoints secured except auth endpoints
- **Password Encryption**: BCrypt password hashing

---

## 🛠️ Technology Stack

### Core Framework
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming language |
| Spring Boot | 3.2.x | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.x | Database access |
| Spring Web | 6.x | REST API development |

### AI & Machine Learning
| Technology | Purpose |
|------------|---------|
| Google Gemini Pro | Chat/completion model for Q&A and extraction |
| Google Gemini Embedding | Text embedding generation |
| Spring AI | Abstraction layer for AI interactions |
| RAG (Retrieval Augmented Generation) | Grounded answer generation |

### Database
| Technology | Purpose |
|------------|---------|
| PostgreSQL | Primary database |
| pgvector | Vector similarity search extension |
| Hibernate | ORM framework |
| Flyway | Database migration management |

### Document Processing
| Technology | Purpose |
|------------|---------|
| Apache Tika | Text extraction from various formats |
| PDFBox | PDF processing |
| Tess4J | OCR for scanned documents |

### Security & Utilities
| Technology | Purpose |
|------------|---------|
| JWT | Token-based authentication |
| Lombok | Boilerplate code reduction |
| BCrypt | Password encoding |
| Jakarta Validation | Request validation |

---

## 🔄 How It Works

### 1. **Authentication Flow**

```
┌─────────┐     POST /api/auth/login     ┌──────────┐
│ Client  │─────────────────────────────▶│ Backend  │
│         │◀─────────────────────────────│          │
└─────────┘     JWT Token + User Info    └──────────┘

Subsequent Requests:
Authorization: Bearer <jwt-token>
```

1. User submits credentials to `/api/auth/login`
2. Backend validates credentials and generates JWT
3. Token returned to client
4. Client includes token in `Authorization` header for all protected requests
5. JwtAuthenticationFilter validates token for each request

### 2. **Document Processing Pipeline**

```
┌─────────┐    1. Upload     ┌──────────────┐
│ Client  │─────────────────▶│  Controller  │
└─────────┘                  └──────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────┐
│           DocumentProcessingService               │
├──────────────────────────────────────────────────┤
│  ┌──────────────┐    ┌──────────────────────┐   │
│  │ 1. Text      │    │ 2. Split into        │   │
│  │ Extraction   │───▶│ Chunks (1000 chars)  │   │
│  │ (Tika)       │    │                      │   │
│  └──────────────┘    └──────────────────────┘   │
│                              │                   │
│                              ▼                   │
│  ┌──────────────┐    ┌──────────────────────┐   │
│  │ 4. Save      │◀───│ 3. Generate          │   │
│  │ Chunks +     │    │ Embeddings (Gemini)  │   │
│  │ Embeddings   │    │                      │   │
│  └──────────────┘    └──────────────────────┘   │
│         │                                        │
│         ▼                                        │
│  ┌──────────────┐                                │
│  │ 5. Classify  │                                │
│  │ Document Type│                                │
│  └──────────────┘                                │
│         │                                        │
│         ▼                                        │
│  ┌──────────────┐                                │
│  │ 6. If Invoice│                                │
│  │ → Extract    │                                │
│  │ Invoice Data │                                │
│  └──────────────┘                                │
└──────────────────────────────────────────────────┘
```

**Step-by-Step:**

1. **Upload**: Client uploads file via `/api/documents` (multipart/form-data)
2. **Initial Storage**: File saved to local storage, document record created with "UPLOADED" status
3. **Async Processing**: `@Async` method triggered immediately
4. **Text Extraction**: Apache Tika extracts raw text from the document
5. **Chunking**: Text split into overlapping chunks (configurable size: 1000 chars, overlap: 200)
6. **Embedding Generation**: Each chunk sent to Gemini Embedding API → vector representation
7. **Vector Storage**: Chunks + embeddings saved to `document_chunks` table
8. **Document Classification**: AI determines document type (LEGAL, FINANCIAL, INVOICE, TECHNICAL, OTHER)
9. **Invoice Processing**: If classified as INVOICE, trigger specialized extraction
10. **Status Update**: Document marked as "COMPLETED" or "FAILED"

### 3. **Semantic Search (RAG Pipeline)**

```
┌─────────┐     POST /api/documents/query    ┌──────────────┐
│ Client  │─────────────────────────────────▶│  Controller  │
└─────────┘                                   └──────────────┘
                                                      │
                                                      ▼
┌─────────────────────────────────────────────────────────────┐
│                      QueryService                           │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐                                        │
│  │ 1. Generate     │                                        │
│  │ Query Embedding │                                        │
│  │ (Gemini)        │                                        │
│  └────────┬────────┘                                        │
│           ▼                                                  │
│  ┌─────────────────┐    ┌─────────────────────────────┐    │
│  │ 2. Vector       │───▶│ SELECT * FROM document_    │    │
│  │ Similarity      │    │ chunks ORDER BY embedding  │    │
│  │ Search          │    │ <=> :query LIMIT 5         │    │
│  └────────┬────────┘    └─────────────────────────────┘    │
│           ▼                                                  │
│  ┌─────────────────┐                                        │
│  │ 3. Build        │                                        │
│  │ Context from    │                                        │
│  │ Retrieved Chunks│                                        │
│  └────────┬────────┘                                        │
│           ▼                                                  │
│  ┌─────────────────┐                                        │
│  │ 4. Send Context │                                        │
│  │ + Question to   │                                        │
│  │ Gemini Chat     │                                        │
│  └────────┬────────┘                                        │
└───────────┬─────────────────────────────────────────────────┘
            ▼
┌─────────────────────┐     ┌─────────┐
│  5. Return          │────▶│ Client  │
│  Grounded Answer    │     │         │
│  + Sources          │     └─────────┘
└─────────────────────┘
```

```

### 4. **Invoice Processing**

```
┌──────────────────────────────────────────────────┐
│          InvoiceExtractionService                 │
├──────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────┐ │
│  │ 1. Send document text to Gemini with        │ │
│  │    specialized extraction prompt            │ │
│  └───────────────────┬─────────────────────────┘ │
│                      ▼                            │
│  ┌─────────────────────────────────────────────┐ │
│  │ 2. Parse JSON response:                     │ │
│  │    {                                         │ │
│  │      "vendor_name": "Acme Corp",            │ │
│  │      "invoice_number": "INV-2026-001",      │ │
│  │      "issue_date": "2026-03-01",            │ │
│  │      "due_date": "2026-03-31",              │ │
│  │      "total_amount": 1500.00,               │ │
│  │      "tax_amount": 150.00                    │ │
│  │    }                                         │ │
│  └───────────────────┬─────────────────────────┘ │
│                      ▼                            │
│  ┌─────────────────────────────────────────────┐ │
│  │ 3. Convert to Invoice entity                 │ │
│  │    - Parse dates to epoch millis             │ │
│  │    - Parse amounts to BigDecimal              │ │
│  └───────────────────┬─────────────────────────┘ │
│                      ▼                            │
│  ┌─────────────────────────────────────────────┐ │
│  │ 4. Save to invoices table                    │ │
│  │    status = UNPAID                            │ │
│  └─────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────┘
```


```

---

## 📊 Database Schema

The application uses PostgreSQL with the **pgvector** extension for vector similarity search.

### Core Tables

| Table | Purpose |
|-------|---------|
| `users` | User accounts and authentication |
| `documents` | Document metadata and status |
| `document_chunks` | Text chunks with vector embeddings |
| `invoices` | Extracted invoice data |

### Key Design Decisions

1. **Epoch Millis for Dates**: All timestamps stored as `BIGINT` (epoch milliseconds) for:
   - Timezone independence
   - Easy comparison and sorting
   - Efficient indexing

2. **Vector Embeddings**: Stored in `vector(768)` column using pgvector
   - Supports cosine similarity search
   - HNSW index for efficient nearest neighbor search

3. **JSONB for Flexibility**: Metadata stored as JSONB for extensibility

**Complete Schema**: See `src/main/resources/DDL.txt` for the complete database schema with all tables, indexes, and constraints.

---

## 🔌 API Endpoints

### Authentication

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/auth/register` | Register new user | `{email, password, name}` | `{token, user}` |
| POST | `/api/auth/login` | User login | `{email, password}` | `{token, user}` |
| POST | `/api/auth/logout` | User logout | - | `{message}` |

### Documents

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| POST | `/api/documents` | Upload document | `multipart/form-data` | `{documentId, fileName, status}` |
| GET | `/api/documents` | List all documents | - | `[{document}]` |
| GET | `/api/documents/{id}` | Get document details | - | `{document}` |
| GET | `/api/documents/{id}/status` | Check processing status | - | `{status}` |
| POST | `/api/documents/query` | Semantic search | `{query}` | `{answer, sources}` |

### Invoices

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/invoices` | List all invoices | `[{invoice}]` |
| GET | `/api/invoices/{id}` | Get invoice details | `{invoice}` |
| PUT | `/api/invoices/{id}/pay` | Mark as paid | `{message}` |
| POST | `/api/invoices/{id}/remind` | Generate reminder | `{message}` |
| GET | `/api/invoices/overdue` | Get overdue invoices | `[{invoice}]` |
| GET | `/api/invoices/due-in-next-days` | Get upcoming invoices | `[{invoice}]` |

---

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- PostgreSQL 15+ with pgvector extension
- Google Gemini API key
- Gradle 8.x or Maven 3.9+

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/maninder-bltr/docu-leader-backend.git
cd docu-leader-backend
```

2. **Set up PostgreSQL with pgvector**
```sql
-- Connect to PostgreSQL
CREATE DATABASE doculeader;
\c doculeader;
CREATE EXTENSION vector;
```

3. **Configure application properties**
Edit `src/main/resources/application.properties`:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/doculeader
spring.datasource.username=postgres
spring.datasource.password=postgres

# Gemini API
spring.ai.vertex.ai.gemini.api-key=YOUR_GEMINI_API_KEY
spring.ai.vertex.ai.gemini.chat.model=gemini-pro
spring.ai.vertex.ai.gemini.embedding.model=embedding-001

# JWT
app.jwt.secret=your-256-bit-secret-key-here
app.jwt.expiration=86400000

# File storage
file.storage-dir=./storage
```

4. **Build the application**
```bash
# Using Gradle
./gradlew build

# Using Maven
./mvnw clean install
```

5. **Run the application**
```bash
# Using Gradle
./gradlew bootRun

# Using Maven
./mvnw spring-boot:run
```

The application will start on `http://localhost:8088`

---

## 🧪 Testing

Run tests:
```bash
# Unit tests
./gradlew test

# Integration tests
./gradlew integrationTest

# With coverage
./gradlew test jacocoTestReport
```

---

## 📈 Performance Optimizations

1. **Async Processing**: Document processing runs asynchronously to prevent blocking
2. **Connection Pooling**: HikariCP for efficient database connections
3. **Batch Inserts**: Chunks saved in batches for better performance
4. **Indexing**: Proper indexes on frequently queried columns
5. **HNSW Index**: Efficient approximate nearest neighbor search for vectors

---

## 🔒 Security Features

1. **JWT Authentication**: Stateless authentication
2. **Password Encryption**: BCrypt with strength 10
3. **CORS Configuration**: Controlled cross-origin access
4. **Input Validation**: All inputs validated
5. **SQL Injection Prevention**: JPA parameterized queries
6. **Rate Limiting**: (Optional) Protection against abuse

---

## 📚 API Documentation

Interactive API documentation available via Swagger UI when running:
```
http://localhost:8088/swagger-ui.html
```

---

## 🏗️ Building for Production

```bash
# Build JAR
./gradlew bootJar

# Run with production profile
java -jar build/libs/docu-leader-backend-*.jar --spring.profiles.active=prod
```

### Production Considerations

- Use environment variables for sensitive data
- Configure proper connection pool size
- Set up monitoring and logging
- Enable database connection encryption (SSL)
- Use a reverse proxy (nginx) for load balancing

---

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Coding Standards

- Follow Java naming conventions
- Write unit tests for new features
- Update documentation
- Use meaningful commit messages

---

## 📄 License

This project is proprietary and confidential. Unauthorized copying, distribution, or use is strictly prohibited.

---

## 👥 Team

- **Maninder Singh** - Lead Backend Developer

---

## 📞 Support

For support, email maninder.bltr@gmail.com or create an issue in the repository.

---

## 🎯 Future Roadmap

- [ ] Multi-user workspaces with role-based access
- [ ] Document versioning and audit trail
- [ ] Scheduled automatic reminders (cron jobs)
- [ ] Email provider integration
- [ ] Advanced analytics dashboard
- [ ] Support for more document types
- [ ] Batch document processing
- [ ] Custom document templates
- [ ] Multi-language support for OCR
- [ ] Real-time collaboration features
