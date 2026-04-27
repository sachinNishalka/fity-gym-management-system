# Fity Gym Management System - Coding Style Guide

This document outlines the coding standards, patterns, and best practices used throughout the Fity application. All developers must follow these guidelines to maintain consistency and code quality.

---

## Table of Contents

1. [Exception Handling Patterns](#1-exception-handling-patterns)
2. [Mapper Usage Guidelines](#2-mapper-usage-guidelines)
3. [Transaction Management](#3-transaction-management)
4. [Service Layer Patterns](#4-service-layer-patterns)
5. [Entity Design Patterns](#5-entity-design-patterns)
6. [Repository Layer](#6-repository-layer)
7. [DTO Patterns](#7-dto-patterns)
8. [Logging Standards](#8-logging-standards)

---

## 1. Exception Handling Patterns

### 1.1 When to Create Custom Exceptions

**Create custom exceptions when:**
- You need domain-specific error handling
- The exception represents a business rule violation
- You want to provide meaningful error messages to consumers
- Generic exceptions don't convey enough context

**Use generic exceptions when:**
- The error is truly unexpected (programming errors)
- Re-throwing framework exceptions (e.g., database connection failures)

### 1.2 Exception Naming Conventions

- Use descriptive names that indicate the error condition
- End exception class names with `Exception`
- Be specific: `MemberNotFoundException` > `NotFoundException`

**Examples:**
```java
MemberNotFoundException
MemberRegistrationException
MemberAccessCreationException
DeviceCommandException
SubscriptionNotFoundException
```

### 1.3 Package Structure for Exceptions

Organize exceptions in service-specific folders under the main exception package:

```
pro.sachin.fity.exception/
├── BusinessExceptionHandler.java (base class)
├── MemberExceptions/
│   ├── MemberNotFoundException.java
│   ├── MemberRegistrationException.java
│   └── MemberAccessCreationException.java
├── SubscriptionExceptions/
│   ├── SubscriptionNotFoundException.java
│   └── OverlappingSubscriptionException.java
├── PaymentExceptions/
│   └── PaymentNotFoundException.java
└── DeviceCommandExceptions/
    └── DeviceCommandException.java
```

### 1.4 How to Extend BusinessExceptionHandler

All custom business exceptions must extend `BusinessExceptionHandler`:

```java
package pro.sachin.fity.exception.MemberExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class MemberRegistrationException extends BusinessExceptionHandler {

    public MemberRegistrationException(String message) {
        super(message);
    }
}
```

**Base Class:**
```java
public abstract class BusinessExceptionHandler extends RuntimeException {

    public BusinessExceptionHandler(String message) {
        super(message);
    }
}
```

### 1.5 Exception Usage in Services

**DO:**
```java
@Override
public MemberDTO getMemberById(Long id) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + id));
    return memberMapper.toDto(member);
}
```

**DON'T:**
```java
@Override
public MemberDTO getMemberById(Long id) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Member not found")); // Generic exception
    return memberMapper.toDto(member);
}
```

### 1.6 Exception Handling with Transactions

When using `@Transactional`, exceptions trigger rollback:

```java
@Override
@Transactional
public void registerMember(MemberDTO memberDTO) {
    try {
        Member member = memberMapper.toEntity(memberDTO);
        member = memberRepository.save(member);
        
        // More operations...
        
        deviceCommandService.queueMemberRegistration(member);
        
    } catch (DeviceCommandException e) {
        log.error("Failed to queue device command", e);
        throw e; // Re-throw to trigger transaction rollback
    } catch (Exception e) {
        log.error("Failed to register member", e);
        throw new MemberRegistrationException("Failed to register member: " + e.getMessage());
    }
}
```

---

## 2. Mapper Usage Guidelines

### 2.1 When to Use MapStruct Mappers

**ALWAYS use mappers for:**
- DTO ↔ Entity conversions
- Reducing boilerplate code
- Ensuring consistency across conversions
- Complex object transformations

**Manual mapping is acceptable for:**
- Setting single override values after mapper conversion
- Very simple DTOs with 1-2 fields
- Performance-critical hot paths (rare)

### 2.2 Configuring MapStruct

All mappers must use Spring component model:

```java
@Mapper(componentModel = "spring")
public interface MemberMapper {
    
    MemberDTO toDto(Member member);
    
    Member toEntity(MemberDTO memberDTO);
    
    Member updateMemberFromDto(MemberDTO memberDTO, @MappingTarget Member member);
}
```

**Key Configuration:**
- `componentModel = "spring"` - Enables Spring dependency injection
- MapStruct generates implementations at compile time
- No manual implementation needed

### 2.3 Mapper Interface Patterns

**Standard Mapping Methods:**

```java
@Mapper(componentModel = "spring")
public interface EntityMapper<D, E> {
    
    // DTO to Entity
    E toEntity(D dto);
    
    // Entity to DTO
    D toDto(E entity);
    
    // Update existing entity from DTO (for PATCH/PUT operations)
    E updateEntityFromDto(D dto, @MappingTarget E entity);
    
    // List conversions
    List<D> toDtoList(List<E> entities);
    List<E> toEntityList(List<D> dtos);
}
```

### 2.4 Mapper Usage Examples

**Registration Flow (NEW entity):**
```java
@Override
@Transactional
public void registerMember(MemberDTO memberDTO) {
    // Use mapper to convert DTO to entity
    Member member = memberMapper.toEntity(memberDTO);
    
    // Override critical default values after mapping
    member.setStatus(MemberStatus.ACTIVE);
    member.setJoinedDate(LocalDateTime.now());
    
    member = memberRepository.save(member);
}
```

**Update Flow (EXISTING entity):**
```java
@Override
public Member updateMember(Long memberId, MemberDTO memberDTO) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new MemberNotFoundException("Member not found"));
    
    // Use @MappingTarget to update existing entity
    Member updatedMember = memberMapper.updateMemberFromDto(memberDTO, member);
    
    return memberRepository.save(updatedMember);
}
```

**Retrieval Flow (Entity to DTO):**
```java
@Override
public MemberDTO getMemberById(Long id) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new MemberNotFoundException("Member not found"));
    
    // Convert entity to DTO for response
    return memberMapper.toDto(member);
}
```

### 2.5 Best Practices

✅ **DO:**
- Use mappers for all DTO ↔ Entity conversions
- Override specific fields after mapping when needed for business rules
- Keep mapper interfaces simple and focused

❌ **DON'T:**
- Manually copy fields when a mapper exists
- Mix manual mapping and mapper usage for the same object
- Create new mapper instances manually (Spring will inject them)

---

## 3. Transaction Management

### 3.1 When to Use @Transactional

**Use `@Transactional` when:**
- Multiple database operations must succeed or fail together
- You need atomicity (all-or-nothing behavior)
- Operations span multiple repositories
- Business logic requires rollback on failure

**Examples requiring transactions:**
- Member registration (member + memberAccess + device command)
- Payment processing (payment + subscription status + member access)
- Subscription creation (subscription + charges + member access)
- Family management (family + multiple members)

### 3.2 Transaction Annotation Placement

**Place @Transactional on:**
- Service layer methods (preferred)
- Public methods only (Spring AOP limitation)
- Methods that orchestrate multiple operations

```java
@Slf4j
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

    @Override
    @Transactional  // ✅ Correct placement
    public void registerMember(MemberDTO memberDTO) {
        // Multiple database operations
    }
}
```

### 3.3 Transaction Isolation Levels

**Default isolation:** `READ_COMMITTED` (Spring default)

**Specify isolation when needed:**
```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public void criticalFinancialOperation() {
    // Operations requiring highest isolation
}
```

**Isolation levels:**
- `READ_UNCOMMITTED` - Lowest isolation, highest performance (rarely used)
- `READ_COMMITTED` - Default, prevents dirty reads
- `REPEATABLE_READ` - Prevents non-repeatable reads
- `SERIALIZABLE` - Highest isolation, prevents phantom reads

### 3.4 Read-Only Transactions

Use `readOnly = true` for query-only operations:

```java
@Transactional(readOnly = true)
public List<MemberDTO> getAllMembers() {
    List<Member> members = memberRepository.findAll();
    return members.stream()
        .map(memberMapper::toDto)
        .collect(Collectors.toList());
}
```

**Benefits:**
- Performance optimization hint to database
- Prevents accidental writes
- Can enable query optimizations

### 3.5 Rollback Behavior

**Automatic rollback on:**
- Unchecked exceptions (RuntimeException and subclasses)
- Errors (including OutOfMemoryError, etc.)

**No rollback on:**
- Checked exceptions (unless explicitly configured)

**Example with explicit rollback control:**
```java
@Transactional
public void registerMember(MemberDTO memberDTO) {
    try {
        Member member = memberMapper.toEntity(memberDTO);
        member = memberRepository.save(member);
        
        MemberAccess memberAccess = new MemberAccess();
        memberAccess.setMember(member);
        memberAccessRepository.save(memberAccess);
        
        deviceCommandService.queueMemberRegistration(member);
        
    } catch (DeviceCommandException e) {
        log.error("Device command failed", e);
        throw e; // Re-throw to trigger rollback
    } catch (Exception e) {
        log.error("Registration failed", e);
        throw new MemberRegistrationException("Failed: " + e.getMessage());
    }
}
```

### 3.6 Transaction Examples from Existing Services

**SubscriptionServiceImpl:**
```java
@Transactional
@Override
public void saveSubscription(SubscriptionDTO subscriptionDTO) {
    // 1. Validate and create subscription
    Subscription subscription = new Subscription();
    // ... set fields
    
    // 2. Save subscription
    Subscription savedSubscription = subscriptionRepository.save(subscription);
    
    // 3. Create charges (dependent operation)
    createSubscriptionCharges(savedSubscription, subscriptionDTO.getDiscountAmount());
    
    // 4. Update member access (dependent operation)
    memberAccessService.updateMemberAccess(
        savedSubscription.getMember().getId(),
        graceEndDate,
        AccessStatus.BLOCKED,
        "New subscription created!"
    );
    
    // All operations succeed together or rollback completely
}
```

### 3.7 Best Practices

✅ **DO:**
- Keep transactions as short as possible
- Place @Transactional on service methods
- Let unchecked exceptions trigger rollback
- Use readOnly=true for queries
- Log before throwing exceptions in transactions

❌ **DON'T:**
- Call @Transactional methods from within the same class (Spring AOP limitation)
- Catch exceptions without re-throwing (prevents rollback)
- Perform long-running operations inside transactions
- Use transactions for single-operation methods (unnecessary overhead)

---

## 4. Service Layer Patterns

### 4.1 Dependency Injection with @RequiredArgsConstructor

**Standard pattern:**
```java
@Slf4j
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberAccessRepository memberAccessRepository;
    private final DeviceCommandService deviceCommandService;
    private final MemberMapper memberMapper;
    
    // No constructor needed - Lombok generates it
}
```

**Why this pattern:**
- Lombok `@RequiredArgsConstructor` generates constructor for `final` fields
- Constructor injection is preferred over field injection
- Immutable dependencies (thread-safe)
- Enables easier testing (can mock dependencies)

### 4.2 Service Interface vs Implementation

**Always define interfaces for services:**

**Interface:**
```java
public interface MemberService {
    void registerMember(MemberDTO memberDTO);
    MemberDTO getMemberById(Long id);
    List<MemberDTO> getAllMembers();
    Member updateMember(Long memberId, MemberDTO memberDTO);
    void deleteMember(Long memberId);
}
```

**Implementation:**
```java
@Slf4j
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {
    // Implementation
}
```

**Benefits:**
- Loose coupling
- Easy to swap implementations
- Better testability (can create test doubles)
- Clear contract definition

### 4.3 Method Naming Conventions

**Follow these patterns:**

| Operation | Method Name Pattern | Example |
|-----------|-------------------|---------|
| Create | `save*`, `create*`, `register*` | `registerMember()` |
| Read Single | `get*ById()`, `find*By*()` | `getMemberById()` |
| Read Multiple | `getAll*()`, `find*By*()` | `getAllMembers()` |
| Update | `update*()` | `updateMember()` |
| Delete | `delete*()`, `remove*()` | `deleteMember()` |
| Business Logic | Descriptive verb | `queueMemberRegistration()` |

### 4.4 Return Types

**Use appropriate return types:**

```java
// Void for operations without return value
public void registerMember(MemberDTO memberDTO) { }

// Entity for internal operations
public Member updateMember(Long id, MemberDTO dto) { }

// DTO for external responses
public MemberDTO getMemberById(Long id) { }

// List<DTO> for collections
public List<MemberDTO> getAllMembers() { }

// ResponseEntity for REST responses (in controllers, not services)
public ResponseEntity<MemberDTO> getMember(Long id) { }
```

### 4.5 Service Method Structure

**Recommended structure:**

```java
@Override
@Transactional
public void businessOperation(InputDTO dto) {
    // 1. Validation & Business Rule Checks
    validateInput(dto);
    checkBusinessRules(dto);
    
    // 2. Fetch Required Entities
    Entity entity = repository.findById(dto.getId())
        .orElseThrow(() -> new EntityNotFoundException("Not found"));
    
    // 3. Transform Data (using mappers)
    Entity updatedEntity = mapper.toEntity(dto);
    
    // 4. Apply Business Logic
    updatedEntity.setStatus(Status.ACTIVE);
    updatedEntity.setProcessedDate(LocalDateTime.now());
    
    // 5. Persist Changes
    Entity saved = repository.save(updatedEntity);
    
    // 6. Trigger Side Effects
    notificationService.notify(saved);
    
    // 7. Log Success
    log.info("Operation completed for entity: {}", saved.getId());
}
```

---

## 5. Entity Design Patterns

### 5.1 Relationship Annotations

**One-to-One:**
```java
@Entity
@Data
public class MemberAccess {
    
    @Id
    @Column(name = "member_id")
    private Long memberId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id")
    private Member member;
}
```

**One-to-Many:**
```java
@Entity
@Data
public class Family {
    
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Member> members = new ArrayList<>();
}
```

**Many-to-One:**
```java
@Entity
@Data
public class Member {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;
}
```

### 5.2 Fetch Strategies

**Default fetch types:**
- `@OneToOne`: EAGER
- `@ManyToOne`: EAGER
- `@OneToMany`: LAZY
- `@ManyToMany`: LAZY

**Best practice: Explicitly specify LAZY:**
```java
@OneToOne(fetch = FetchType.LAZY)
@MapsId
@JoinColumn(name = "member_id")
private Member member;
```

**Why LAZY:**
- Prevents N+1 query problems
- Better performance
- Load related data only when needed
- Use `@EntityGraph` for eager loading when required

### 5.3 Cascade Operations

**Common cascade types:**

```java
// Cascade all operations
@OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
private List<Payments> payments;

// Specific cascade operations
@OneToMany(mappedBy = "family", 
    cascade = {CascadeType.PERSIST, CascadeType.MERGE})
private List<Member> members;

// No cascade (default)
@ManyToOne
@JoinColumn(name = "plan_id")
private Plan plan;
```

**Cascade types:**
- `PERSIST` - Save operation cascades
- `MERGE` - Update operation cascades
- `REMOVE` - Delete operation cascades
- `REFRESH` - Refresh operation cascades
- `DETACH` - Detach operation cascades
- `ALL` - All operations cascade

### 5.4 ID Generation Strategies

**Auto-increment (MySQL):**
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

**Sequence (PostgreSQL, Oracle):**
```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
@SequenceGenerator(name = "member_seq", sequenceName = "member_sequence")
private Long id;
```

**Shared primary key (One-to-One):**
```java
@Id
@Column(name = "member_id")
private Long memberId;

@OneToOne(fetch = FetchType.LAZY)
@MapsId
@JoinColumn(name = "member_id")
private Member member;
```

### 5.5 Audit Fields

**Use Hibernate annotations:**

```java
@Entity
@Data
public class Member {
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

**Or use JPA auditing:**
```java
@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class Member {
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

---

## 6. Repository Layer

### 6.1 JPA Repository Conventions

**Extend JpaRepository:**
```java
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    // Custom query methods follow naming conventions
    Optional<Member> findByMemberCode(String memberCode);
    
    List<Member> findByStatus(MemberStatus status);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT m FROM Member m WHERE m.status = :status AND m.joinedDate >= :date")
    List<Member> findActiveMembers(@Param("status") MemberStatus status, 
                                    @Param("date") LocalDateTime date);
}
```

### 6.2 Query Method Naming Patterns

**Standard patterns:**

| Pattern | Example | SQL Equivalent |
|---------|---------|----------------|
| `findBy*` | `findByEmail(String email)` | `WHERE email = ?` |
| `findBy*And*` | `findByFirstNameAndLastName()` | `WHERE first_name = ? AND last_name = ?` |
| `findBy*Or*` | `findByEmailOrPhoneNumber()` | `WHERE email = ? OR phone_number = ?` |
| `existsBy*` | `existsByMemberCode()` | `SELECT EXISTS(WHERE...)` |
| `countBy*` | `countByStatus()` | `SELECT COUNT(*) WHERE...` |
| `deleteBy*` | `deleteByStatus()` | `DELETE WHERE status = ?` |

### 6.3 Custom Query Methods

**Using @Query:**
```java
@Query("SELECT s FROM Subscription s WHERE s.member.id = :memberId AND s.status = :status")
Subscription findByMemberIdAndStatus(@Param("memberId") Long memberId, 
                                      @Param("status") SubscriptionStatus status);

@Query(value = "SELECT * FROM subscriptions WHERE grace_end_date < CURRENT_DATE", 
       nativeQuery = true)
List<Subscription> findExpiredSubscriptions();
```

---

## 7. DTO Patterns

### 7.1 DTO Naming Conventions

**Request DTOs:**
```java
MemberDTO           // General purpose
CreateMemberDTO     // Specific to creation
UpdateMemberDTO     // Specific to updates
```

**Response DTOs:**
```java
MemberResponseDTO
MemberDetailsDTO
MemberSummaryDTO
```

### 7.2 What Should Be in a DTO vs Entity

**DTO contains:**
- Data for transfer (no business logic)
- Validation annotations
- Simplified relationships (IDs instead of objects)
- Formatted/computed fields for display

**Entity contains:**
- Persistence mapping
- Relationships
- Business logic methods
- Database constraints

**Example:**

**DTO:**
```java
@Data
public class MemberDTO {
    private Long id;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private Long familyId;  // Just the ID
    private String memberAccessStatus;  // Computed field
}
```

**Entity:**
```java
@Entity
@Data
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(unique = true)
    private String email;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;  // Full object
    
    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY)
    private MemberAccess memberAccess;  // Full object
}
```

---

## 8. Logging Standards

### 8.1 Using @Slf4j

**Add Lombok annotation:**
```java
@Slf4j
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {
    
    public void registerMember(MemberDTO memberDTO) {
        log.info("Registering member: {}", memberDTO.getMemberCode());
        // ... logic
        log.debug("Member details: {}", member);
    }
}
```

### 8.2 Log Levels

**When to use each level:**

| Level | Usage | Example |
|-------|-------|---------|
| `ERROR` | System errors, exceptions | `log.error("Failed to save member", exception)` |
| `WARN` | Unexpected but recoverable | `log.warn("Member {} has no active subscription", id)` |
| `INFO` | Important business events | `log.info("Member registered: {}", memberCode)` |
| `DEBUG` | Detailed diagnostic info | `log.debug("Fetching member with id: {}", id)` |
| `TRACE` | Very detailed diagnostic | `log.trace("Query params: {}", params)` |

### 8.3 What to Log and When

**DO log:**
- Business operation start/completion
- Important state changes
- Exception details
- External system calls
- Performance metrics (if needed)

**Examples:**
```java
// Operation start
log.info("Starting member registration for code: {}", memberDTO.getMemberCode());

// Success
log.info("Member registered successfully with ID: {}", member.getId());

// State change
log.info("Member access changed from {} to {}", oldStatus, newStatus);

// Exception with context
log.error("Failed to register member: {}", memberDTO.getMemberCode(), exception);

// External call
log.info("Device command queued for member: {}", memberCode);
```

### 8.4 Structured Logging Examples

**Good logging:**
```java
log.info("Member registered successfully with ID: {} and code: {}", 
    member.getId(), member.getMemberCode());

log.error("Failed to queue device command for member {}", 
    member.getMemberCode(), exception);
```

**Bad logging:**
```java
log.info("Member registered");  // Not enough context

log.info("Member: " + member.toString());  // String concatenation

System.out.println("Debug info");  // Don't use System.out
```

### 8.5 Logging Best Practices

✅ **DO:**
- Use parameterized logging: `log.info("User: {}", username)`
- Include relevant context (IDs, codes, statuses)
- Log exceptions with full stack traces
- Use appropriate log levels
- Log business events, not every line of code

❌ **DON'T:**
- Use string concatenation: `log.info("User: " + username)`
- Log sensitive data (passwords, tokens, full credit card numbers)
- Log inside loops (can flood logs)
- Use System.out.println()
- Log too verbosely in production

---

## Summary

This coding style guide provides the foundation for consistent, maintainable code in the Fity application. Key takeaways:

1. **Always use custom exceptions** extending BusinessExceptionHandler
2. **Use MapStruct mappers** for all DTO ↔ Entity conversions
3. **Apply @Transactional** for multi-operation atomicity
4. **Follow service layer patterns** with interface/implementation separation
5. **Design entities carefully** with proper relationships and fetch strategies
6. **Leverage JPA naming conventions** for repository methods
7. **Use DTOs appropriately** for data transfer, not persistence
8. **Log meaningfully** with appropriate levels and context

When in doubt, refer to existing implementations in:
- `MemberServiceImpl` - Demonstrates mapper usage, transactions, exception handling
- `SubscriptionServiceImpl` - Shows complex transaction management
- `DeviceCommandServiceImpl` - Example of custom exception usage

**Remember:** Consistency is more important than perfection. Follow these patterns to maintain a clean, understandable codebase.
