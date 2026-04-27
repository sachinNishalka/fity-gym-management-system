# Member Features - Complete Overview

This document provides comprehensive documentation of all member-related features currently implemented in the Fity Gym Management System.

---

## Table of Contents

1. [Feature: Member Registration](#1-feature-member-registration)
2. [Feature: Get All Members](#2-feature-get-all-members)
3. [Feature: Get Member By ID](#3-feature-get-member-by-id)
4. [Feature: Update Member](#4-feature-update-member)
5. [Feature: Delete Member](#5-feature-delete-member)
6. [Database Schema](#6-database-schema-for-member-features)
7. [Future Enhancements](#7-future-enhancements)

---

## Date and Time Format Standards

### Overview
The Fity API uses standardized date and time formats for consistent serialization and deserialization across all endpoints.

### Date Format (LocalDate)
**Format**: `yyyy-MM-dd`  
**Example**: `"1990-05-15"`  
**Used For**: 
- `dateOfBirth`
- `allowedUntil` (in MemberAccess)
- Any date-only fields

**Valid Examples:**
```json
{
  "dateOfBirth": "1990-05-15",
  "dateOfBirth": "2024-01-01",
  "dateOfBirth": "1985-12-31"
}
```

**Invalid Examples:**
```json
{
  "dateOfBirth": "05-15-1990",        // ❌ Wrong order
  "dateOfBirth": "1990/05/15",        // ❌ Wrong separator
  "dateOfBirth": "1990-5-15",         // ❌ Missing leading zeros
  "dateOfBirth": "1990-05-15T00:00:00" // ❌ Includes time (use LocalDate)
}
```

### DateTime Format (LocalDateTime)
**Format**: `yyyy-MM-dd'T'HH:mm:ss`  
**Example**: `"2024-01-15T10:30:45"`  
**Used For**: 
- `joinedDate` (read-only, set by server)
- Timestamps in response DTOs

**Important**: 
- ⚠️ **Read-only fields** - These are automatically set by the server and should NOT be included in request payloads
- ✅ **Response only** - You'll see these in GET responses but don't send them in POST/PUT requests

**Response Example:**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "joinedDate": "2024-01-15T10:30:45",
  "status": "ACTIVE"
}
```

### Jackson Annotations Used

The application uses Jackson annotations for proper date/time handling:

```java
@Data
public class MemberDTO {
    
    // Date field with format specification
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    
    // DateTime field with format specification (response only)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime joinedDate;
}
```

### Common Errors and Solutions

#### Error 1: Cannot deserialize LocalDateTime from date string
```
Error: Cannot deserialize value of type `java.time.LocalDateTime` from String "2024-01-15"
```

**Cause**: Trying to send a date-only string to a LocalDateTime field

**Solution**: 
- Don't send `joinedDate` in requests (it's auto-set)
- If you must send it, use full datetime format: `"2024-01-15T10:30:00"`

#### Error 2: Cannot parse LocalDate
```
Error: Text '2024/01/15' could not be parsed at index 4
```

**Cause**: Wrong date format (using `/` instead of `-`)

**Solution**: Use `yyyy-MM-dd` format: `"2024-01-15"`

#### Error 3: Unexpected date format
```
Error: Text '15-01-2024' could not be parsed at index 2
```

**Cause**: Wrong date order (DD-MM-YYYY instead of YYYY-MM-DD)

**Solution**: Use ISO format: `"2024-01-15"`

### Best Practices

1. **Request Payload** - Only include editable fields:
   ```json
   {
     "firstName": "John",
     "dateOfBirth": "1990-05-15",
     "phoneNumber": "0771234567"
   }
   ```

2. **Omit Auto-Generated Fields**:
   - ❌ Don't send: `id`, `joinedDate`, `status`
   - ✅ These are set automatically by the server

3. **Date Validation**:
   - Dates should be in the past for `dateOfBirth`
   - Use current date for registration (`joinedDate` is auto-set)

4. **Time Zones**:
   - Server uses system default timezone
   - All timestamps are in server's local time
   - Consider adding timezone support in future (e.g., `ZonedDateTime`)

### Testing with cURL

**Correct Date Format:**
```bash
curl -X POST http://localhost:8080/api/v1/member/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-05-15",
    "phoneNumber": "0771234567",
    "email": "john@example.com"
  }'
```

**Response includes auto-generated fields:**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-05-15",
  "phoneNumber": "0771234567",
  "email": "john@example.com",
  "joinedDate": "2024-01-15T10:30:45",
  "status": "ACTIVE"
}
```

---

## 1. Feature: Member Registration

### Description
Registers a new member in the gym management system with complete profile information, health details, and fitness goals. Automatically creates a blocked member access record and queues fingerprint device registration.

### API Endpoint
```
POST /api/v1/member/register
```

### Request DTO
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-05-15",
  "phoneNumber": "0771234567",
  "email": "john.doe@example.com",
  "gender": "male",
  "memberCode": "MEM001",
  "address": "123 Main Street, Colombo",
  "height": 175.5,
  "weight": 70.0,
  "idNumber": "901234567V",
  "emergencyNumber": "0779876543",
  "facebookName": "johndoe",
  
  // Fitness Goals
  "bodyBuilding": true,
  "fatBurning": false,
  "physicalFitness": true,
  "sportsSkills": false,
  "bodyShape": true,
  "otherService": "Yoga classes",
  
  // Health Information
  "cholesterol": false,
  "bloodPressure": false,
  "diabetes": false,
  "heartProblem": false,
  "surgery": false,
  "fractures": false,
  "kidneyLiver": false,
  "otherDisease": false,
  "currentTreatment": false
}
```

**Important Notes:**
- ❌ **Do NOT send** `joinedDate` - It's automatically set by the server
- ❌ **Do NOT send** `status` - It's automatically set to `ACTIVE`
- ❌ **Do NOT send** `id` - It's auto-generated
- ✅ **Date format**: `yyyy-MM-dd` (e.g., "1990-05-15")
- ✅ All fields are optional except `firstName`, `lastName`, and `phoneNumber`

### Response
```
Status: 201 CREATED
Body: MemberDTO (same structure as request)
```

### Business Rules

1. **Automatic Status Assignment**
   - All new members are assigned `MemberStatus.ACTIVE` by default
   - This is set by the service layer, not provided by the client

2. **Automatic Timestamp**
   - `joinedDate` is automatically set to the current server timestamp
   - Uses `LocalDateTime.now()` to ensure accuracy

3. **Member Access Creation**
   - A `MemberAccess` record is automatically created with:
     - `accessStatus`: `BLOCKED` (default for new members)
     - `reason`: "New member registration - awaiting subscription"
     - `allowedUntil`: `null` (no access until subscription is paid)
   - This ensures new members cannot access facilities until they purchase a subscription

4. **Device Integration**
   - Fingerprint device registration command is queued automatically
   - Command type: `REGISTER_MEMBER`
   - Status: `PENDING` (waiting for device to process)
   - If device command queueing fails, entire registration rolls back

5. **Unique Constraints**
   - `memberCode` must be unique across all members
   - `email` should be unique (validation recommended but not enforced at DB level)

### Transaction Flow

```
Start Transaction
    ├─ Convert DTO to Entity (using MemberMapper)
    ├─ Set status = ACTIVE
    ├─ Set joinedDate = now()
    ├─ Save Member to database
    ├─ Create MemberAccess (status = BLOCKED)
    ├─ Save MemberAccess to database
    ├─ Queue device command for fingerprint registration
    └─ Commit Transaction

On Error → Rollback all operations
```

### Exception Scenarios

| Scenario | Exception | HTTP Status |
|----------|-----------|-------------|
| Member save fails | `MemberRegistrationException` | 500 |
| MemberAccess creation fails | `MemberAccessCreationException` | 500 |
| Device command queueing fails | `DeviceCommandException` | 500 |
| Duplicate member code | `DataIntegrityViolationException` | 409 |

### Implementation Details

**Service Method:**
```java
@Override
@Transactional
public void registerMember(MemberDTO memberDTO) {
    try {
        // Use mapper to convert DTO to entity
        Member member = memberMapper.toEntity(memberDTO);
        
        // Override critical default values
        member.setStatus(MemberStatus.ACTIVE);
        member.setJoinedDate(LocalDateTime.now());
        
        // Save member
        member = memberRepository.save(member);
        log.info("Member registered successfully with ID: {}", member.getId());
        
        // Create MemberAccess record with BLOCKED status
        MemberAccess memberAccess = new MemberAccess();
        memberAccess.setMember(member);
        memberAccess.setAccessStatus(AccessStatus.BLOCKED);
        memberAccess.setReason("New member registration - awaiting subscription");
        memberAccess.setAllowedUntil(null);
        
        memberAccessRepository.save(memberAccess);
        log.info("MemberAccess created with BLOCKED status");
        
        // Queue device command for fingerprint registration
        deviceCommandService.queueMemberRegistration(member);
        log.info("Device command queued for member registration");
        
    } catch (DeviceCommandException e) {
        log.error("Failed to queue device command", e);
        throw e; // Re-throw to trigger transaction rollback
    } catch (Exception e) {
        log.error("Failed to register member", e);
        throw new MemberRegistrationException("Failed to register member");
    }
}
```

**Key Technologies:**
- MapStruct mapper for DTO ↔ Entity conversion
- Spring `@Transactional` for atomicity
- Custom exceptions for error handling
- SLF4J logging for audit trail

---

## 2. Feature: Get All Members

### Description
Retrieves a list of all registered members with their basic information and current access status.

### API Endpoint
```
GET /api/v1/member/all
```

### Request Parameters
None

### Response
```
Status: 200 OK
Body: List<MemberDTO>
```

**Example Response:**
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "0771234567",
    "email": "john.doe@example.com",
    "memberCode": "MEM001",
    "status": "ACTIVE",
    "memberAccessStatus": "ALLOWED",
    "joinedDate": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "firstName": "Jane",
    "lastName": "Smith",
    "phoneNumber": "0779876543",
    "email": "jane.smith@example.com",
    "memberCode": "MEM002",
    "status": "ACTIVE",
    "memberAccessStatus": "BLOCKED",
    "joinedDate": "2024-01-16T14:20:00"
  }
]
```

### Business Logic

1. **Access Status Computation**
   - If member has `MemberAccess` record: return actual status (`ALLOWED`, `BLOCKED`)
   - If no `MemberAccess` record exists: return `"ACCESS_DENIED"` (fallback)

2. **Data Transformation**
   - Uses `MemberMapper.toDto()` to convert entities to DTOs
   - Manually adds `memberAccessStatus` field (computed, not stored)

### Implementation Details

**Service Method:**
```java
@Override
public List<MemberDTO> getAllMembers() {
    List<Member> members = memberRepository.findAll();
    
    List<MemberDTO> memberDTOs = new ArrayList<>();
    
    for (Member member : members) {
        MemberDTO memberDTO = memberMapper.toDto(member);
        
        // Set access status
        if (member.getMemberAccess() != null) {
            memberDTO.setMemberAccessStatus(
                member.getMemberAccess().getAccessStatus().toString()
            );
        } else {
            memberDTO.setMemberAccessStatus("ACCESS_DENIED");
        }
        
        memberDTOs.add(memberDTO);
    }
    
    return memberDTOs;
}
```

### Performance Considerations

- Uses `FetchType.LAZY` for `MemberAccess` relationship
- Could benefit from `@EntityGraph` to optimize N+1 queries
- Consider pagination for large datasets (future enhancement)

---

## 3. Feature: Get Member By ID

### Description
Retrieves detailed information for a specific member including their access status.

### API Endpoint
```
GET /api/v1/member/{id}
```

### Path Parameters
- `id` (Long, required) - The unique identifier of the member

### Request Example
```
GET /api/v1/member/1
```

### Response
```
Status: 200 OK
Body: MemberDTO
```

**Example Response:**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-05-15",
  "phoneNumber": "0771234567",
  "email": "john.doe@example.com",
  "gender": "male",
  "memberCode": "MEM001",
  "address": "123 Main Street, Colombo",
  "height": 175.5,
  "weight": 70.0,
  "status": "ACTIVE",
  "memberAccessStatus": "ALLOWED",
  "joinedDate": "2024-01-15T10:30:00",
  "idNumber": "901234567V",
  "emergencyNumber": "0779876543",
  "bodyBuilding": true,
  "physicalFitness": true
}
```

### Exception Handling

**Member Not Found:**
```
Status: 404 NOT FOUND (handled by GlobalExceptionHandler)
Exception: MemberNotFoundException
Message: "Member not found with id: {id}"
```

### Implementation Details

**Service Method:**
```java
@Override
public MemberDTO getMemberById(Long id) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + id));
    
    MemberDTO memberDTO = memberMapper.toDto(member);
    
    // Set access status
    if (member.getMemberAccess() != null) {
        memberDTO.setMemberAccessStatus(
            member.getMemberAccess().getAccessStatus().toString()
        );
    } else {
        memberDTO.setMemberAccessStatus("ACCESS_DENIED");
    }
    
    return memberDTO;
}
```

### Use Cases

- Display member profile in admin dashboard
- View member details before creating subscription
- Check member access status
- Retrieve member information for device registration

---

## 4. Feature: Update Member

### Description
Updates existing member information. Allows modification of personal details, contact information, and health/fitness data.

### API Endpoint
```
PUT /api/v1/member/{id}
```

### Path Parameters
- `id` (Long, required) - The unique identifier of the member to update

### Request DTO
```json
{
  "firstName": "John",
  "lastName": "Doe Updated",
  "phoneNumber": "0771234567",
  "email": "john.updated@example.com",
  "address": "456 New Street, Colombo",
  "height": 176.0,
  "weight": 72.0,
  "emergencyNumber": "0779999999",
  "bodyBuilding": true,
  "fatBurning": true
}
```

### Response
```
Status: 200 OK
Body: Member entity
```

### Business Rules

**Updatable Fields:**
- Personal information (firstName, lastName, dateOfBirth, gender)
- Contact information (phoneNumber, email, address, emergencyNumber)
- Physical attributes (height, weight)
- Fitness goals (bodyBuilding, fatBurning, etc.)
- Health information (cholesterol, diabetes, etc.)

**Immutable Fields (Not Updated):**
- `id` - Primary key
- `memberCode` - Unique identifier
- `joinedDate` - Historical timestamp
- `status` - Use separate status change endpoint (future)

### Implementation Details

**Service Method:**
```java
@Override
public Member updateMember(Long memberId, MemberDTO memberDTO) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));
    
    // Use mapper to update existing entity
    Member updatedMember = memberMapper.updateMemberFromDto(memberDTO, member);
    
    return memberRepository.save(updatedMember);
}
```

**Mapper Configuration:**
```java
@Mapper(componentModel = "spring")
public interface MemberMapper {
    Member updateMemberFromDto(MemberDTO memberDTO, @MappingTarget Member member);
}
```

### Exception Handling

| Scenario | Exception | HTTP Status |
|----------|-----------|-------------|
| Member not found | `MemberNotFoundException` | 404 |
| Invalid data format | `MethodArgumentNotValidException` | 400 |
| Database constraint violation | `DataIntegrityViolationException` | 409 |

---

## 5. Feature: Delete Member

### Description
Deletes a member from the system. Currently performs hard delete (physical removal from database).

### API Endpoint
```
DELETE /api/v1/member/{id}
```

### Path Parameters
- `id` (Long, required) - The unique identifier of the member to delete

### Request Example
```
DELETE /api/v1/member/1
```

### Response
```
Status: 200 OK
Body: Empty
```

### Business Rules (Current Implementation)

⚠️ **Warning: Hard Delete** - This operation permanently removes the member record.

**Current Behavior:**
- Physically deletes member from database
- Cascades to related `MemberAccess` record (CASCADE.ALL)
- Does NOT check for active subscriptions (potential data integrity issue)

### Implementation Details

**Service Method:**
```java
@Override
public void deleteMember(Long memberId) {
    memberRepository.deleteById(memberId);
}
```

### Known Issues & Recommendations

❌ **Current Problems:**
1. No validation before deletion
2. Orphaned subscription records if member had subscriptions
3. No audit trail of deleted members
4. Cannot recover deleted data

✅ **Recommended Improvements:**

1. **Soft Delete Pattern:**
```java
@Override
public void deleteMember(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new MemberNotFoundException("Member not found"));
    
    // Check for active subscriptions
    if (subscriptionRepository.existsByMemberIdAndStatus(
            memberId, SubscriptionStatus.ACTIVE)) {
        throw new IllegalStateException(
            "Cannot delete member with active subscriptions");
    }
    
    // Soft delete - change status instead
    member.setStatus(MemberStatus.INACTIVE);
    memberRepository.save(member);
    
    log.info("Member {} soft deleted", memberId);
}
```

2. **Add audit fields:**
```java
@Column(name = "deleted_at")
private LocalDateTime deletedAt;

@Column(name = "deleted_by")
private String deletedBy;
```

---

## 6. Database Schema for Member Features

### Member Table

**Table Name:** `member`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique member identifier |
| `first_name` | VARCHAR(255) | NOT NULL | Member's first name |
| `last_name` | VARCHAR(255) | NOT NULL | Member's last name |
| `date_of_birth` | DATE | | Date of birth |
| `phone_number` | VARCHAR(20) | NOT NULL | Contact phone number |
| `email` | VARCHAR(255) | | Email address |
| `joined_date` | TIMESTAMP | NOT NULL | Registration timestamp |
| `status` | VARCHAR(20) | NOT NULL | ACTIVE, INACTIVE, SUSPENDED |
| `gender` | VARCHAR(10) | | male, female, other |
| `member_code` | VARCHAR(50) | UNIQUE, NOT NULL | Unique member code |
| `address` | VARCHAR(500) | | Physical address |
| `height` | FLOAT | | Height in cm |
| `weight` | FLOAT | | Weight in kg |
| `id_number` | VARCHAR(50) | | National ID number |
| `emergency_number` | VARCHAR(20) | | Emergency contact |
| `facebook_name` | VARCHAR(100) | | Facebook profile name |
| `family_id` | BIGINT | FOREIGN KEY | Reference to family (nullable) |
| `body_building` | BOOLEAN | | Fitness goal |
| `fat_burning` | BOOLEAN | | Fitness goal |
| `physical_fitness` | BOOLEAN | | Fitness goal |
| `sports_skills` | BOOLEAN | | Fitness goal |
| `body_shape` | BOOLEAN | | Fitness goal |
| `other_service` | VARCHAR(255) | | Other fitness goals |
| `cholesterol` | BOOLEAN | | Health condition |
| `blood_pressure` | BOOLEAN | | Health condition |
| `diabetes` | BOOLEAN | | Health condition |
| `heart_problem` | BOOLEAN | | Health condition |
| `surgery` | BOOLEAN | | Health history |
| `fractures` | BOOLEAN | | Health history |
| `kidney_liver` | BOOLEAN | | Health condition |
| `other_disease` | BOOLEAN | | Other health issues |
| `current_treatment` | BOOLEAN | | Currently under treatment |

### MemberAccess Table

**Table Name:** `member_access`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `member_id` | BIGINT | PRIMARY KEY, FOREIGN KEY | References member.id |
| `access_status` | VARCHAR(20) | NOT NULL | ALLOWED, BLOCKED |
| `allowed_until` | DATE | | Access expiry date |
| `reason` | VARCHAR(500) | | Reason for current status |
| `updated_at` | TIMESTAMP | NOT NULL | Last update timestamp |

### Entity Relationships

```
Member (1) ←→ (1) MemberAccess
  │
  │ (1) ←→ (Many)
  │
  ├── Subscription
  │
  │ (Many) ←→ (1)
  │
  └── Family
```

**Relationship Details:**

1. **Member ↔ MemberAccess (One-to-One)**
   - Cascade: ALL
   - Fetch: LAZY
   - Mapping: `@OneToOne(mappedBy = "member")`
   - Shared primary key using `@MapsId`

2. **Member ↔ Subscription (One-to-Many)**
   - Cascade: ALL
   - Fetch: LAZY
   - Mapping: `@OneToMany(mappedBy = "member")`
   - A member can have multiple subscriptions over time

3. **Member ↔ Family (Many-to-One)**
   - Fetch: LAZY
   - Mapping: `@ManyToOne`
   - Optional relationship (members can exist without family)

### Indexes

**Recommended Indexes:**
```sql
CREATE INDEX idx_member_code ON member(member_code);
CREATE INDEX idx_member_status ON member(status);
CREATE INDEX idx_member_email ON member(email);
CREATE INDEX idx_member_phone ON member(phone_number);
CREATE INDEX idx_member_family ON member(family_id);
```

---

## 7. Future Enhancements

### Planned Features (Not Yet Implemented)

#### 7.1 Member Search & Filtering
```
GET /api/v1/member/search?name=john&phone=077&status=ACTIVE
```
- Search by name (first name, last name, or both)
- Filter by phone number (partial match)
- Filter by email
- Filter by status
- Filter by family membership
- Pagination support (`page`, `size`, `sort`)

#### 7.2 Member Status Management
```
PATCH /api/v1/member/{id}/status
Body: { "status": "SUSPENDED", "reason": "Payment overdue" }
```
- Change member status (ACTIVE, INACTIVE, SUSPENDED)
- Require reason for status change
- Audit trail (who changed, when, why)
- Automatically update access permissions

#### 7.3 Member Subscription History
```
GET /api/v1/member/{id}/subscriptions
```
- List all subscriptions (current and historical)
- Include subscription status, dates, plan details
- Show payment status for each subscription
- Filter by subscription status

#### 7.4 Member Payment History
```
GET /api/v1/member/{id}/payments
```
- List all payments made by member
- Include receipt numbers, amounts, dates
- Filter by date range
- Calculate total paid amount

#### 7.5 Member Attendance Tracking
```
GET /api/v1/member/{id}/attendance?from=2024-01-01&to=2024-01-31
```
- View gym entry/exit records
- Calculate attendance frequency
- Generate attendance reports
- Identify inactive members

#### 7.6 Member Plan Eligibility Check
```
GET /api/v1/member/{id}/eligible-plans
```
- Check which plans member can purchase
- Consider age restrictions (kids plans)
- Check family membership eligibility
- Show available discounts

#### 7.7 Batch Operations
```
POST /api/v1/member/bulk-import
POST /api/v1/member/bulk-status-update
```
- Import multiple members from CSV
- Bulk status updates
- Bulk notification sending

#### 7.8 Member Analytics
```
GET /api/v1/member/analytics
```
- Total active members count
- New member registrations (by period)
- Member retention rate
- Member demographics (age, gender distribution)
- Popular fitness goals

### Technical Debt

1. **Validation**
   - Add `@Valid` annotation to controller methods
   - Add validation constraints to MemberDTO
   - Implement custom validators for phone numbers, emails

2. **Security**
   - Add authentication/authorization
   - Role-based access control (only staff can register members)
   - Audit logging for sensitive operations

3. **Performance**
   - Add pagination to `getAllMembers()`
   - Implement query optimization with `@EntityGraph`
   - Add caching for frequently accessed members

4. **Data Integrity**
   - Soft delete implementation
   - Prevent deletion with active subscriptions
   - Add database constraints (UNIQUE email, phone format validation)

5. **Documentation**
   - Add Swagger/OpenAPI documentation
   - Add request/response examples
   - Document error codes and messages

---

## API Summary Table

| Feature | Method | Endpoint | Status |
|---------|--------|----------|--------|
| Register Member | POST | `/api/v1/member/register` | ✅ Implemented |
| Get All Members | GET | `/api/v1/member/all` | ✅ Implemented |
| Get Member By ID | GET | `/api/v1/member/{id}` | ✅ Implemented |
| Update Member | PUT | `/api/v1/member/{id}` | ✅ Implemented |
| Delete Member | DELETE | `/api/v1/member/{id}` | ✅ Implemented (needs improvement) |
| Search Members | GET | `/api/v1/member/search` | ❌ Not Implemented |
| Change Status | PATCH | `/api/v1/member/{id}/status` | ❌ Not Implemented |
| Get Subscriptions | GET | `/api/v1/member/{id}/subscriptions` | ❌ Not Implemented |
| Get Payments | GET | `/api/v1/member/{id}/payments` | ❌ Not Implemented |
| Get Attendance | GET | `/api/v1/member/{id}/attendance` | ❌ Not Implemented |
| Get Eligible Plans | GET | `/api/v1/member/{id}/eligible-plans` | ❌ Not Implemented |

---

## Related Documentation

- [Database Relationships](../DATABASE_RELATIONSHIPS.md)
- [Exception Structure](../EXCEPTION_STRUCTURE_SUMMARY.md)
- [Coding Style Guide](../CODING_STYLE_GUIDE.md)
- [Implementation Guide](../IMPLEMENTATION_GUIDE.md)

---

**Last Updated:** January 2025  
**Version:** 1.0  
**Author:** Fity Development Team
