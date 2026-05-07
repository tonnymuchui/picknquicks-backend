# M-Pesa Payment System Architecture - Refactored with SOLID Principles

## Overview

A comprehensive refactoring of the M-Pesa payment processing system following SOLID principles and M-Pesa documentation requirements. The system now properly separates concerns, prevents double-charging, and ensures only validated payments are marked as completed.

## Problem Statement (Original Issues)

1. **Critical Bug**: Payments were being marked as COMPLETED without actual M-Pesa verification
2. **Design Flaw**: CASH_ON_DELIVERY orders were incorrectly marked as PAID through M-Pesa callbacks
3. **SRP Violation**: MpesaServiceImpl was handling authentication, validation, state management, and event publishing
4. **DRY Violation**: Order and payment state updates were duplicated across multiple code paths
5. **Missing Validations**: No idempotency, no payment method verification, incomplete callback validation

## Solution Architecture

### Component Breakdown

```
┌─────────────────────────────────────────────────────────┐
│                    MpesaServiceImpl                      │
│  (Orchestrator - Routes to appropriate services)       │
└─────────────────────────────────────────────────────────┘
              ↓                    ↓                ↓
    ┌────────────────┐  ┌──────────────────┐  ┌─────────┐
    │ Authentication │  │ Callback Handler │  │Initiate │
    │   Service      │  │   Processor      │  │STK Push │
    └────────────────┘  └──────────────────┘  └─────────┘
              ↓                    ↓                ↓
    ┌────────────────────────────────────────────────────┐
    │   Validation & State Management Services           │
    │  • PaymentStateService                             │
    │  • OrderStateService                               │
    │  • MpesaCallbackValidator                          │
    └────────────────────────────────────────────────────┘
              ↓                    ↓                ↓
    ┌────────────────────────────────────────────────────┐
    │              Domain Models                         │
    │  • Payment Entity (with state validation)          │
    │  • Order Entity (with state validation)            │
    └────────────────────────────────────────────────────┘
```

### Services Overview

#### 1. **MpesaAuthenticationService**
**Responsibility**: M-Pesa protocol-level authentication and token management

**Features**:
- OAuth token acquisition from M-Pesa API
- STK push password generation (Base64 encoding)
- Phone number formatting to M-Pesa format (254XXXXXXXXX)
- Transaction timestamp generation
- Follows M-Pesa API documentation

**Key Methods**:
- `obtainAccessToken()` - Gets OAuth token with Consumer Key/Secret
- `generatePassword(timestamp)` - Creates Base64-encoded password
- `formatPhoneNumber(phone)` - Normalizes phone to 254XXXXXXXXX format
- `generateTimestamp()` - Creates yyyyMMddHHmmss format timestamp

---

#### 2. **PaymentStateService**
**Responsibility**: Payment state machine - manages valid state transitions

**State Flow**:
```
PENDING
   ↓
PROCESSING (STK push sent)
   ├─→ COMPLETED (Payment received via M-Pesa)
   └─→ FAILED (Payment rejected or error)
```

**Features**:
- Ensures valid state transitions only
- Prevents double-completion (idempotency)
- Prevents invalid transitions (e.g., COMPLETED → FAILED)
- Stores transaction details upon completion
- Logs all state changes

**Key Methods**:
- `transitionToProcessing(...)` - On STK push initiated
- `transitionToCompleted(...)` - On successful M-Pesa callback
- `transitionToFailed(...)` - On payment rejection
- `canTransitionToCompleted(...)` - Validates state
- `isValidForCallbackProcessing(...)` - Checks if ready for callback

---

#### 3. **OrderStateService**
**Responsibility**: Order state management based on payment status

**Order State Transitions on Payment**:
```
M-Pesa Payment Flow:
PENDING → PAYMENT_PENDING (STK sent) → PAID (callback success) or PAYMENT_FAILED

CASH_ON_DELIVERY:
PENDING → PROCESSING → ... (no M-Pesa callback expected)
```

**Features**:
- Prevents M-Pesa callbacks on CASH_ON_DELIVERY orders (CRITICAL FIX)
- Updates order status when payment state changes
- Validates order can receive payment callbacks
- Proper logging for all order transitions

**Key Methods**:
- `handlePaymentCompleted(order)` - Updates to PAID
- `handlePaymentFailed(order, reason)` - Updates to PAYMENT_FAILED
- `handlePaymentInitiated(order)` - Updates to PAYMENT_PENDING
- `validateOrderCanReceivePayment(order)` - Ensures order is M-Pesa type

---

#### 4. **MpesaCallbackValidator**
**Responsibility**: M-Pesa callback verification and metadata extraction

**Validation Checks**:
- Callback structure integrity
- Checkout request ID presence and validity
- Payment existence and correct state (PROCESSING)
- Payment amount matches expected amount
- Required metadata fields (receipt, transaction ID)

**Features**:
- Structured callback validation
- Safe metadata extraction with null checks
- Payment amount validation prevents amount fraud
- Idempotency: handles duplicate callbacks gracefully

**Key Methods**:
- `extractAndValidate(callback)` - Structure validation
- `validatePaymentAmount(payment, amount)` - Amount verification
- `extractMetadata(stkCallback)` - Safe metadata extraction
- `isSuccessfulPayment(stkCallback)` - Result code check

---

#### 5. **MpesaCallbackProcessor**
**Responsibility**: Orchestrates callback processing workflow

**Workflow**:
```
1. Extract & validate callback
2. Find payment by checkout request ID
3. Find order with pessimistic lock
4. Validate order can receive callback
5. If successful (resultCode == 0):
   - Extract & validate metadata
   - Update payment to COMPLETED
   - Update order to PAID
   - Publish OrderPaidEvent
6. If failed:
   - Update payment to FAILED
   - Update order to PAYMENT_FAILED
7. Store callback data for audit trail
```

**Features**:
- Atomic callback processing (transactional)
- Event publishing for downstream handlers
- Comprehensive error handling
- Audit trail (stores callback JSON)

**Key Methods**:
- `processCallback(callback)` - Main entry point
- `handleSuccessfulPayment(...)` - Success paths
- `handleFailedPayment(...)` - Failure paths

---

#### 6. **MpesaServiceImpl** (Refactored)
**Responsibility**: M-Pesa payment service facade

**Delegated to Specialist Services**:
- Authentication → MpesaAuthenticationService
- Callback Processing → MpesaCallbackProcessor ✓
- Payment State → PaymentStateService
- Order State → OrderStateService
- Validation → MpesaCallbackValidator

**Features**:
- STK push initiation with proper validation
- Circuit breaker for fault tolerance
- Retry mechanism for transient failures
- Prevents duplicate STK pushes

**Key Methods**:
- `initiateStkPush(orderId, phone, amount)` - Initiates payment
- `handleCallback(callback)` - Delegates to processor
- `validateOrderAndPayment(order)` - STK push validation
- `initiateStkPushFallback(...)` - Circuit breaker fallback

## SOLID Principles Implementation

### Single Responsibility Principle (SRP)
✅ Each service has one reason to change:
- **PaymentStateService**: Only if payment state rules change
- **OrderStateService**: Only if order state rules change
- **MpesaCallbackValidator**: Only if callback validation rules change
- **MpesaAuthenticationService**: Only if M-Pesa auth protocol changes
- **MpesaCallbackProcessor**: Only if workflow orchestration changes

### Open/Closed Principle (OCP)
✅ Open for extension, closed for modification:
- State services can be extended with new state types
- Can add new payment methods without modifying existing code
- Validator can add new validation rules easily
- Callback processor can be extended for new callback types

### Liskov Substitution Principle (LSP)
✅ Proper inheritance and interface compliance:
- MpesaServiceImpl implements MpesaService contract correctly
- Services are interchangeable within their contracts

### Interface Segregation Principle (ISP)
✅ Clients depend only on methods they use:
- Focused service interfaces
- No unused dependencies injected
- Clear method contracts

### Dependency Inversion Principle (DIP)
✅ Depend on abstractions, not concretions:
- MpesaServiceImpl depends on services (abstractions)
- Constructor injection for loose coupling
- Easily testable and mockable

## DRY (Don't Repeat Yourself) Improvements

### Before (Code Duplication):
```java
// Duplicated payment state update logic
payment.setStatus(PaymentStatus.COMPLETED);
paymentRepository.save(payment);

// Duplicated order state update logic
order.setPaymentStatus(PaymentStatus.COMPLETED);
order.updateStatus(OrderStatus.PAID);
orderRepository.save(order);

// This was repeated in:
// - initiateStkPush()
// - handleCallback() - success path
// - handleCallback() - failure path
```

### After (Single Source of Truth):
```java
// Centralized payment state updates
paymentStateService.transitionToCompleted(payment, transactionId, receipt);
paymentRepository.save(payment);

// Centralized order state updates
orderStateService.handlePaymentCompleted(order);
orderRepository.save(order);
```

## Key Bug Fixes

### 1. **Payment Double-Charging Fix** ⚠️ CRITICAL
**Before**:
```java
// WRONG: Mark payment as complete without M-Pesa verification
if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
    order.setPaymentStatus(PaymentStatus.COMPLETED); // BUG!
    orderRepository.save(order);
}
```

**After**:
```java
// CORRECT: Validate payment method first
orderStateService.validateOrderCanReceivePayment(order);
// Throws BadRequestException if order is CASH_ON_DELIVERY
```

### 2. **CASH_ON_DELIVERY Order Protection** ⚠️ CRITICAL
**Before**:
- M-Pesa callbacks could mark CASH_ON_DELIVERY orders as PAID
- This allowed customers to bypass shipping fees

**After**:
```java
// New validation in OrderStateService
if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
    throw new BadRequestException(
        "Cannot mark CASH_ON_DELIVERY order as PAID via M-Pesa"
    );
}
```

### 3. **Payment State Validation**
**Before**:
- No check for duplicate STK pushes
- No validation of payment state before callback processing

**After**:
```java
// Prevent duplicate STK pushes
if (payment.getStatus() == PaymentStatus.PROCESSING) {
    throw new BadRequestException("Payment already in process");
}

// Idempotent callback handling
if (payment.getStatus() == PaymentStatus.COMPLETED) {
    log.warn("Duplicate callback for completed payment");
    return; // Handle gracefully
}
```

## Implementation Details

### Database Transaction Management
```java
@Transactional
public void handleCallback(MpesaCallbackRequest callback) {
    // All state changes are atomic
    // Either all succeed or none are persisted
}
```

### Error Handling Strategy
```
1. Validation errors → BadRequestException → 400 Bad Request
2. Not found errors → ResourceNotFoundException → 404 Not Found
3. Processing errors → RuntimeException with cause → 500
4. Transient errors → Retry via Resilience4j
5. Persistent errors → Circuit breaker fallback
```

### Audit Trail
```java
// Store complete callback for audit
payment.setCallbackData(objectMapper.writeValueAsString(callback));
```

## Testing Considerations

### Unit Test Strategy
```
✓ PaymentStateService: Test all state transitions
✓ OrderStateService: Test validation rules
✓ MpesaCallbackValidator: Test validation logic
✓ MpesaCallbackProcessor: Test workflow orchestration
✓ MpesaServiceImpl: Test delegation and circuit breaker
```

### Integration Test Strategy
```
✓ End-to-end STK push initiation
✓ Callback processing with valid/invalid data
✓ Double-callback idempotency
✓ CASH_ON_DELIVERY order rejection
✓ Concurrent payment processing
```

## Deployment Notes

### Database Constraints
- Payment table has unique constraint on `transaction_id`
- Index on `mpesa_checkout_request_id` for fast callback lookup
- Payment status index for query optimization

### Configuration Requirements
- M-Pesa Consumer Key/Secret must be configured
- M-Pesa API endpoints configured (staging vs production)
- Circuit breaker configuration for resilience

### M-Pesa API Compatibility
- Follows M-Pesa STK Push API v2
- Callback format matches official documentation
- Phone number formatting per M-Pesa requirements
- Timestamp format: yyyyMMddHHmmss (M-Pesa required)

## Commits

All changes committed individually for clear history:

1. **99068ba**: PaymentStateService - Payment state machine
2. **4d4f377**: OrderStateService - Order state management  
3. **0af4256**: MpesaCallbackValidator - Callback verification
4. **7a8670d**: MpesaAuthenticationService - M-Pesa protocol
5. **06a96ad**: MpesaCallbackProcessor - Callback orchestration
6. **dbc880c**: MpesaServiceImpl Refactor - Main service refactored

## Conclusion

This refactored architecture:
- ✅ Fixes critical payment bugs
- ✅ Follows SOLID principles
- ✅ Follows DRY principle
- ✅ Follows M-Pesa documentation
- ✅ Improves maintainability
- ✅ Enables easy testing
- ✅ Provides clear audit trail
- ✅ Is production-ready

