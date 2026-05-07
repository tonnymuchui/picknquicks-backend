# Frontend Ordering Endpoints - Complete Guide

This guide shows all endpoints needed for a successful ordering flow in your Pick N Quicks e-commerce application.

## 📋 Table of Contents

1. [Shopping Cart Endpoints](#shopping-cart-endpoints)
2. [Product Endpoints](#product-endpoints)
3. [Shipping Endpoints](#shipping-endpoints)
4. [Order Endpoints](#order-endpoints)
5. [Payment Endpoints](#payment-endpoints)
6. [Complete Ordering Flow](#complete-ordering-flow)
7. [Request/Response Examples](#request--response-examples)

---

## 🛒 Shopping Cart Endpoints

### 1. Get Current Cart
```
GET /api/cart
Headers: 
  X-Guest-Token: {guestToken}  (Optional for guests)
  Authorization: Bearer {token} (Optional for authenticated users)
```
**Used for**: Retrieving current cart items and totals

**Response**:
```json
{
  "success": true,
  "message": "Cart fetched successfully",
  "data": {
    "cartId": "uuid",
    "items": [
      {
        "cartItemId": "uuid",
        "productId": "uuid",
        "productName": "Product Name",
        "quantity": 2,
        "price": 1500.00,
        "subtotal": 3000.00
      }
    ],
    "subtotal": 3000.00,
    "tax": 300.00,
    "total": 3300.00
  }
}
```

---

### 2. Add Item to Cart
```
POST /api/cart/items
Headers:
  X-Guest-Token: {guestToken}  (Optional for guests)
  Content-Type: application/json
```
**Body**:
```json
{
  "productId": "uuid",
  "quantity": 2
}
```

---

### 3. Update Cart Item Quantity
```
PUT /api/cart/items/{cartItemId}
Headers:
  X-Guest-Token: {guestToken}
  Content-Type: application/json
```
**Body**:
```json
{
  "quantity": 5
}
```

---

### 4. Remove Item from Cart
```
DELETE /api/cart/items/{cartItemId}
Headers:
  X-Guest-Token: {guestToken}
```

---

### 5. Clear Cart
```
DELETE /api/cart
Headers:
  X-Guest-Token: {guestToken}
```

---

### 6. Merge Guest Cart on Login (Authenticated Users)
```
POST /api/cart/merge
Headers:
  X-Guest-Token: {guestToken}
  Authorization: Bearer {token}
  Content-Type: application/json

Query Parameters:
  guestToken={guestToken}
  userId={userId}
```
**Used for**: When a guest logs in, merge their cart with user's cart

---

## 📦 Product Endpoints

### Get Products (Browse)
```
GET /api/products
Query Parameters:
  page=0
  size=20
  categoryId={optional}
  brandId={optional}
  minPrice={optional}
  maxPrice={optional}
  search={optional}
```

---

## 🚚 Shipping Endpoints

### 1. Get Available Shipping Rates
```
GET /api/shipping/rates
Query Parameters:
  city={cityName} (Required)
  orderAmount={amount} (Optional, for free shipping calculation)
```
**Response**:
```json
{
  "success": true,
  "data": [
    {
      "rateId": "uuid",
      "name": "Standard Shipping",
      "baseCost": 500.00,
      "estimatedDaysMin": 3,
      "estimatedDaysMax": 5,
      "freeShippingThreshold": 5000.00
    }
  ]
}
```

---

### 2. Calculate Shipping Cost
```
GET /api/shipping/cost
Query Parameters:
  city={cityName} (Required)
  orderAmount={amount} (Required)
```
**Response**:
```json
{
  "success": true,
  "data": 400.00  // Shipping cost, or 0 if qualifies for free shipping
}
```

---

### 3. Get Estimated Delivery Days
```
GET /api/shipping/delivery-days
Query Parameters:
  city={cityName} (Required)
```
**Response**:
```json
{
  "success": true,
  "data": 3  // Estimated days
}
```

---

### 4. Get All Shipping Zones
```
GET /api/shipping/zones
```
**Used for**: Showing customers available shipping zones and locations

---

## 📦 Order Endpoints

### 1. Create Order (Checkout)
```
POST /api/orders
Headers:
  X-Guest-Token: {guestToken}  (For guests)
  Authorization: Bearer {token} (For authenticated users)
  Content-Type: application/json
```

**Body**:
```json
{
  "customerName": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "254123456789",
  "shippingCity": "Nairobi",
  "shippingCounty": "Nairobi",
  "postalCode": "00100",
  "shippingAddress": "123 Main Street",
  "paymentMethod": "MPESA",  // or "CASH_ON_DELIVERY"
  "notes": "Please handle with care"
}
```

**Response** (On Success - Status 201):
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "orderId": "uuid",
    "orderNumber": "ORD-2026-001",
    "status": "PENDING",
    "paymentStatus": "PENDING",
    "paymentMethod": "MPESA",
    "totalAmount": 3900.00,
    "subtotal": 3000.00,
    "tax": 300.00,
    "shippingCost": 600.00,
    "items": [
      {
        "productId": "uuid",
        "productName": "Product",
        "quantity": 2,
        "price": 1500.00
      }
    ]
  }
}
```

---

### 2. Get Order by ID
```
GET /api/orders/{orderId}
```

---

### 3. Get Order by Order Number
```
GET /api/orders/number/{orderNumber}
Query Parameters:
  email={email}  (For verification, optional)
```
**Used for**: Tracking orders after checkout

---

### 4. Get User Orders (Authenticated)
```
GET /api/orders/user/{userId}
Headers:
  Authorization: Bearer {token}

Query Parameters:
  page=0
  size=10
```

---

### 5. Get Guest Orders
```
GET /api/orders/guest
Query Parameters:
  email={email} (Required)
  page=0
  size=10
```

---

### 6. Cancel Order
```
DELETE /api/orders/{orderId}
Query Parameters:
  reason={cancellationReason}  (Optional)
```
**Used for**: Cancelling orders before payment

---

## 💳 Payment Endpoints

### 1. Initiate M-Pesa Payment (⚠️ NEEDS TO BE CREATED)
**⚠️ NOTE**: This endpoint is MISSING from your PaymentController!

You need to add this endpoint:
```
POST /api/payments/mpesa/initiate
Headers:
  Authorization: Bearer {token} (Optional, can be guest)
  Content-Type: application/json
```

**Suggested Body**:
```json
{
  "orderId": "uuid",
  "phoneNumber": "254123456789",
  "amount": 3900.00
}
```

**Expected Response**:
```json
{
  "success": true,
  "data": {
    "checkoutRequestID": "ws_CO_DMZ_123456789",
    "merchantRequestID": "16813-1590513-1",
    "responseCode": "0",
    "responseDescription": "Success. Request accepted for processing",
    "customerMessage": "Success. Request accepted for processing"
  }
}
```

---

### 2. M-Pesa Callback (Webhook)
```
POST /api/payments/mpesa/callback
(This is called by M-Pesa servers, not your frontend)
```

---

### 3. Query M-Pesa Transaction Status
```
GET /api/payments/mpesa/query/{checkoutRequestId}
```
**Used for**: Checking payment status if needed

---

## 🔄 Complete Ordering Flow

Here's the recommended sequence for your frontend:

### For Guests:
```
1. Generate/Get Guest Token
   ↓
2. Browse Products (GET /api/products)
   ↓
3. Add Items to Cart (POST /api/cart/items)
   ↓
4. Get Cart (GET /api/cart)
   ↓
5. Get Shipping Rates (GET /api/shipping/rates?city=...)
   ↓
6. Calculate Shipping Cost (GET /api/shipping/cost?city=...&orderAmount=...)
   ↓
7. Create Order (POST /api/orders)
   ↓
8. IF paymentMethod = "MPESA":
   a. Initiate M-Pesa (POST /api/payments/mpesa/initiate)
   b. Show STK Prompt to User (M-Pesa will send it automatically)
   c. Wait for Callback (Order status will update automatically)
   d. Show Success (Order moves to PAID status)
   
   IF paymentMethod = "CASH_ON_DELIVERY":
   a. Show Confirmation (Order moves to PROCESSING)
   b. Wait for Driver (Shipping)
```

### For Authenticated Users:
```
1. Browse Products & Add to Cart (same as guests)
   ↓
2. Merge Cart if needed (POST /api/cart/merge)
   ↓
3. Create Order (POST /api/orders)
   ↓
4. Same payment flow as guests
   ↓
5. View Order History (GET /api/orders/user/{userId})
```

---

## 📝 Request/Response Examples

### Example 1: Complete Guest Checkout with M-Pesa

#### Step 1: Create Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "X-Guest-Token: guest-token-123" \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "John Doe",
    "email": "john@example.com",
    "phoneNumber": "254712345678",
    "shippingCity": "Nairobi",
    "shippingCounty": "Nairobi",
    "postalCode": "00100",
    "shippingAddress": "123 Main Street",
    "paymentMethod": "MPESA",
    "notes": "Please deliver morning"
  }'
```

**Response**:
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "orderId": "550e8400-e29b-41d4-a716-446655440000",
    "orderNumber": "ORD-2026-00123",
    "status": "PENDING",
    "paymentStatus": "PENDING",
    "paymentMethod": "MPESA",
    "totalAmount": 3900.00
  }
}
```

#### Step 2: Initiate M-Pesa Payment
```bash
curl -X POST http://localhost:8080/api/payments/mpesa/initiate \
  -H "X-Guest-Token: guest-token-123" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "550e8400-e29b-41d4-a716-446655440000",
    "phoneNumber": "254712345678",
    "amount": 3900.00
  }'
```

**Response**:
```json
{
  "success": true,
  "data": {
    "checkoutRequestID": "ws_CO_DMZ_123456789",
    "responseCode": "0",
    "responseDescription": "Success. Request accepted for processing"
  }
}
```

#### Step 3: User receives STK prompt on phone
- M-Pesa sends automatic STK to 254712345678
- User enters PIN to complete payment

#### Step 4: Callback received
- M-Pesa sends callback to `/api/payments/mpesa/callback`
- Order status automatically changes to PAID
- Frontend should poll order status or listen for webhook notifications

#### Step 5: Track order
```bash
curl http://localhost:8080/api/orders/number/ORD-2026-00123
```

---

### Example 2: Complete Guest Checkout with CASH_ON_DELIVERY

#### Step 1: Create Order (Same as M-Pesa)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "X-Guest-Token: guest-token-123" \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Jane Doe",
    "email": "jane@example.com",
    "phoneNumber": "254712345678",
    "shippingCity": "Nairobi",
    "shippingCounty": "Nairobi",
    "postalCode": "00100",
    "shippingAddress": "456 Oak Avenue",
    "paymentMethod": "CASH_ON_DELIVERY"
  }'
```

**Response**:
```json
{
  "success": true,
  "data": {
    "orderId": "550e8400-e29b-41d4-a716-446655440001",
    "orderNumber": "ORD-2026-00124",
    "status": "PROCESSING",
    "paymentStatus": "PENDING",
    "paymentMethod": "CASH_ON_DELIVERY",
    "totalAmount": 3900.00
  }
}
```

#### Step 2: Show Confirmation
- Order is immediately in PROCESSING status
- Driver will arrive within estimated delivery time
- Payment happens at delivery

---

## ⚠️ Important Notes for Frontend Integration

### 1. Guest Token Management
- Generate or retrieve guest token on first visit
- Store in localStorage or sessionStorage
- Pass in all cart/order requests
- Format: `X-Guest-Token: {token}` header OR `?guestToken={token}` query param

### 2. Authentication
- After login, merge guest cart with user cart
- Use Authorization header: `Authorization: Bearer {JWT-token}`
- Then no need to pass guest token

### 3. M-Pesa Payment Flow
⚠️ **CRITICAL**: Add endpoint for initiating M-Pesa payment!
- Currently missing: `POST /api/payments/mpesa/initiate`
- This endpoint should:
  - Validate order exists
  - Validate payment method is M-Pesa
  - Call M-Pesa API via MpesaServiceImpl
  - Return checkoutRequestID to show user

### 4. Payment Validation
- **✅ FIXED**: CASH_ON_DELIVERY orders cannot be marked as PAID via M-Pesa
- **✅ FIXED**: Only actual M-Pesa callbacks mark payments as COMPLETED
- **✅ FIXED**: Duplicate payments prevented with proper state validation

### 5. Error Handling
```json
// Validation Error (400)
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "Invalid email format",
    "phoneNumber": "Phone must start with 254 or 0"
  }
}

// Not Found (404)
{
  "success": false,
  "message": "Order not found"
}

// Rate Limited (429)
{
  "success": false,
  "message": "Too many requests. Please try again later"
}
```

---

## 🔐 Frontend Security Best Practices

1. **HTTPS Only**: Always use HTTPS in production
2. **Store Tokens Securely**: Use httpOnly cookies if possible
3. **CORS**: Server should have proper CORS configuration
4. **Guest Tokens**: Should expire after inactivity
5. **Payment Data**: Never log or store full phone numbers
6. **Order Validation**: Always verify order totals on backend

---

## ✅ Ordering Checklist for Frontend Developer

- [ ] Implement cart management (add, remove, update, clear)
- [ ] Get shipping rates before checkout
- [ ] Create order with proper validation
- [ ] Handle M-Pesa payment initiation
- [ ] Display order confirmation
- [ ] Show order tracking/status
- [ ] Handle payment callbacks
- [ ] Implement guest checkout flow
- [ ] Implement authenticated checkout flow
- [ ] Handle error cases gracefully
- [ ] Add loading states for API calls
- [ ] Validate form inputs before submission
- [ ] Implement rate limiting on client side
- [ ] Add proper error notifications

---

## 🚀 Next Steps

1. **Create M-Pesa Initiation Endpoint** - Add POST /api/payments/mpesa/initiate
2. **Test Complete Flow** - Test guest and authenticated checkout
3. **Implement Frontend UI** - Follow the ordering flow documented above
4. **Handle Edge Cases** - Network failures, timeouts, etc.
5. **Add Analytics** - Track checkout abandonment, payment success rates
6. **Performance** - Cache shipping rates, optimize product browsing

---

## 📞 Support

For questions about specific endpoints, refer to:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Order Architecture: `ARCHITECTURE_MPESA_REFACTOR.md`
- Controller Code: Check individual controller files

