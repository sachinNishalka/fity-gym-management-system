# Gym Management System - Implementation Guide

## 📌 Overview
This guide outlines what needs to be implemented to complete the gym management system. It's organized by priority and includes detailed implementation notes.

---

## 🔴 CRITICAL PRIORITY - Core Business Logic

### 1. Fix Subscription Enrollment Flow

**Current Problem:** 
- Manual date calculation by client
- Separate endpoints for subscription, charges, and payment
- No automatic charge creation
- Missing post-enrollment actions

**What to Implement:**

#### 1.1 Create Unified Enrollment Service Method
```java
// In SubscriptionService
SubscriptionEnrollmentResponse enrollInPlan(SubscriptionEnrollmentRequest request)
```

**This method should:**
1. Validate member/family and plan exist
2. Check no overlapping active subscriptions
3. Validate plan type eligibility (age for KIDS, member count for FAMILY)
4. **AUTO-CALCULATE dates:**
   - `endDate = startDate + plan.durationDays`
   - `dueDate = endDate` (or `endDate - 7 days` if you want early reminder)
   - `graceEndDate = endDate + 7 days`
5. Set `status = ACTIVE`
6. Create Subscription entity
7. **AUTO-CREATE SubscriptionCharges:**
   - `totalAmount = plan.price`
   - `netAmount = totalAmount - discountAmount`
8. If `initialPaymentAmount > 0`, create Payment record
9. Update MemberAccess based on payment status
10. Schedule notification reminder (due date - 7 days)
11. Return complete enrollment summary

**Use @Transactional to ensure all steps succeed or rollback together**

---

### 2. Implement Payment Post-Processing Logic

**Current Problem:**
- Payments are saved but nothing happens after
- Member access is not updated
- Subscription status is not changed

**What to Implement:**

#### 2.1 Add Post-Payment Business Logic
```java
// In PaymentService
void updateMemberAccessAfterPayment(Long subscriptionId)
```

**This method should:**
1. Calculate balance due: `netAmount - SUM(payments.amount)`
2. If balance <= 0 (fully paid):
   - Update `member_access.access_status = 'ALLOWED'`
   - If subscription status was 'BLOCKED', change to 'ACTIVE'
   - Update `member_access.allowed_until = subscription.grace_end_date`
   - Cancel pending reminder notifications
   - Send payment confirmation notification
3. If partial payment:
   - Keep tracking balance
   - Don't change access status yet

---

### 3. Fix Data Types

**Critical Issues:**

#### 3.1 Change ID Types
- **Current:** `int Id`
- **Should be:** `Long id`
- **Reason:** SQL uses BIGINT, Java int limits to 2.1 billion records
- **Apply to:** All entities (Member, Plan, Subscription, etc.)

#### 3.2 Change Money Types
- **Current:** `int` or `double` for amounts
- **Should be:** `BigDecimal`
- **Reason:** Accurate decimal arithmetic for money
- **Apply to:** Plan.price, SubscriptionCharges amounts, Payment.amount

#### 3.3 Change Timestamp Types
- **Current:** `LocalDate` for timestamps
- **Should be:** `LocalDateTime` for timestamps
- **Reason:** SQL uses TIMESTAMP (includes time), not just DATE
- **Apply to:** Payments.paidOn, createdAt fields

---

## 🟡 HIGH PRIORITY - Missing Core Entities

### 4. Create MemberAccess Entity (CRITICAL for Door System)

**SQL Reference:**
```sql
CREATE TABLE member_access (
    member_id BIGINT PRIMARY KEY REFERENCES members(member_id),
    access_status VARCHAR(10) NOT NULL,  -- 'allowed' or 'blocked'
    allowed_until DATE,
    reason VARCHAR(30),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by_user_id BIGINT REFERENCES users(user_id)
);
```

**What to Implement:**
```java
@Entity
public class MemberAccess {
    @Id
    private Long memberId;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "member_id")
    private Member member;
    
    @Enumerated(EnumType.STRING)
    private AccessStatus accessStatus; // ALLOWED, BLOCKED
    
    private LocalDate allowedUntil;
    private String reason;
    private LocalDateTime updatedAt;
    
    @ManyToOne
    @JoinColumn(name = "updated_by_user_id")
    private User updatedBy;
}
```

**When to Update:**
- Member registration: Create with status = BLOCKED
- After enrollment + full payment: Change to ALLOWED
- After subscription expires (past grace period): Change to BLOCKED
- After partial payment: Keep as BLOCKED
- After full payment on blocked subscription: Change to ALLOWED
- Manual override by admin: Change with reason

**Create Endpoint:**
```java
// For door system to check
GET /api/v1/access/check/{memberId}
// Returns: { "allowed": true/false, "reason": "..." }
```

---

### 5. Create Users Entity (Staff Management)

**SQL Reference:**
```sql
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(200) NOT NULL,
    role VARCHAR(30) NOT NULL,  -- 'coach', 'admin', 'reception'
    active BOOLEAN NOT NULL DEFAULT TRUE
);
```

**What to Implement:**
```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Enumerated(EnumType.STRING)
    private UserRole role; // COACH, ADMIN, RECEPTION
    
    private Boolean active = true;
    
    // TODO: Add authentication fields (username, password, etc.)
}
```

**Why Needed:**
- Track who created subscriptions (`createdByUserId`)
- Track who received payments (`receivedByUserId`)
- Track who extended grace periods (`extendedByUserId`)
- Track who updated member access (`updatedByUserId`)
- Accountability and audit trail

---

### 6. Create Attendance Entity

**SQL Reference:**
```sql
CREATE TABLE attendance (
    attendance_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    member_id BIGINT NOT NULL REFERENCES members(member_id),
    check_in_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    captured_by VARCHAR(20) NOT NULL DEFAULT 'door',  -- 'door' or 'manual'
    subscription_id BIGINT REFERENCES subscriptions(subscription_id)
);
```

**What to Implement:**
```java
@Entity
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Column(nullable = false)
    private LocalDateTime checkInTime = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    private CaptureMethod capturedBy; // DOOR, MANUAL
    
    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;
}
```

**Create Endpoints:**
```java
POST /api/v1/attendance/checkin
// Request: { "memberId": 123 }
// Logic:
//   1. Check member_access.access_status
//   2. If ALLOWED → create attendance record, return success (door opens)
//   3. If BLOCKED → return error with reason (door stays closed)

GET /api/v1/attendance/member/{memberId}?from=2026-01-01&to=2026-01-31
// Return attendance history for member

GET /api/v1/attendance/falling-behind
// SQL from snippets.sql line 207-213
// Return members who haven't visited in 7+ days
```

---

### 7. Create GraceExtension Entity

**SQL Reference:**
```sql
CREATE TABLE grace_extensions (
    extension_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    subscription_id BIGINT NOT NULL REFERENCES subscriptions(subscription_id),
    extended_by_user_id BIGINT NOT NULL REFERENCES users(user_id),
    old_grace_end_date DATE NOT NULL,
    new_grace_end_date DATE NOT NULL,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**What to Implement:**
```java
@Entity
public class GraceExtension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;
    
    @ManyToOne
    @JoinColumn(name = "extended_by_user_id", nullable = false)
    private User extendedBy;
    
    private LocalDate oldGraceEndDate;
    private LocalDate newGraceEndDate;
    private String reason;
    private LocalDateTime createdAt = LocalDateTime.now();
}
```

**Create Endpoint:**
```java
POST /api/v1/subscription/{id}/extend-grace
// Request: { "newGraceEndDate": "2026-02-01", "reason": "Medical emergency" }
// Logic:
//   1. Save old grace_end_date
//   2. Update subscription.grace_end_date
//   3. Create GraceExtension record (audit trail)
//   4. If subscription was BLOCKED, change to IN_GRACE
//   5. Update member_access if needed
// Security: Only COACH and ADMIN roles can extend
```

---

### 8. Create NotificationQueue Entity

**SQL Reference:**
```sql
CREATE TABLE notification_queue (
    notification_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    member_id BIGINT NOT NULL REFERENCES members(member_id),
    subscription_id BIGINT NOT NULL REFERENCES subscriptions(subscription_id),
    notification_type VARCHAR(30) NOT NULL,  -- 'renewal_reminder', 'grace_warning', 'blocked_notice'
    scheduled_for TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    channel VARCHAR(20) NOT NULL DEFAULT 'sms'
);
```

**What to Implement:**
```java
@Entity
public class NotificationQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;
    
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType; 
    // RENEWAL_REMINDER, GRACE_WARNING, BLOCKED_NOTICE, PAYMENT_CONFIRMATION
    
    private LocalDateTime scheduledFor;
    private LocalDateTime sentAt;
    
    @Enumerated(EnumType.STRING)
    private NotificationStatus status; // PENDING, SENT, FAILED, CANCELLED
    
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel; // SMS, EMAIL, BOTH
}
```

**When to Create Notifications:**
- **During enrollment:** Schedule renewal reminder for (due_date - 7 days)
- **When entering grace period:** Send grace warning
- **When blocked:** Send blocked notice
- **After payment:** Send payment confirmation

**Create Background Job:**
```java
@Scheduled(fixedRate = 300000) // Every 5 minutes
public void sendPendingNotifications() {
    // Find notifications where:
    //   - status = PENDING
    //   - scheduled_for <= now
    // Send them via SMS/email
    // Update status to SENT or FAILED
}
```

---

### 9. Create FamilyMember Junction Entity

**SQL Reference:**
```sql
CREATE TABLE family_members (
    family_id BIGINT NOT NULL REFERENCES families(family_id),
    member_id BIGINT NOT NULL REFERENCES members(member_id),
    role VARCHAR(20) NOT NULL DEFAULT 'member',  -- 'primary', 'spouse', 'child', 'member'
    PRIMARY KEY (family_id, member_id)
);
```

**What to Implement:**
```java
@Entity
@Table(name = "family_members")
@IdClass(FamilyMemberId.class)
public class FamilyMember {
    @Id
    @Column(name = "family_id")
    private Long familyId;
    
    @Id
    @Column(name = "member_id")
    private Long memberId;
    
    @ManyToOne
    @MapsId("familyId")
    @JoinColumn(name = "family_id")
    private Family family;
    
    @ManyToOne
    @MapsId("memberId")
    @JoinColumn(name = "member_id")
    private Member member;
    
    @Enumerated(EnumType.STRING)
    private FamilyRole role; // PRIMARY, SPOUSE, CHILD, MEMBER
}

// Composite key class
@Embeddable
public class FamilyMemberId implements Serializable {
    private Long familyId;
    private Long memberId;
    // equals() and hashCode()
}
```

**Update Family Entity:**
```java
@Entity
public class Family {
    // ... existing fields ...
    
    @OneToMany(mappedBy = "family")
    private List<FamilyMember> familyMembers = new ArrayList<>();
    
    // Helper method
    public int getMemberCount() {
        return familyMembers.size();
    }
}
```

---

## 🟠 MEDIUM PRIORITY - Business Logic Features

### 10. Implement Subscription Renewal

**What to Implement:**
```java
POST /api/v1/subscription/{id}/renew
// Request: { 
//   "planId": 2,  // optional, can change plan during renewal
//   "discountAmount": 0,
//   "initialPaymentAmount": 5000
// }
// Logic:
//   1. Get old subscription
//   2. Create new subscription with:
//      - Same member/family
//      - New plan (or same plan)
//      - startDate = MAX(oldSubscription.endDate + 1, today)
//      - Calculate all other dates
//   3. Create charges automatically
//   4. If payment provided, record it
//   5. Update member access if fully paid
```

---

### 11. Implement Automated Status Updates (Background Jobs)

**What to Implement:**

#### 11.1 Daily Status Update Job
```java
@Scheduled(cron = "0 0 2 * * *") // Run at 2 AM daily
public void updateSubscriptionStatuses() {
    LocalDate today = LocalDate.now();
    
    // 1. Find subscriptions to move to IN_GRACE
    List<Subscription> toGrace = subscriptionRepository
        .findByEndDateBeforeAndStatus(today, SubscriptionStatus.ACTIVE);
    toGrace.forEach(sub -> sub.setStatus(SubscriptionStatus.IN_GRACE));
    subscriptionRepository.saveAll(toGrace);
    
    // 2. Find subscriptions to BLOCK
    List<Subscription> toBlock = subscriptionRepository
        .findByGraceEndDateBeforeAndStatus(today, SubscriptionStatus.IN_GRACE);
    toBlock.forEach(sub -> {
        sub.setStatus(SubscriptionStatus.BLOCKED);
        // Update member_access
        memberAccessService.blockMember(sub.getMemberId(), "Subscription expired");
    });
    subscriptionRepository.saveAll(toBlock);
    
    // 3. Send notifications for subscriptions expiring in 7 days
    LocalDate sevenDaysFromNow = today.plusDays(7);
    List<Subscription> expiringSoon = subscriptionRepository
        .findByDueDateBetween(today, sevenDaysFromNow);
    expiringSoon.forEach(sub -> {
        notificationService.scheduleRenewalReminder(sub);
    });
}
```

---

### 12. Implement Monitoring Endpoints

**Based on SQL queries in snippets.sql:**

#### 12.1 Get Falling Behind Members
```java
GET /api/v1/reports/falling-behind
// SQL from line 182-196 in snippets.sql
// Return members in grace period with outstanding balance
```

#### 12.2 Get Blocked Members
```java
GET /api/v1/reports/blocked
// SQL from line 200-203 in snippets.sql
// Return members past grace period
```

#### 12.3 Get Members Who Haven't Visited
```java
GET /api/v1/reports/no-visits?days=7
// SQL from line 207-213 in snippets.sql
// Return members with no attendance in X days
```

#### 12.4 Get Balance Due Report
```java
GET /api/v1/reports/balance-due
// SQL from line 162-170 in snippets.sql
// Return all subscriptions with outstanding balance
```

---

## 🔵 LOW PRIORITY - Enhanced Features

### 13. Create MemberAttributes Entity
For storing flexible member data (address, emergency contact, health info, etc.)

### 14. Add Subscription Cancellation
With refund calculation for prorated amounts

### 15. Add Receipt Generation
Generate PDF receipts for payments

### 16. Add Dashboard Statistics
Total members, active subscriptions, revenue, etc.

### 17. Add Authentication & Authorization
Spring Security with JWT tokens

### 18. Add Global Exception Handling
@ControllerAdvice for consistent error responses

### 19. Add Input Validation
@Valid annotations and custom validators

### 20. Add API Documentation
Swagger/OpenAPI documentation

---

## 📋 Implementation Checklist

### Phase 1: Fix Core Logic (Week 1-2)
- [ ] Change all ID types from `int` to `Long`
- [ ] Change all money types from `int/double` to `BigDecimal`
- [ ] Implement unified subscription enrollment with auto-calculated dates
- [ ] Implement automatic charge creation with subscription
- [ ] Implement payment post-processing logic
- [ ] Create MemberAccess entity and service
- [ ] Update member access after payments

### Phase 2: Complete Core Entities (Week 3-4)
- [ ] Create Users entity (staff management)
- [ ] Create Attendance entity
- [ ] Create GraceExtension entity
- [ ] Create NotificationQueue entity
- [ ] Create FamilyMember junction entity
- [ ] Add all entity relationships (@OneToMany, @ManyToOne)

### Phase 3: Business Logic (Week 5-6)
- [ ] Implement subscription renewal
- [ ] Implement grace period extension
- [ ] Implement automated status updates (background jobs)
- [ ] Implement notification sending
- [ ] Implement attendance check-in with access control

### Phase 4: Monitoring & Reports (Week 7-8)
- [ ] Implement falling behind report
- [ ] Implement blocked members report
- [ ] Implement no-visits report
- [ ] Implement balance due report
- [ ] Implement daily collection report
- [ ] Dashboard statistics

### Phase 5: Polish & Production Ready (Week 9-10)
- [ ] Add global exception handling
- [ ] Add input validation
- [ ] Add authentication & authorization
- [ ] Add API documentation
- [ ] Add logging and monitoring
- [ ] Add unit and integration tests
- [ ] Performance optimization
- [ ] Deploy to production

---

## 🎯 Key Business Rules Summary

1. **Subscription Creation:**
   - Auto-calculate: endDate, dueDate, graceEndDate
   - Auto-create: SubscriptionCharges
   - No overlapping active subscriptions per member/family

2. **Payment Processing:**
   - If fully paid → Update member_access to ALLOWED
   - If subscription was BLOCKED → Change to ACTIVE
   - Track who received payment (receivedByUserId)

3. **Access Control:**
   - Default: BLOCKED on member creation
   - ALLOWED: After full payment
   - BLOCKED: After grace period expires or manual block

4. **Grace Period:**
   - Default: 7 days after end_date
   - Can be extended by COACH or ADMIN
   - All extensions logged in grace_extensions table

5. **Status Flow:**
   - ACTIVE → (after end_date) → IN_GRACE → (after grace_end_date) → BLOCKED
   - Can return to ACTIVE after full payment

6. **Plan Eligibility:**
   - KIDS plans: Check member age is within plan's age_min/age_max
   - FAMILY plans: Check family member count <= plan's max_family_members
   - INDIVIDUAL plans: No special checks

---

## 💡 Architecture Best Practices

1. **Separation of Concerns:**
   - Controllers: Handle HTTP, validate input, call services
   - Services: Business logic, orchestration, transactions
   - Repositories: Data access only

2. **DTOs vs Entities:**
   - Never expose entities directly in API
   - Use DTOs for requests and responses
   - Separate request DTOs from response DTOs

3. **Transaction Management:**
   - Use @Transactional for multi-step operations
   - Subscription enrollment = one transaction
   - Payment + access update = one transaction

4. **Auditing:**
   - Track who created (createdByUserId)
   - Track who modified (updatedByUserId)
   - Track when (timestamps)

5. **Data Types:**
   - IDs: Long (not int)
   - Money: BigDecimal (not double/int)
   - Timestamps: LocalDateTime (not LocalDate)
   - Dates: LocalDate (for actual dates like DOB)

---

## 📞 Integration Points

### Door System Integration
- **Endpoint:** `GET /api/v1/access/check/{memberId}`
- **Response:** `{ "allowed": true, "reason": null }`
- **Logic:** Query member_access table
- **Update:** After payments, grace extensions, subscription changes

### Notification System Integration
- **SMS Gateway:** Configure for sending text messages
- **Email Service:** Configure for sending emails
- **Queue Processing:** Background job to send pending notifications

### Payment Gateway (Future)
- **Currently:** Cash only
- **Future:** Integrate card payment, mobile payment
- **Design:** Payment method enum ready for expansion

---

Good luck with the implementation! Focus on Phase 1 first - get the core business logic working correctly before adding additional features.




