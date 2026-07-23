# Exception Structure Summary

## Overview
All service layer exceptions have been organized into dedicated folders following the pattern established by `GraceExtensionExceptions`.

## Folder Structure

```
pro.sachin.fity.exception/
│
├── BusinessExceptionHandler.java (Base abstract class)
├── SubscriptionNotFoundException.java (Shared exception)
│
├── DeviceCommandExceptions/
│   ├── DeviceCommandException.java
│   ├── DeviceCommandNotFoundException.java
│   └── PayloadSerializationException.java
│
├── FamilyExceptions/
│   ├── FamilyHasActiveMembersException.java
│   ├── FamilyNotFoundException.java
│   ├── MemberAlreadyInFamilyException.java
│   └── MemberNotInFamilyException.java
│
├── GraceExtensionExceptions/ (ALREADY EXISTED)
│   ├── InvalidGraceExtensionDateException.java
│   ├── MaxExtensionDaysExceededException.java
│   ├── MaxExtensionLimitReachedException.java
│   └── NotInGracePeriodException.java
│
├── MemberAccessExceptions/
│   └── InvalidMemberAccessException.java
│
├── MemberExceptions/
│   ├── MemberAccessCreationException.java
│   ├── MemberNotFoundException.java
│   └── MemberRegistrationException.java
│
├── PaymentExceptions/
│   └── SubscriptionChargesNotFoundException.java
│
├── PlanExceptions/
│   └── PlanNotFoundException.java
│
└── SubscriptionExceptions/
    ├── ActiveSubscriptionAlreadyExistsException.java
    ├── ExcessiveBorrowedDaysException.java
    ├── InvalidRenewalStatusException.java
    ├── InvalidSubscriptionEntityException.java
    ├── PendingRenewalAlreadyExistsException.java
    └── PendingSubscriptionAlreadyExistsException.java
```

## Statistics

| Exception Folder | Number of Exceptions | Service Layer |
|------------------|---------------------|---------------|
| GraceExtensionExceptions | 4 | GraceExtensionServiceImpl |
| SubscriptionExceptions | 6 | SubscriptionServiceImpl |
| FamilyExceptions | 4 | FamilyServiceImpl |
| DeviceCommandExceptions | 3 | DeviceCommandServiceImpl |
| PaymentExceptions | 1 | PaymentServiceImpl |
| MemberExceptions | 3 | MemberServiceImpl |
| PlanExceptions | 1 | PlanServiceImpl |
| MemberAccessExceptions | 1 | MemberAccessImpl |
| **TOTAL** | **23** | **8 Services** |

**Plus 1 shared exception** (`SubscriptionNotFoundException`)

## Exception Hierarchy

```
java.lang.RuntimeException
    └── BusinessExceptionHandler (abstract)
        ├── SubscriptionNotFoundException
        │
        ├── DeviceCommandExceptions.*
        ├── FamilyExceptions.*
        ├── GraceExtensionExceptions.*
        ├── MemberAccessExceptions.*
        ├── MemberExceptions.*
        ├── PaymentExceptions.*
        ├── PlanExceptions.*
        └── SubscriptionExceptions.*
```

## Benefits of This Structure

1. **Organization**: Exceptions grouped by domain/service
2. **Consistency**: All follow the same pattern
3. **Maintainability**: Easy to locate and manage exceptions
4. **Clarity**: Exception names clearly indicate what went wrong
5. **Type Safety**: Better than generic `RuntimeException` or `EntityNotFoundException`

## Next Steps

See `EXCEPTIONS_MAPPING.md` for:
- Detailed mapping of each exception to its usage in code
- Line references where exceptions should be thrown
- Recommendations for updating service implementations
- Guide for creating global exception handler

## Pattern Example

Every exception follows this pattern:

```java
package pro.sachin.fity.exception.[ServiceName]Exceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class [ExceptionName] extends BusinessExceptionHandler {

    public [ExceptionName](String message) {
        super(message);
    }
}
```

This ensures:
- All business exceptions extend `BusinessExceptionHandler`
- All exceptions can be caught at the controller level
- Consistent error messaging across the application
