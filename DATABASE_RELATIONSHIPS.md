# Database Relationships & Business Flow

## 📊 Entity Relationship Diagram (Text Format)

```
┌─────────────────┐
│     MEMBER      │
│─────────────────│
│ id (PK)         │◄─────┐
│ firstName       │      │
│ lastName        │      │ 1:1
│ dob             │      │
│ phone           │      │
│ email           │      │
│ joinedDate      │      │
│ status          │      │
└─────────────────┘      │
         │               │
         │ 1:N           │
         ▼               │
┌─────────────────┐      │     ┌──────────────────┐
│  SUBSCRIPTION   │      └─────┤  MEMBER_ACCESS   │
│─────────────────│            │──────────────────│
│ id (PK)         │            │ memberId (PK/FK) │
│ memberId (FK)   │◄───────────┤ accessStatus     │◄── Door checks this!
│ familyId (FK)   │            │ allowedUntil     │
│ planId (FK)     │            │ reason           │
│ startDate       │            │ updatedAt        │
│ endDate         │ Auto       │ updatedByUserId  │
│ dueDate         │ Calc       └──────────────────┘
│ graceEndDate    │   ▲
│ status          │   │
│ createdByUserId │   │ Payment updates this!
└─────────────────┘   │
         │ 1:1        │
         ▼            │
┌─────────────────┐   │
│ SUBSCRIPTION_   │   │
│    CHARGES      │   │
│─────────────────│   │
│ id (PK)         │   │
│ subscriptionId  │   │
│ totalAmount     │ Auto
│ discountAmount  │ Calc
│ netAmount       │   │
└─────────────────┘   │
         │ 1:N        │
         ▼            │
┌─────────────────┐   │
│    PAYMENTS     │───┘
│─────────────────│
│ id (PK)         │
│ subscriptionId  │
│ amount          │
│ paidOn          │
│ receiptNo       │
│ receivedByUserId│
│ notes           │
└─────────────────┘


┌─────────────────┐
│      PLAN       │
│─────────────────│
│ id (PK)         │
│ name            │◄────── Defines pricing & duration
│ durationDays    │
│ price           │
│ planType        │        (INDIVIDUAL/KIDS/FAMILY)
│ ageMin          │
│ ageMax          │
│ maxFamilyMembers│
│ isActive        │
└─────────────────┘
         ▲
         │
         │
         │
┌─────────────────┐
│  SUBSCRIPTION   │
│─────────────────│
│ planId (FK)     │
└─────────────────┘


┌─────────────────┐
│     FAMILY      │
│─────────────────│
│ id (PK)         │
│ familyName      │
│ createdAt       │
└─────────────────┘
         │ 1:N
         ▼
┌─────────────────┐         ┌─────────────────┐
│ FAMILY_MEMBERS  │  N:M    │     MEMBER      │
│─────────────────│◄────────┤─────────────────│
│ familyId (PK/FK)│         │ id (PK)         │
│ memberId (PK/FK)│         └─────────────────┘
│ role            │
└─────────────────┘
   (Junction Table)


┌─────────────────┐
│  SUBSCRIPTION   │
│─────────────────│
│ id (PK)         │
└─────────────────┘
         │ 1:N
         ▼
┌─────────────────┐         ┌─────────────────┐
│ GRACE_EXTENSION │         │      USER       │
│─────────────────│         │─────────────────│
│ id (PK)         │    N:1  │ id (PK)         │
│ subscriptionId  │◄────────┤ name            │
│ extendedByUserId│         │ role            │
│ oldGraceEndDate │         │ active          │
│ newGraceEndDate │         └─────────────────┘
│ reason          │              ▲
│ createdAt       │              │
└─────────────────┘              │
                                 │ Used for audit:
                                 │ - createdByUserId
                                 │ - receivedByUserId
                                 │ - extendedByUserId
                                 │ - updatedByUserId


┌─────────────────┐
│     MEMBER      │
│─────────────────│
│ id (PK)         │
└─────────────────┘
         │ 1:N
         ▼
┌─────────────────┐
│   ATTENDANCE    │
│─────────────────│
│ id (PK)         │◄── Logged when member checks in
│ memberId (FK)   │
│ checkInTime     │
│ capturedBy      │    (DOOR/MANUAL)
│ subscriptionId  │
└─────────────────┘


┌─────────────────┐
│     MEMBER      │
│─────────────────│
│ id (PK)         │
└─────────────────┘
         │ 1:N
         ▼
┌─────────────────┐         ┌─────────────────┐
│ NOTIFICATION_   │         │  SUBSCRIPTION   │
│     QUEUE       │    N:1  │─────────────────│
│─────────────────│◄────────┤ id (PK)         │
│ id (PK)         │         └─────────────────┘
│ memberId (FK)   │
│ subscriptionId  │
│ notificationType│    (RENEWAL_REMINDER/GRACE_WARNING/BLOCKED_NOTICE)
│ scheduledFor    │
│ sentAt          │
│ status          │    (PENDING/SENT/FAILED/CANCELLED)
│ channel         │    (SMS/EMAIL)
└─────────────────┘
```

---

## 🔄 Business Flow Diagrams

### Flow 1: Member Enrollment & Payment

```
┌─────────────┐
│   CLIENT    │
└──────┬──────┘
       │ POST /api/v1/subscription/enroll
       │ { memberId, planId, discountAmount, initialPayment }
       ▼
┌──────────────────────────────────────────────────────┐
│           SUBSCRIPTION SERVICE                       │
│  (All logic in ONE transaction)                      │
├──────────────────────────────────────────────────────┤
│                                                      │
│  1. Validate Member & Plan exist                    │
│     ├─ Check member.status = ACTIVE                 │
│     ├─ Check plan.isActive = true                   │
│     └─ Check no overlapping subscriptions           │
│                                                      │
│  2. Calculate Dates AUTOMATICALLY                   │
│     ├─ endDate = startDate + plan.durationDays      │
│     ├─ dueDate = endDate                            │
│     └─ graceEndDate = endDate + 7 days              │
│                                                      │
│  3. Create Subscription                             │
│     ├─ Set status = ACTIVE                          │
│     └─ Link to member & plan                        │
│                                                      │
│  4. Create SubscriptionCharges AUTOMATICALLY        │
│     ├─ totalAmount = plan.price                     │
│     ├─ discountAmount = request.discountAmount      │
│     └─ netAmount = totalAmount - discountAmount     │
│                                                      │
│  5. IF initialPayment > 0:                          │
│     ├─ Create Payment record                        │
│     ├─ Calculate: totalPaid vs netAmount            │
│     └─ IF fully paid:                               │
│        ├─ Update MemberAccess.status = ALLOWED      │
│        └─ Set allowedUntil = graceEndDate           │
│     └─ ELSE:                                        │
│        └─ Keep MemberAccess.status = BLOCKED        │
│                                                      │
│  6. Schedule Notification                           │
│     └─ Create notification for (dueDate - 7 days)   │
│                                                      │
│  7. Return Complete Response                        │
│     └─ All subscription details + financial info    │
│                                                      │
└──────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────┐
│   CLIENT    │  Receives complete enrollment details
└─────────────┘
```

---

### Flow 2: Payment Processing & Access Update

```
┌─────────────┐
│   STAFF     │  (Receptionist receives cash payment)
└──────┬──────┘
       │ POST /api/v1/payment/record
       │ { subscriptionId, amount, receivedByUserId, note }
       ▼
┌──────────────────────────────────────────────────────┐
│             PAYMENT SERVICE                          │
│  (@Transactional - atomic operation)                 │
├──────────────────────────────────────────────────────┤
│                                                      │
│  1. Validate Payment                                │
│     ├─ Check subscription exists                    │
│     ├─ Check amount > 0                             │
│     └─ Check amount <= balance due                  │
│                                                      │
│  2. Generate Receipt Number                         │
│     └─ Format: REC-20260117-00001                   │
│                                                      │
│  3. Create Payment Record                           │
│     ├─ Link to subscription                         │
│     ├─ Set paidOn = now                             │
│     └─ Track receivedByUserId                       │
│                                                      │
│  4. Calculate New Balance                           │
│     ├─ totalPaid = SUM(all payments)                │
│     └─ balance = netAmount - totalPaid              │
│                                                      │
│  5. IF Fully Paid (balance <= 0):                   │
│     ├─ Get MemberAccess for member                  │
│     ├─ Update access.status = ALLOWED ✓             │
│     ├─ Set access.allowedUntil = graceEndDate       │
│     │                                                │
│     ├─ IF subscription.status = BLOCKED:            │
│     │  └─ Change to ACTIVE ✓                        │
│     │                                                │
│     └─ Cancel pending reminder notifications        │
│                                                      │
│  6. Send Payment Confirmation                       │
│     └─ SMS/Email to member                          │
│                                                      │
│  7. Return Response                                 │
│     ├─ Payment details                              │
│     ├─ Updated balance                              │
│     ├─ Access status                                │
│     └─ Receipt number                               │
│                                                      │
└──────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────┐
│ DOOR SYSTEM │  Can now verify access is ALLOWED
└─────────────┘
```

---

### Flow 3: Door Access Check

```
┌─────────────────┐
│  DOOR SYSTEM    │  (Member scans card/fingerprint)
│  (IoT Device)   │
└────────┬────────┘
         │ GET /api/v1/access/check/123
         │
         ▼
┌─────────────────────────────────────────────────┐
│      MEMBER ACCESS SERVICE                      │
├─────────────────────────────────────────────────┤
│                                                 │
│  1. Query MemberAccess by memberId             │
│                                                 │
│  2. Check access.status                        │
│     ├─ IF ALLOWED:                             │
│     │  ├─ Check allowedUntil >= today          │
│     │  └─ Return: { allowed: true }            │
│     │                                           │
│     └─ IF BLOCKED:                             │
│        └─ Return: {                            │
│             allowed: false,                     │
│             reason: "Payment overdue"           │
│           }                                     │
│                                                 │
│  3. IF allowed = true:                         │
│     └─ Create Attendance record                │
│        ├─ memberId                             │
│        ├─ checkInTime = now                    │
│        └─ capturedBy = DOOR                    │
│                                                 │
└─────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────┐
│  DOOR SYSTEM    │  Opens if allowed = true
└─────────────────┘  Stays closed if false
```

---

### Flow 4: Daily Automated Jobs (Background)

```
┌──────────────────────┐
│   SCHEDULER          │  Runs at 2:00 AM daily
│  (@Scheduled)        │
└──────────┬───────────┘
           │
           ▼
┌─────────────────────────────────────────────────────┐
│     SUBSCRIPTION STATUS UPDATE JOB                  │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Task 1: Move to Grace Period                      │
│  ─────────────────────────────                      │
│  Query: WHERE endDate < today                       │
│         AND status = 'ACTIVE'                       │
│  Action:                                            │
│    ├─ Update status = 'IN_GRACE'                    │
│    └─ Create notification (GRACE_WARNING)           │
│                                                     │
│  Task 2: Block Expired Subscriptions               │
│  ────────────────────────────────                   │
│  Query: WHERE graceEndDate < today                  │
│         AND status = 'IN_GRACE'                     │
│  Action:                                            │
│    ├─ Update subscription.status = 'BLOCKED'        │
│    ├─ Update memberAccess.status = 'BLOCKED'        │
│    ├─ Set reason = "Subscription expired"           │
│    └─ Create notification (BLOCKED_NOTICE)          │
│                                                     │
│  Task 3: Schedule Renewal Reminders                │
│  ───────────────────────────────                    │
│  Query: WHERE dueDate BETWEEN today AND today+7     │
│  Action:                                            │
│    └─ Create notification (RENEWAL_REMINDER)        │
│       scheduled for today at 10:00 AM               │
│                                                     │
└─────────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────┐
│ NOTIFICATION SERVICE │  Processes pending notifications
└──────────────────────┘
```

---

## 🎯 Status State Machine

```
┌─────────────────────────────────────────────────────┐
│           Subscription Status Flow                  │
└─────────────────────────────────────────────────────┘

        [CREATED]
            │
            │ On enrollment
            ▼
        ┌────────┐
        │ ACTIVE │◄─────────────┐
        └───┬────┘              │
            │                   │
            │ After endDate     │ Payment received
            │ (automated)       │ while BLOCKED
            ▼                   │
      ┌──────────┐              │
      │ IN_GRACE │              │
      └────┬─────┘              │
           │                    │
           │ After graceEndDate │
           │ (automated)        │
           ▼                    │
      ┌─────────┐               │
      │ BLOCKED │───────────────┘
      └────┬────┘
           │
           │ Manual action
           │ or renewal
           ▼
      ┌────────┐
      │ ENDED  │
      └────────┘


┌─────────────────────────────────────────────────────┐
│         Member Access Status Flow                   │
└─────────────────────────────────────────────────────┘

     [MEMBER CREATED]
            │
            │ Default status
            ▼
      ┌─────────┐
      │ BLOCKED │◄─────────────┐
      └────┬────┘              │
           │                   │
           │ After full        │ Subscription
           │ payment           │ expires or
           │                   │ manual block
           ▼                   │
      ┌─────────┐              │
      │ ALLOWED │──────────────┘
      └─────────┘
      
   Door Opens  │  ▲  Door Stays Closed
               │  │
               └──┘
          Access Check
```

---

## 📊 Balance Calculation Logic

```
┌─────────────────────────────────────────────────────┐
│        Subscription Balance Calculation             │
└─────────────────────────────────────────────────────┘

Subscription Created
      │
      ▼
SubscriptionCharges Created
      │
      ├─ totalAmount = plan.price (e.g., $5000)
      ├─ discountAmount = discount (e.g., $500)
      └─ netAmount = 5000 - 500 = $4500
                                    │
                                    ▼
                        ┌────────────────────────┐
                        │  Amount to be Paid     │
                        │     $4500.00           │
                        └────────────────────────┘
                                    │
                                    │
              ┌─────────────────────┼─────────────────────┐
              │                     │                     │
              ▼                     ▼                     ▼
       Payment #1             Payment #2            Payment #3
        $2000.00              $1000.00              $1500.00
              │                     │                     │
              └─────────────────────┴─────────────────────┘
                                    │
                                    ▼
                        Total Paid = SUM(payments)
                                 $4500.00
                                    │
                                    ▼
                    Balance = netAmount - totalPaid
                            = 4500 - 4500
                            = $0.00
                                    │
                                    ▼
                        ┌────────────────────────┐
                        │   FULLY PAID! ✓        │
                        │                        │
                        │ Actions Triggered:     │
                        │ ✓ Update access        │
                        │ ✓ Unblock member       │
                        │ ✓ Cancel reminders     │
                        └────────────────────────┘


Partial Payment Example:
      netAmount: $5000
      Payment #1: $2000
      ───────────────────
      Balance: $3000 (still owes)
      Access: BLOCKED (no change)
      
      Payment #2: $3000
      ───────────────────
      Balance: $0 (fully paid)
      Access: ALLOWED ✓
```

---

## 🔗 Key Relationships Summary

| Parent | Child | Type | Why |
|--------|-------|------|-----|
| Member | Subscription | 1:N | Member can have multiple subscriptions (historical) |
| Member | MemberAccess | 1:1 | Each member has one access record |
| Member | Attendance | 1:N | Member can check in multiple times |
| Family | FamilyMember | 1:N | Family has multiple members |
| FamilyMember | Member | N:M | Junction table for family-member relationship |
| Plan | Subscription | 1:N | Many subscriptions can use same plan |
| Subscription | SubscriptionCharges | 1:1 | Each subscription has exactly one charge record |
| Subscription | Payments | 1:N | Subscription can have multiple payments (installments) |
| Subscription | GraceExtension | 1:N | Subscription can be extended multiple times |
| Subscription | NotificationQueue | 1:N | Multiple notifications per subscription |
| User | Subscription | 1:N | User (staff) creates many subscriptions |
| User | Payment | 1:N | User (staff) receives many payments |
| User | GraceExtension | 1:N | User (coach) extends many grace periods |

---

## 🎬 Complete Business Scenario

```
Day 1: Member Registration
├─ Member registered
├─ MemberAccess created (status: BLOCKED)
└─ Door: CLOSED ❌

Day 1: Enrollment in 3-month plan ($5000)
├─ Subscription created
│  ├─ startDate: 2026-01-17
│  ├─ endDate: 2026-04-17 (calculated)
│  ├─ dueDate: 2026-04-17
│  ├─ graceEndDate: 2026-04-24
│  └─ status: ACTIVE
├─ SubscriptionCharges created
│  ├─ totalAmount: $5000
│  ├─ discountAmount: $0
│  └─ netAmount: $5000
├─ Payment: $5000 (full payment)
├─ MemberAccess updated (status: ALLOWED)
├─ Notification scheduled for 2026-04-10
└─ Door: OPEN ✓

Day 45: Regular check-in
├─ Door checks: /api/v1/access/check/123
├─ Response: { allowed: true }
├─ Attendance recorded
└─ Door: OPEN ✓

Day 83 (2026-04-10): Automated notification
└─ SMS sent: "Your membership expires in 7 days"

Day 90 (2026-04-17): End date reached
├─ Automated job runs
├─ Status changed: ACTIVE → IN_GRACE
├─ Member still has access (grace period)
└─ Door: OPEN ✓ (until 2026-04-24)

Day 97 (2026-04-24): Grace period ends
├─ Automated job runs
├─ Status changed: IN_GRACE → BLOCKED
├─ MemberAccess updated (status: BLOCKED)
├─ Notification sent: "Access blocked"
└─ Door: CLOSED ❌

Day 100: Payment received
├─ Payment: $5000
├─ New subscription created (renewal)
│  ├─ startDate: 2026-04-27
│  ├─ endDate: 2026-07-27
│  └─ status: ACTIVE
├─ MemberAccess updated (status: ALLOWED)
└─ Door: OPEN ✓
```

---

This covers the complete relationship structure and business flows for your gym management system!




