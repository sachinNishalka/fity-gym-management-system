# Member API Testing Examples

This document provides practical examples for testing the Member API endpoints with correct date/time formats.

---

## Test Case 1: SUCCESS - Valid Member Registration

### cURL Command
```bash
curl -X POST http://localhost:8080/api/v1/member/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-05-15",
    "phoneNumber": "0771234567",
    "email": "john.doe@example.com",
    "gender": "male",
    "memberCode": "JD001",
    "address": "123 Main Street, Colombo",
    "height": 175.5,
    "weight": 70.0,
    "idNumber": "901234567V",
    "emergencyNumber": "0779876543",
    "facebookName": "johndoe",
    "bodyBuilding": true,
    "fatBurning": false,
    "physicalFitness": true,
    "sportsSkills": false,
    "bodyShape": true,
    "otherService": "Yoga classes",
    "cholesterol": false,
    "bloodPressure": false,
    "diabetes": false,
    "heartProblem": false,
    "surgery": false,
    "fractures": false,
    "kidneyLiver": false,
    "otherDisease": false,
    "currentTreatment": false
  }'
```

### Expected Response
```json
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-05-15",
  "phoneNumber": "0771234567",
  "email": "john.doe@example.com",
  "joinedDate": "2024-01-15T10:30:45",
  "status": "ACTIVE",
  "gender": "male",
  "memberCode": "JD001",
  "address": "123 Main Street, Colombo",
  "height": 175.5,
  "weight": 70.0,
  "idNumber": "901234567V",
  "emergencyNumber": "0779876543",
  "facebookName": "johndoe",
  "bodyBuilding": true,
  "fatBurning": false,
  "physicalFitness": true,
  "sportsSkills": false,
  "bodyShape": true,
  "otherService": "Yoga classes",
  "cholesterol": false,
  "bloodPressure": false,
  "diabetes": false,
  "heartProblem": false,
  "surgery": false,
  "fractures": false,
  "kidneyLiver": false,
  "otherDisease": false,
  "currentTreatment": false,
  "memberAccessStatus": "BLOCKED"
}
```

**Note**: 
- ✅ `joinedDate` is automatically set by server
- ✅ `status` is automatically set to "ACTIVE"
- ✅ `memberAccessStatus` is automatically set to "BLOCKED"

---

## Test Case 2: SUCCESS - Minimal Registration

### cURL Command
```bash
curl -X POST http://localhost:8080/api/v1/member/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "phoneNumber": "0779876543",
    "dateOfBirth": "1985-12-20",
    "memberCode": "JS001"
  }'
```

### Expected Response
```json
HTTP/1.1 201 Created

{
  "id": 2,
  "firstName": "Jane",
  "lastName": "Smith",
  "dateOfBirth": "1985-12-20",
  "phoneNumber": "0779876543",
  "memberCode": "JS001",
  "joinedDate": "2024-01-15T11:00:00",
  "status": "ACTIVE",
  "memberAccessStatus": "BLOCKED"
}
```

---

## Test Case 3: ERROR - Duplicate Member Code

### cURL Command
```bash
curl -X POST http://localhost:8080/api/v1/member/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Duplicate",
    "lastName": "User",
    "phoneNumber": "0771111111",
    "dateOfBirth": "1995-01-01",
    "memberCode": "JD001"
  }'
```

### Expected Response
```json
HTTP/1.1 409 Conflict

{
  "timestamp": "2024-01-15T11:05:00",
  "status": 409,
  "error": "Conflict",
  "message": "Member code already exists",
  "path": "/api/v1/member/register"
}
```

---

## Test Case 4: ERROR - Invalid Date Format

### cURL Command (Wrong Format)
```bash
curl -X POST http://localhost:8080/api/v1/member/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Wrong",
    "lastName": "Date",
    "phoneNumber": "0771234567",
    "dateOfBirth": "05-15-1990"
  }'
```

### Expected Response
```json
HTTP/1.1 400 Bad Request

{
  "timestamp": "2024-01-15T11:10:00",
  "status": 400,
  "error": "Bad Request",
  "message": "JSON parse error: Cannot deserialize value",
  "path": "/api/v1/member/register"
}
```

**Solution**: Use correct format `"1990-05-15"`

---

## Test Case 5: ERROR - Sending joinedDate (Should Not)

### cURL Command (Incorrect - includes joinedDate)
```bash
curl -X POST http://localhost:8080/api/v1/member/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "0771234567",
    "dateOfBirth": "1990-05-15",
    "joinedDate": "2024-01-15"
  }'
```

### Expected Behavior
The server will **ignore** the `joinedDate` field and set it automatically.

**Best Practice**: Don't include `joinedDate` in requests at all!

---

## Test Case 6: Get Member By ID

### cURL Command
```bash
curl -X GET http://localhost:8080/api/v1/member/1 \
  -H "Accept: application/json"
```

### Expected Response
```json
HTTP/1.1 200 OK

{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-05-15",
  "phoneNumber": "0771234567",
  "email": "john.doe@example.com",
  "joinedDate": "2024-01-15T10:30:45",
  "status": "ACTIVE",
  "memberCode": "JD001",
  "memberAccessStatus": "BLOCKED"
}
```

**Note**: Response includes `joinedDate` in full datetime format

---

## Test Case 7: Update Member

### cURL Command
```bash
curl -X PUT http://localhost:8080/api/v1/member/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe Updated",
    "phoneNumber": "0771234567",
    "email": "john.updated@example.com",
    "address": "456 New Street, Colombo",
    "height": 176.0,
    "weight": 72.0
  }'
```

### Expected Response
```json
HTTP/1.1 200 OK

{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe Updated",
  "phoneNumber": "0771234567",
  "email": "john.updated@example.com",
  "address": "456 New Street, Colombo",
  "height": 176.0,
  "weight": 72.0,
  "joinedDate": "2024-01-15T10:30:45",
  "status": "ACTIVE",
  "memberCode": "JD001"
}
```

**Note**: `joinedDate` remains unchanged (immutable field)

---

## Test Case 8: Get All Members

### cURL Command
```bash
curl -X GET http://localhost:8080/api/v1/member/all \
  -H "Accept: application/json"
```

### Expected Response
```json
HTTP/1.1 200 OK

[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-05-15",
    "phoneNumber": "0771234567",
    "email": "john.doe@example.com",
    "joinedDate": "2024-01-15T10:30:45",
    "status": "ACTIVE",
    "memberCode": "JD001",
    "memberAccessStatus": "BLOCKED"
  },
  {
    "id": 2,
    "firstName": "Jane",
    "lastName": "Smith",
    "dateOfBirth": "1985-12-20",
    "phoneNumber": "0779876543",
    "joinedDate": "2024-01-15T11:00:00",
    "status": "ACTIVE",
    "memberCode": "JS001",
    "memberAccessStatus": "ALLOWED"
  }
]
```

---

## Using Postman

### Setup
1. Create a new request collection: "Fity Member API"
2. Set base URL: `http://localhost:8080`
3. Add header: `Content-Type: application/json`

### Register Member Request
- **Method**: POST
- **URL**: `{{baseUrl}}/api/v1/member/register`
- **Body** (raw JSON):
```json
{
  "firstName": "Test",
  "lastName": "User",
  "dateOfBirth": "1990-05-15",
  "phoneNumber": "0771234567",
  "email": "test@example.com",
  "memberCode": "TEST001"
}
```

### Environment Variables
```
baseUrl = http://localhost:8080/api/v1
```

---

## Common Mistakes to Avoid

### ❌ Mistake 1: Sending joinedDate in request
```json
{
  "firstName": "John",
  "joinedDate": "2024-01-15T10:30:00"  // ❌ Don't send this
}
```

**Fix**: Remove `joinedDate` from request

### ❌ Mistake 2: Wrong date format
```json
{
  "dateOfBirth": "15-05-1990"  // ❌ Wrong format
}
```

**Fix**: Use `"1990-05-15"` (yyyy-MM-dd)

### ❌ Mistake 3: Including time in date field
```json
{
  "dateOfBirth": "1990-05-15T00:00:00"  // ❌ Date only, no time
}
```

**Fix**: Use `"1990-05-15"` without time

### ❌ Mistake 4: Sending status
```json
{
  "firstName": "John",
  "status": "ACTIVE"  // ❌ Auto-set by server
}
```

**Fix**: Remove `status` from request

---

## Date Format Quick Reference

| Field | Type | Format | Example | Read-Only |
|-------|------|--------|---------|-----------|
| `dateOfBirth` | LocalDate | `yyyy-MM-dd` | `"1990-05-15"` | No |
| `joinedDate` | LocalDateTime | `yyyy-MM-dd'T'HH:mm:ss` | `"2024-01-15T10:30:45"` | Yes |
| `id` | Long | Number | `1` | Yes |
| `status` | MemberStatus | Enum | `"ACTIVE"` | Yes |

---

## Testing Checklist

- [ ] Valid registration with all fields
- [ ] Minimal registration (required fields only)
- [ ] Duplicate member code (should fail with 409)
- [ ] Invalid date format (should fail with 400)
- [ ] Get member by ID
- [ ] Update existing member
- [ ] Get all members
- [ ] Delete member

---

**Last Updated**: January 2025  
**Version**: 1.0
