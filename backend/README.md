# Syntopia Backend (Spring Boot)

Spring Boot 3 API serving the Syntopia platform. This document summarizes the standardized API response format and global error handling.

## Run locally
- Prereqs: Java 21+, ArangoDB (see repository root for Docker Compose)
- Start: `./mvnw spring-boot:run` (or `mvn spring-boot:run`)

## Standardized API Responses
All endpoints return a unified envelope via `ApiResponse`.

- Success with data:
  ```json
  { "success": true, "data": { "...": "..." } }
  ```
- Success with message and data:
  ```json
  { "success": true, "message": "Done", "data": { "...": "..." } }
  ```
- Paginated success:
  ```json
  {
    "success": true,
    "data": [ { "...": "..." } ],
    "pagination": {
      "page": 0,
      "size": 20,
      "total": 123,
      "totalPages": 7,
      "hasNext": true,
      "hasPrev": false
    }
  }
  ```
- Error:
  ```json
  { "success": false, "error": "Reason" }
  ```
- Error with details (structured):
  ```json
  { "success": false, "error": "Reason", "details": { "...": "..." } }
  ```

### Validation errors (400)
On `@Valid` request failures, the `GlobalExceptionHandler` returns field-level errors:
```json
{
  "success": false,
  "error": "Validation failed",
  "details": {
    "validationErrors": {
      "fieldA": "must not be blank",
      "fieldB": "must match pattern"
    }
  }
}
```

### Unauthorized (401)
When a controller throws `SecurityException` (e.g., missing auth) OR the Spring Security filter chain blocks an unauthenticated request:
```json
{ "success": false, "error": "Authentication required" }
```

### Other mappings
Handled centrally by `GlobalExceptionHandler`:
- `IllegalArgumentException` → 400 `{ success:false, error:"..." }`
- `SecurityException` → 401 `{ success:false, error:"..." }`
- `Exception` (fallback) → 500 `{ success:false, error:"Internal server error", details:"..." }`

## Client handling tips
- Always check `success`.
- On failure, read `error` for a human-readable message.
- If present, `details.validationErrors` contains a map of field → message.
- For lists, use `pagination` to drive paging UI.

## Tests
Controller and handler behavior is covered with WebMvc tests, including:
- Validation mapping (400 with `details.validationErrors`)
- Security mapping (401)
- Generic error mapping (500)

This ensures the response contract remains consistent across endpoints.
