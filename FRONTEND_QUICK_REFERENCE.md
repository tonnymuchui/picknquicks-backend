# Frontend Ordering - Quick Reference Guide

## 🚀 Ordering Endpoints at a Glance

### Base URL
```
http://localhost:8080/api
```

---

## 📋 Quick Reference Table

| Feature | Endpoint | Method | Headers | Notes |
|---------|----------|--------|---------|-------|
| Get Cart | `/cart` | GET | X-Guest-Token | Get current shopping cart |
| Add to Cart | `/cart/items` | POST | X-Guest-Token | Add product to cart |
| Update Cart | `/cart/items/{id}` | PUT | X-Guest-Token | Change quantity |
| Remove from Cart | `/cart/items/{id}` | DELETE | X-Guest-Token | Remove item |
| Clear Cart | `/cart` | DELETE | X-Guest-Token | Empty entire cart |
| Get Products | `/products` | GET | - | Browse with filters |
| Get Shipping Rates | `/shipping/rates?city=Nairobi` | GET | - | Show shipping options |
| Calculate Shipping | `/shipping/cost?city=Nairobi&orderAmount=3000` | GET | - | Get shipping price |
| Create Order | `/orders` | POST | X-Guest-Token | Checkout |
| Initiate M-Pesa | `/payments/mpesa/initiate` | POST | - | Send STK push |
| Get Order Status | `/orders/{orderId}` | GET | - | Track order |
| Track by Number | `/orders/number/ORD-xxxx` | GET | - | Public tracking |

---

## 🔄 Ordering Flow Sequence

### Guest Checkout with M-Pesa

```
1️⃣ BROWSE
   GET /api/products
   
2️⃣ SHOP
   POST /api/cart/items (Add product)
   GET /api/cart (View cart)
   
3️⃣ CALCULATE SHIPPING
   GET /api/shipping/cost?city=Nairobi&orderAmount=3000
   
4️⃣ CHECKOUT
   POST /api/orders
   ├─ Returns: orderId, orderNumber, totalAmount
   
5️⃣ PAY
   POST /api/payments/mpesa/initiate
   ├─ Phone receives STK prompt
   ├─ User enters PIN
   
6️⃣ CONFIRM
   Wait for callback...
   Order status changes to PAID
   
7️⃣ TRACK
   GET /api/orders/number/ORD-xxxx
   ├─ Shows delivery status
   ├─ Shows shipping tracking number
```

### Authenticated User Checkout

```
Same as guest, but:
- Use Authorization: Bearer {token} instead of X-Guest-Token
- GET /api/orders/user/{userId} to see all orders
```

---

## 📝 Request/Response Examples

### Example 1: Create Order
**Request:**
```bash
POST /api/orders
X-Guest-Token: guest-token-123
Content-Type: application/json

{
  "customerName": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "254712345678",
  "shippingCity": "Nairobi",
  "shippingCounty": "Nairobi",
  "shippingAddress": "123 Main Street",
  "postalCode": "00100",
  "paymentMethod": "MPESA"
}
```

**Response (201 Created):**
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
    "totalAmount": 3900.00,
    "subtotal": 3000.00,
    "tax": 300.00,
    "shippingCost": 600.00
  }
}
```

---

### Example 2: Initiate M-Pesa Payment
**Request:**
```bash
POST /api/payments/mpesa/initiate
Content-Type: application/json

{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "phoneNumber": "254712345678",
  "amount": 3900.00
}
```

**Response:**
```json
{
  "success": true,
  "message": "M-Pesa payment initiated. Please enter your PIN on your phone.",
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

### Example 3: Get Shipping Cost
**Request:**
```bash
GET /api/shipping/cost?city=Nairobi&orderAmount=3000
```

**Response:**
```json
{
  "success": true,
  "message": "Shipping cost calculated successfully",
  "data": 600.00
}
```

---

### Example 4: Get Order Status
**Request:**
```bash
GET /api/orders/number/ORD-2026-00123
```

**Response:**
```json
{
  "success": true,
  "message": "Order fetched successfully",
  "data": {
    "orderId": "550e8400-e29b-41d4-a716-446655440000",
    "orderNumber": "ORD-2026-00123",
    "status": "PAID",
    "paymentStatus": "COMPLETED",
    "trackingNumber": "Z1234567890ZA",
    "estimatedDeliveryDate": "2026-05-10",
    "items": [
      {
        "productName": "Product Name",
        "quantity": 2,
        "price": 1500.00
      }
    ]
  }
}
```

---

## 🛠️ Implementation Checklist

### Cart Management
- [ ] Store guest token in localStorage
- [ ] Display cart item count in header
- [ ] Show cart total with taxes and shipping
- [ ] Handle add/remove/update actions
- [ ] Clear cart after successful order

### Checkout Page
- [ ] Validate form inputs before submission
- [ ] Show shipping options by city
- [ ] Display final total before payment
- [ ] Accept payment method selection (M-Pesa or COD)
- [ ] Prevent duplicate order submission

### M-Pesa Payment
- [ ] Call initiate endpoint with order details
- [ ] Show confirmation message to user
- [ ] Instruct user to enter PIN on phone
- [ ] Poll order status or listen for webhooks
- [ ] Handle payment timeout gracefully

### Order Tracking
- [ ] Display order tracking page
- [ ] Show status progression (Pending → Paid → Processing → Shipped → Delivered)
- [ ] Display tracking number and carrier info
- [ ] Show estimated delivery date
- [ ] Allow order cancellation if not shipped

---

## ⚠️ Important Notes

### Phone Number Format
Valid formats accepted:
- `254712345678` ✅
- `0712345678` ✅ (Converted to 254712345678)
- `+254712345678` ✅ (Converted to 254712345678)

Invalid:
- `712345678` ❌ (Missing country code)
- `+1234567890` ❌ (Wrong country code)

### Payment Methods
- **MPESA**: Online payment, STK push sent
- **CASH_ON_DELIVERY**: Payment at delivery, no online payment

### Amounts
- Minimum: 1 KES
- Maximum: 999,999.99 KES
- Must be positive number

---

## 🔐 Security Notes

1. **HTTPS Only**: Production must use HTTPS
2. **Guest Tokens**: 
   - Generate on first visit
   - Store in localStorage/sessionStorage
   - Pass in all cart/order requests
   - Expire after inactivity

3. **Phone Numbers**:
   - Never log full phone numbers
   - Mask in UI (254712****678)
   - Validate format server-side

4. **Order IDs**:
   - UUIDs (not sequential)
   - Cannot be guessed
   - Safe to share in URLs

5. **Payment Data**:
   - Never store M-Pesa credentials
   - Never log sensitive data
   - Use HTTPS for all transactions

---

## 🐛 Error Handling

### 400 Bad Request
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "Invalid email format",
    "phoneNumber": "Invalid phone number"
  }
}
```
**Action**: Show validation errors to user, ask to correct

### 404 Not Found
```json
{
  "success": false,
  "message": "Order not found"
}
```
**Action**: Show error message, redirect to order list

### 429 Too Many Requests
```json
{
  "success": false,
  "message": "Too many requests. Please try again later"
}
```
**Action**: Show loading state, retry after delay

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "Payment service error: Connection timeout"
}
```
**Action**: Show friendly error, offer support contact

---

## 📊 Status Flows

### Order Status Flow
```
PENDING
   ↓ (After payment or COD confirmation)
PAYMENT_PENDING / PROCESSING
   ↓
PAID
   ↓
READY_TO_SHIP
   ↓
SHIPPED
   ↓
DELIVERED ✅
```

### Payment Status Flow
```
PENDING
   ↓
PROCESSING (STK sent or waiting for COD)
   ↓
COMPLETED ✅  OR  FAILED ❌
```

---

## 🔗 Related Documentation

- **Full Endpoints Guide**: See `FRONTEND_ORDERING_ENDPOINTS.md`
- **M-Pesa Architecture**: See `ARCHITECTURE_MPESA_REFACTOR.md`
- **Swagger UI**: Visit `http://localhost:8080/swagger-ui.html`

---

## 📞 Troubleshooting

### STK Not Appearing
- Verify phone number format (254712345678)
- Check M-Pesa credentials in config
- Ensure user has active M-Pesa account
- Try with different phone number

### Order Not Created
- Validate all required fields are present
- Check cart is not empty
- Verify shipping address is valid
- Check server logs for errors

### Payment Not Processing
- Verify order exists with correct ID
- Check amount matches order total
- Ensure phone number is active
- Check M-Pesa service status

---

## ✅ Ready to Build!

You now have:
1. ✅ All required endpoints documented
2. ✅ Complete request/response examples
3. ✅ Error handling guide
4. ✅ Security best practices
5. ✅ Implementation checklist

**Next Steps**:
1. Set up frontend project
2. Implement cart management
3. Build checkout form
4. Integrate M-Pesa payment
5. Add order tracking
6. Test complete flow

