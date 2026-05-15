# API Reference

## Endpoints

### Health Check
**GET** `/api/health.php`

Check server and database status.

**Response:**
```json
{
  "success": true,
  "status": "healthy",
  "database": {
    "connected": true,
    "database": "ultradb_prod",
    "host": "localhost",
    "timestamp": "2026-05-15 10:30:45"
  },
  "timestamp": 1715769045
}
```

### Query Endpoint
**POST** `/api/query.php`

Execute database queries.

**Headers:**
```
X-API-Token: your_api_token
Content-Type: application/x-www-form-urlencoded
```

**Parameters:**
- `action` - "select" or "execute" or "health"
- `query` - SQL query
- `readOnly` - true/false (default: true)
- `token` - API token (alternative to header)

**Example - SELECT:**
```
POST /api/query.php
X-API-Token: abc123...

action=select&query=SELECT * FROM users WHERE id=1
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com"
    }
  ],
  "count": 1
}
```

**Example - INSERT:**
```
POST /api/query.php
X-API-Token: abc123...

action=execute&query=INSERT INTO users (name, email) VALUES ('Jane', 'jane@example.com')
```

**Response:**
```json
{
  "success": true,
  "rowsAffected": 1
}
```

### Mail Endpoint
**POST** `/api/mail.php`

Send emails through the server.

**Headers:**
```
X-API-Token: your_api_token
Content-Type: application/x-www-form-urlencoded
```

**Parameters:**
- `to` - Recipient email
- `subject` - Email subject
- `message` - Email body
- `cc` - CC recipients (comma-separated)
- `bcc` - BCC recipients (comma-separated)
- `replyTo` - Reply-To address
- `fromName` - Sender name
- `isHtml` - 1 for HTML, 0 for plain text

**Example:**
```
POST /api/mail.php
X-API-Token: abc123...

to=user@example.com&subject=Hello&message=Welcome!&isHtml=0
```

**Response:**
```json
{
  "success": true,
  "message": "Email sent"
}
```

---

## Error Responses

### 401 Unauthorized
```json
{
  "success": false,
  "error": "Unauthorized"
}
```
**Cause:** Invalid or missing API token

### 400 Bad Request
```json
{
  "success": false,
  "error": "Missing required fields"
}
```
**Cause:** Missing required parameters

### 403 Forbidden
```json
{
  "success": false,
  "error": "Query not allowed"
}
```
**Cause:** Query uses forbidden operation (DROP, ALTER, etc.)

### 405 Method Not Allowed
```json
{
  "success": false,
  "error": "Method not allowed"
}
```
**Cause:** Wrong HTTP method (use POST)

### 500 Server Error
```json
{
  "success": false,
  "error": "Server error"
}
```
**Cause:** Internal server error

---

## Extension Blocks Reference

### Connection Management
- `Initialize(serverUrl)` - Set server address
- `Connect(host, user, pass, database)` - Connect to database
- `Disconnect()` - Close connection
- `TestConnection()` - Test active connection
- `GetStatus()` - Check if connected
- `HealthCheck()` - Full health check
- `SetAutoReconnect(enable, timeout)` - Auto reconnection settings

### Query Operations
- `Query(sql)` - Execute SELECT, get list result
- `QueryJSON(sql)` - Execute SELECT, get JSON result
- `ExecuteUpdate(sql)` - Execute INSERT/UPDATE/DELETE
- `BatchExecute(queries)` - Execute multiple queries
- `GetRowCount()` - Get affected rows from last operation
- `GetColumnInfo()` - Get column metadata

### Transactions
- `BeginTransaction()` - Start transaction
- `Commit()` - Commit transaction
- `Rollback()` - Rollback transaction

### Security
- `EncryptValue(value)` - AES-256 encrypt
- `DecryptValue(encrypted)` - AES-256 decrypt
- `HashValue(value)` - SHA-256 hash with salt
- `VerifyHash(value, hash)` - Verify hash

### Mail
- `SendMail(to, subject, message)` - Simple mail
- `SendMailAdvanced(to, subject, message, fromName, replyTo, isHtml)` - Advanced options
- `SendMailWithCcBcc(to, cc, bcc, subject, message, isHtml)` - With CC/BCC

### Configuration
- `SetSecureConfig(key, value)` - Store encrypted config
- `GetSecureConfig(key)` - Retrieve encrypted config
- `ExportConfig()` - Export all config as JSON
- `ImportConfig(json)` - Import config from JSON

### Events
- `OnInitialized()` - Extension initialized
- `OnConnected()` - Connected to database
- `OnDisconnected()` - Disconnected from database
- `OnConnectionTest(success)` - Connection test result
- `OnHealthCheck(result)` - Health check result (JSON)
- `OnQueryResult(result)` - Query result (JSON)
- `OnQueryListResult(result)` - Query result (list)
- `OnUpdateComplete(rowCount)` - Update completed
- `OnBatchComplete(rowsAffected)` - Batch completed
- `OnTransactionBegin()` - Transaction started
- `OnTransactionCommit()` - Transaction committed
- `OnTransactionRollback()` - Transaction rolled back
- `OnMailSent()` - Email sent successfully
- `OnConfigSet(key)` - Config saved
- `OnConfigImported()` - Config imported
- `OnError(operation, message)` - Error occurred

---

## Request Examples

### Python
```python
import requests

API_URL = "https://your-domain.com/api"
TOKEN = "your_api_token"

# Health check
response = requests.get(
    f"{API_URL}/health.php",
    headers={"X-API-Token": TOKEN}
)
print(response.json())

# Query
response = requests.post(
    f"{API_URL}/query.php",
    headers={"X-API-Token": TOKEN},
    data={
        "action": "select",
        "query": "SELECT * FROM users"
    }
)
print(response.json())

# Mail
response = requests.post(
    f"{API_URL}/mail.php",
    headers={"X-API-Token": TOKEN},
    data={
        "to": "user@example.com",
        "subject": "Hello",
        "message": "Welcome!",
        "isHtml": "0"
    }
)
print(response.json())
```

### JavaScript/Node.js
```javascript
const API_URL = "https://your-domain.com/api";
const TOKEN = "your_api_token";

// Query
fetch(`${API_URL}/query.php`, {
  method: "POST",
  headers: {
    "X-API-Token": TOKEN,
    "Content-Type": "application/x-www-form-urlencoded"
  },
  body: new URLSearchParams({
    action: "select",
    query: "SELECT * FROM users"
  })
})
.then(r => r.json())
.then(data => console.log(data));

// Mail
fetch(`${API_URL}/mail.php`, {
  method: "POST",
  headers: {
    "X-API-Token": TOKEN,
    "Content-Type": "application/x-www-form-urlencoded"
  },
  body: new URLSearchParams({
    to: "user@example.com",
    subject: "Hello",
    message: "Welcome!",
    isHtml: "0"
  })
})
.then(r => r.json())
.then(data => console.log(data));
```

### cURL
```bash
# Health check
curl -H "X-API-Token: your_token" \
  https://your-domain.com/api/health.php

# Query
curl -X POST \
  -H "X-API-Token: your_token" \
  -d "action=select&query=SELECT * FROM users" \
  https://your-domain.com/api/query.php

# Mail
curl -X POST \
  -H "X-API-Token: your_token" \
  -d "to=user@example.com&subject=Hello&message=Welcome&isHtml=0" \
  https://your-domain.com/api/mail.php
```

---

## Rate Limiting

Default: 100 requests per hour per IP

**Response on limit exceeded:**
```json
{
  "success": false,
  "error": "Rate limit exceeded"
}
```

**Retry-After header:** Seconds until next request allowed

---

## Status Codes

| Code | Meaning | Action |
|------|---------|--------|
| 200 | Success | Use response data |
| 400 | Bad request | Check parameters |
| 401 | Unauthorized | Check API token |
| 403 | Forbidden | Check permissions |
| 405 | Method not allowed | Use POST |
| 429 | Rate limited | Wait then retry |
| 500 | Server error | Check logs |
| 503 | Service unavailable | Try later |

---

*API Version: 1.0*
*Last Updated: 2026-05-15*
