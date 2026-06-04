# API Documentation (summary of new/updated endpoints)

This document lists the APIs added or updated in the project and where to add the Razorpay keys.

## Razorpay configuration
- Add your Razorpay keys into `src/main/resources/application.properties`:

```
razorpay.key=YOUR_KEY_HERE
razorpay.secret=YOUR_SECRET_HERE
razorpay.test=true
```

Set `razorpay.test=false` in production.

## Payment APIs (test-mode friendly)
- POST `/api/payment/create` - create a Razorpay payment order.
  - Params: `amount` (long, in paise), `type` (string: `room` or `restaurant`), optional `id` (reference id)
  - Response: JSON with `orderId`, `amount`, `currency`, `razorpayKey`, `receipt`, `testMode`
  - If valid `razorpay.key` and `razorpay.secret` are configured, this calls Razorpay's order API.

- GET `/payment/checkout` - render a checkout page that opens Razorpay Checkout.
  - Query params: `amount` (in paise), `type`, optional `refId`
  - Use the returned page button to launch Razorpay and pay.

- POST `/api/payment/verify` - verify a completed Razorpay payment.
  - Params: `razorpay_payment_id`, `razorpay_order_id`, `razorpay_signature`, `type`, optional `refId`, optional `amount`
  - Response: returns the `billing/gst-bill` view with the bill and payment status.

- GET `/billing/gst/{type}/{id}` - view generated GST bill (test-mode indicates Paid/ Pending)
  - Query param: `amount` (in paise)

## Partner (Zomato/Swiggy) API
- POST `/api/partners/pair` - pair a partner to a restaurant
  - Body (JSON): { "partnerName":"Zomato", "partnerUrl":"https://...", "restaurantId":"123" }
  - Returns: saved Partner object

- GET `/api/partners` - list paired partners

## Notes and next steps
- The payment integration is intentionally minimal and in test mode; to integrate with live Razorpay:
  - Use Razorpay Java SDK or call their REST API to create orders server-side using `razorpay.key` and `razorpay.secret`.
  - Implement webhook handling and signature verification using the secret.
- The GST bill template is located at `src/main/resources/templates/billing/gst-bill.html` and uses paise-to-rupee conversion.
- The header/footer fragments are in `src/main/resources/templates/fragments/` and are included in login and admin dashboard pages.

If you want, I can:
- Wire the PaymentController to create real Razorpay orders and verify signatures once you add the keys.
- Generate PDF bills (using iText or similar) instead of HTML.
- Add more pages to include the header/footer site-wide.

