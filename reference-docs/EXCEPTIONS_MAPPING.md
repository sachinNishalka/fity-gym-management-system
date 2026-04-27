# Exception Handling Structure

This document maps all custom exceptions in the system, organized by service layer.

## Base Exception
- **BusinessExceptionHandler** (`pro.sachin.fity.exception`)
  - Abstract base class for all business exceptions
  - Extends `RuntimeException`

---

## 1. GraceExtensionExceptions
**Location**: `pro.sachin.fity.exception.GraceExtensionExceptions`

**Service**: GraceExtensionServiceImpl

| Exception | Thrown When | Line Reference |
|-----------|-------------|----------------|
| `NotInGracePeriodException` | Attempting to extend grace for subscription not in IN_GRACE status | GraceExtensionServiceImpl:35 |
| `InvalidGraceExtensionDateException` | New grace date is not after current grace date | GraceExtensionServiceImpl:47 |
| `MaxExtensionDaysExceededException` | Extension exceeds 7 days limit | GraceExtensionServiceImpl:55 |
| `MaxExtensionLimitReachedException` | More than 2 extensions attempted for same subscription | GraceExtensionServiceImpl:61 |

---

## 2. SubscriptionExceptions
**Location**: `pro.sachin.fity.exception.SubscriptionExceptions`

**Service**: SubscriptionServiceImpl

| Exception | Thrown When | Line Reference |
|-----------|-------------|----------------|
| `ActiveSubscriptionAlreadyExistsException` | Member/Family already has active subscription | SubscriptionServiceImpl:146-149, 156-159 |
| `PendingSubscriptionAlreadyExistsException` | Member/Family already has pending (unpaid) subscription | SubscriptionServiceImpl:153-156, 163-166 |
| `InvalidSubscriptionEntityException` | Subscription has neither member nor family | SubscriptionServiceImpl:169-171 |
| `PendingRenewalAlreadyExistsException` | Renewal subscription already exists for member/family | SubscriptionServiceImpl:210-214, 217-221 |
| `InvalidRenewalStatusException` | Subscription status is not ACTIVE, IN_GRACE, or DUE | SubscriptionServiceImpl:200-203 |
| `ExcessiveBorrowedDaysException` | Borrowed days exceed acceptable limits (< 7 days effective duration) | SubscriptionServiceImpl:254-259 |

**Related Generic Exception**:
- `SubscriptionNotFoundException` - Moved to base exception folder

---

## 3. MemberExceptions
**Location**: `pro.sachin.fity.exception.MemberExceptions`

**Service**: MemberServiceImpl

| Exception | Thrown When | Line Reference |
|-----------|-------------|----------------|
| `MemberNotFoundException` | Member not found by ID (get, update) | MemberServiceImpl:77, 88 |
| `MemberRegistrationException` | Member registration fails due to database or business rule error | MemberServiceImpl:51 |
| `MemberAccessCreationException` | MemberAccess record creation fails during registration | Not explicitly used (wrapped in generic catch) |

**Implementation Status**: ✅ Updated to use custom exceptions

**Usage Examples**:

```java
// MemberNotFoundException - Used in getMemberById and updateMember
@Override
public MemberDTO getMemberById(Long id) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + id));
    return memberMapper.toDto(member);
}

// MemberRegistrationException - Used in registerMember
@Override
@Transactional
public void registerMember(MemberDTO memberDTO) {
    try {
        Member member = memberMapper.toEntity(memberDTO);
        member.setStatus(MemberStatus.ACTIVE);
        member = memberRepository.save(member);
        
        MemberAccess memberAccess = new MemberAccess();
        memberAccess.setMember(member);
        memberAccessRepository.save(memberAccess);
        
        deviceCommandService.queueMemberRegistration(member);
    } catch (DeviceCommandException e) {
        throw e; // Re-throw to trigger rollback
    } catch (Exception e) {
        throw new MemberRegistrationException("Failed to register member: " + e.getMessage());
    }
}
```

---

## 4. FamilyExceptions
**Location**: `pro.sachin.fity.exception.FamilyExceptions`

**Service**: FamilyServiceImpl

| Exception | Thrown When | Line Reference |
|-----------|-------------|----------------|
| `FamilyNotFoundException` | Family not found by ID | FamilyServiceImpl:40, 56, 70, 84, 95, 109 |
| `MemberNotFoundException` | Member not found while creating/managing family | FamilyServiceImpl:30, 58, 72 |
| `MemberAlreadyInFamilyException` | Member already belongs to another family | FamilyServiceImpl:62-64 |
| `MemberNotInFamilyException` | Member doesn't belong to specified family | FamilyServiceImpl:76-78 |
| `FamilyHasActiveMembersException` | Attempting to delete family with active members | FamilyServiceImpl:100-102 |

**Note**: Currently using `EntityNotFoundException` and `IllegalArgumentException`. Should be replaced.

---

## 5. PaymentExceptions
**Location**: `pro.sachin.fity.exception.PaymentExceptions`

**Service**: PaymentServiceImpl

| Exception | Thrown When | Line Reference |
|-----------|-------------|----------------|
| `SubscriptionNotFoundException` | Subscription not found for payment | PaymentServiceImpl:43, 73 |
| `SubscriptionChargesNotFoundException` | Subscription charges record not found | PaymentServiceImpl:101-102, 143-145 |

**Note**: Currently using `EntityNotFoundException` and `IllegalStateException`. Should be replaced.

---

## 6. PlanExceptions
**Location**: `pro.sachin.fity.exception.PlanExceptions`

**Service**: PlanServiceImpl

| Exception | Thrown When | Line Reference |
|-----------|-------------|----------------|
| `PlanNotFoundException` | Plan not found by ID (get, update) | PlanServiceImpl:31, 36 |

**Note**: Currently using `EntityNotFoundException`. Should be replaced.

---

## 7. MemberAccessExceptions
**Location**: `pro.sachin.fity.exception.MemberAccessExceptions`

**Service**: MemberAccessImpl

| Exception | Thrown When | Line Reference |
|-----------|-------------|----------------|
| `InvalidMemberAccessException` | Member ID is null | MemberAccessImpl:59-61 |
| `MemberNotFoundException` | Member not found for access update | MemberAccessImpl:63-64 |

**Note**: Currently using `IllegalArgumentException` and `EntityNotFoundException`. Should be replaced.

---

## 8. DeviceCommandExceptions
**Location**: `pro.sachin.fity.exception.DeviceCommandExceptions`

**Service**: DeviceCommandServiceImpl

| Exception | Thrown When | Line Reference |
|-----------|-------------|----------------|
| `DeviceCommandNotFoundException` | Command not found for acknowledgement | DeviceCommandServiceImpl:114 |
| `DeviceCommandException` | Failed to serialize command payload to JSON | DeviceCommandServiceImpl:134 |
| `PayloadSerializationException` | (Legacy - replaced by DeviceCommandException) | - |

**Implementation Status**: ✅ Updated to use custom exceptions

**Usage Examples**:

```java
// DeviceCommandNotFoundException - Used in acknowledgeCommand
@Override
public void acknowledgeCommand(Long commandId, boolean success) {
    DeviceCommand command = deviceCommandRepository.findById(commandId)
        .orElseThrow(() -> new DeviceCommandNotFoundException("Command not found with id " + commandId));
    
    command.setStatus(success ? CommandStatus.COMPLETED : CommandStatus.FAILED);
    deviceCommandRepository.save(command);
}

// DeviceCommandException - Used in toJson (private method)
private String toJson(Map<String, Object> map) {
    try {
        return objectMapper.writeValueAsString(map);
    } catch (JsonProcessingException e) {
        log.error("Failed to serialize payload to JSON", e);
        throw new DeviceCommandException("Failed to serialize command payload");
    }
}
```

**Note**: `DeviceCommandException` is also caught and re-thrown in `MemberServiceImpl.registerMember()` to trigger transaction rollback when device command queueing fails.

---

## Summary

### Exception Folders Created:
1. ✅ **GraceExtensionExceptions** (Already existed - 4 exceptions)
2. ✅ **SubscriptionExceptions** (6 exceptions)
3. ✅ **MemberExceptions** (3 exceptions - **Updated**)
4. ✅ **FamilyExceptions** (5 exceptions)
5. ✅ **PaymentExceptions** (1 exception + reuse SubscriptionNotFoundException)
6. ✅ **PlanExceptions** (1 exception)
7. ✅ **MemberAccessExceptions** (1 exception + reuse MemberNotFoundException)
8. ✅ **DeviceCommandExceptions** (3 exceptions - **Updated**)

### Total Custom Exceptions: 23 (Updated from 21)

### Shared Exceptions:
- `SubscriptionNotFoundException` - Used by Payment and Subscription services
- `MemberNotFoundException` - Used by Member, Family, and MemberAccess services

---

## Recent Updates (January 2025)

### Member Registration Refactoring

**What Changed:**
- Refactored `MemberServiceImpl.registerMember()` to use MapStruct mapper instead of manual field mapping
- Added `@Transactional` annotation for atomicity across multiple operations
- Implemented custom exception handling with `MemberRegistrationException` and `DeviceCommandException`
- Added automatic `MemberAccess` creation with `BLOCKED` status during registration
- Set default values: `status = ACTIVE`, `joinedDate = LocalDateTime.now()`
- Added comprehensive logging throughout the registration flow

**Benefits:**
- **Reduced code**: 40+ lines of manual setters reduced to ~15 lines with mapper
- **Atomicity**: Transaction rollback if any step fails (member, memberAccess, or device command)
- **Better error handling**: Specific exceptions instead of generic RuntimeException
- **Audit trail**: Detailed logging of each step in registration process
- **Data integrity**: MemberAccess is always created with member (no orphaned records)

**Transaction Flow:**
```
@Transactional
registerMember()
  ├─ Convert DTO to Entity (MemberMapper)
  ├─ Set defaults (status=ACTIVE, joinedDate=now)
  ├─ Save Member
  ├─ Create MemberAccess (status=BLOCKED)
  ├─ Save MemberAccess
  ├─ Queue Device Command
  └─ Commit (or Rollback on error)
```

**Exception Handling Pattern:**
```java
try {
    // Business operations
} catch (DeviceCommandException e) {
    log.error("Device command failed", e);
    throw e; // Re-throw to trigger rollback
} catch (Exception e) {
    log.error("Registration failed", e);
    throw new MemberRegistrationException("Failed to register member");
}
```

**Files Modified:**
- `MemberServiceImpl.java` - Refactored registerMember, getMemberById, updateMember
- `DeviceCommandServiceImpl.java` - Updated toJson to use DeviceCommandException
- Created `MemberRegistrationException.java`
- Created `MemberAccessCreationException.java`
- Created `DeviceCommandException.java`

---

## Next Steps (Recommended)

### Phase 1: Update Service Implementations
Replace all generic exceptions with custom exceptions:

1. ✅ **MemberServiceImpl**: Updated - Uses `MemberNotFoundException` and `MemberRegistrationException`
2. ✅ **DeviceCommandServiceImpl**: Updated - Uses `DeviceCommandException`
3. **SubscriptionServiceImpl**: Replace `EntityNotFoundException` and `IllegalStateException`
4. **FamilyServiceImpl**: Replace `EntityNotFoundException` and `IllegalArgumentException`
5. **PaymentServiceImpl**: Replace `EntityNotFoundException` and `IllegalStateException`
6. **PlanServiceImpl**: Replace `EntityNotFoundException`
7. **MemberAccessImpl**: Replace `IllegalArgumentException` and `EntityNotFoundException`

**Progress: 2/7 services updated (29%)**

### Phase 2: Global Exception Handler
Create `@ControllerAdvice` class to handle all custom exceptions globally with proper HTTP status codes.

### Phase 3: Testing
Update test cases to expect custom exceptions instead of generic ones.

---

## Pattern Followed

All custom exceptions follow the same pattern as `GraceExtensionExceptions`:

```java
package pro.sachin.fity.exception.[ServiceName]Exceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class [ExceptionName] extends BusinessExceptionHandler {

    public [ExceptionName](String message) {
        super(message);
    }
}
```

This ensures consistency across the entire exception hierarchy.
