package com.example.Resort.controller;

import com.example.Resort.entity.Booking;
import com.example.Resort.entity.RestaurantOrder;
import com.example.Resort.repository.BookingRepository;
import com.example.Resort.repository.RestaurantOrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class PaymentController {

    private final BookingRepository bookingRepository;
    private final RestaurantOrderRepository restaurantOrderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${razorpay.test:true}")
    private boolean razorpayTestMode;

    @Value("${razorpay.key:}")
    private String razorpayKey;

    @Value("${razorpay.secret:}")
    private String razorpaySecret;

    public PaymentController(BookingRepository bookingRepository,
                             RestaurantOrderRepository restaurantOrderRepository) {
        this.bookingRepository = bookingRepository;
        this.restaurantOrderRepository = restaurantOrderRepository;
    }

    @PostMapping("/api/payment/create")
    @ResponseBody
    public Map<String, Object> createOrder(@RequestParam long amount,
                                           @RequestParam String type,
                                           @RequestParam(required = false) Long id) {
        String key = razorpayKey == null ? "" : razorpayKey.trim();
        String secret = razorpaySecret == null ? "" : razorpaySecret.trim();
        if (!key.isBlank() && !secret.isBlank()) {
            return createRazorpayOrder(amount, type, id, key, secret);
        }

        Map<String, Object> resp = new HashMap<>();
        String orderId = "test_order_" + UUID.randomUUID();
        resp.put("orderId", orderId);
        resp.put("amount", amount);
        resp.put("currency", "INR");
        resp.put("razorpayKey", key);
        resp.put("testMode", true);
        resp.put("receipt", type + "-" + (id == null ? "manual" : id));
        return resp;
    }

    @GetMapping("/payment/checkout")
    public String checkout(@RequestParam(required = false, defaultValue = "0") String amount,
                           @RequestParam String type,
                           @RequestParam(required = false) Long refId,
                           Model model) {
        long amountInPaise;
        try {
            amountInPaise = Math.round(Double.parseDouble(amount));
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount for Razorpay checkout: " + amount);
        }

        String key = razorpayKey == null ? "" : razorpayKey.trim();
        String secret = razorpaySecret == null ? "" : razorpaySecret.trim();
        Map<String, Object> orderData = (!key.isBlank() && !secret.isBlank())
                ? createRazorpayOrder(amountInPaise, type, refId, key, secret)
                : Map.of(
                "orderId", "test_order_" + UUID.randomUUID(),
                "amount", amountInPaise,
                "currency", "INR",
                "razorpayKey", key,
                "testMode", true,
                "receipt", type + "-" + (refId == null ? "manual" : refId)
        );

        model.addAttribute("orderId", orderData.get("orderId"));
        model.addAttribute("amount", orderData.get("amount"));
        model.addAttribute("currency", orderData.get("currency"));
        model.addAttribute("razorpayKey", orderData.get("razorpayKey"));
        model.addAttribute("testMode", orderData.get("testMode"));
        model.addAttribute("receipt", orderData.get("receipt"));
        model.addAttribute("type", type);
        model.addAttribute("refId", refId);
        model.addAttribute("amountDisplay", String.format("₹%.2f", amountInPaise / 100.0));
        model.addAttribute("description", "Payment for " + type + " order");
        return "billing/checkout";
    }

    @PostMapping("/api/payment/verify")
    public String verifyPayment(@RequestParam String razorpay_payment_id,
                                @RequestParam String razorpay_order_id,
                                @RequestParam String razorpay_signature,
                                @RequestParam String type,
                                @RequestParam(required = false) Long refId,
                                @RequestParam(required = false, defaultValue = "0") Long amount,
                                Model model) {
        long amt = amount == null ? 0L : amount;
        double amountRupees = amt / 100.0;
        boolean paid = false;
        String key = razorpayKey == null ? "" : razorpayKey.trim();
        String secret = razorpaySecret == null ? "" : razorpaySecret.trim();

        if (!key.isBlank() && !secret.isBlank()) {
            paid = verifySignature(razorpay_order_id, razorpay_payment_id, razorpay_signature, secret);
            if (!paid) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment verification failed");
            }
        } else if (razorpayTestMode) {
            paid = true;
        }

        if (paid && refId != null) {
            markPaid(type, refId);
        }

        model.addAttribute("orderId", razorpay_order_id);
        model.addAttribute("amount", String.format("₹%.2f", amountRupees));
        model.addAttribute("type", type);
        model.addAttribute("name", "Guest");
        double gst = amountRupees * 0.18;
        double total = amountRupees + gst;
        model.addAttribute("gst", String.format("₹%.2f", gst));
        model.addAttribute("total", String.format("₹%.2f", total));
        model.addAttribute("paid", paid);
        return "billing/gst-bill";
    }

    private Map<String, Object> createRazorpayOrder(long amount, String type, Long refId, String key, String secret) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", amount);
            payload.put("currency", "INR");
            payload.put("receipt", type + "-" + (refId == null ? UUID.randomUUID() : refId));
            payload.put("payment_capture", 1);
            String body = objectMapper.writeValueAsString(payload);

            String auth = Base64.getEncoder().encodeToString((key + ":" + secret).getBytes(StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.razorpay.com/v1/orders"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + auth)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Razorpay order creation failed: " + response.body());
            }
            JsonNode json = objectMapper.readTree(response.body());
            return Map.of(
                    "orderId", json.get("id").asText(),
                    "amount", json.get("amount").asLong(),
                    "currency", json.get("currency").asText(),
                    "razorpayKey", key,
                    "testMode", razorpayTestMode,
                    "receipt", json.get("receipt").asText()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create Razorpay order", e);
        }
    }

    private boolean verifySignature(String orderId, String paymentId, String signature, String secret) {
        try {
            String data = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String expected = bytesToHex(digest);
            return expected.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void markPaid(String type, Long refId) {
        if ("room".equalsIgnoreCase(type)) {
            Booking booking = bookingRepository.findById(refId).orElseThrow();
            booking.setPaymentStatus("Paid");
            bookingRepository.save(booking);
        } else if ("restaurant".equalsIgnoreCase(type)) {
            RestaurantOrder order = restaurantOrderRepository.findById(refId).orElseThrow();
            order.setStatus("Paid");
            restaurantOrderRepository.save(order);
        }
    }
}
