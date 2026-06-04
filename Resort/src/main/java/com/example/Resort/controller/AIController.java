package com.example.Resort.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class AIController {

    private final WebClient webClient = WebClient.create("https://api.groq.com/openai/v1");
    @Value("${groq.api.key}")
    private String apiKey;
    private static final String SESSION_CHAT = "chatHistory";

    @GetMapping("/ai")
    public String aiPage(Model model, HttpSession session) {
        addChatMessages(model, session);
        return "ai";
    }

    @PostMapping("/ask-ai")
    public String askAI(@RequestParam String prompt, Model model, HttpSession session) {
        List<Map<String, String>> chatHistory = getChatHistory(session);
        chatHistory.add(Map.of("sender", "User", "message", prompt));

        String response = callAI(prompt);
        chatHistory.add(Map.of("sender", "Resort AI", "message", response));

        session.setAttribute(SESSION_CHAT, chatHistory);
        model.addAttribute("messages", chatHistory);
        model.addAttribute("response", response);

        return "ai";
    }

    private void addChatMessages(Model model, HttpSession session) {
        List<Map<String, String>> chatHistory = getChatHistory(session);
        model.addAttribute("messages", chatHistory);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> getChatHistory(HttpSession session) {
        Object stored = session.getAttribute(SESSION_CHAT);
        if (stored instanceof List) {
            return (List<Map<String, String>>) stored;
        }
        return new ArrayList<>();
    }

    private String callAI(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                    "model", "llama3-8b-8192",
                    "messages", new Object[]{
                            Map.of("role", "user", "content", prompt)
                    }
            );

            return webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            return generateFallbackResponse(prompt);
        }
    }

    private String generateFallbackResponse(String prompt) {
        String lower = prompt == null ? "" : prompt.toLowerCase();
        if (lower.contains("hi") || lower.contains("hello") || lower.contains("hey")) {
            return "Hello! I can help with room booking, restaurant orders, services, or billing questions. What would you like to do today?";
        }
        if (lower.contains("room") || lower.contains("booking") || lower.contains("check-in") || lower.contains("checkin")) {
            return "You can browse rooms, add a booking, or view check-in details. Let me know the room type or dates you need.";
        }
        if (lower.contains("food") || lower.contains("restaurant") || lower.contains("menu")) {
            return "Our restaurant menu includes chef specials, pasta, and beverages. You can order from the restaurant page or ask for help to place an order.";
        }
        if (lower.contains("bill") || lower.contains("billing") || lower.contains("paid")) {
            return "You can view room and restaurant billing from the billing page. I can also help explain payment status and totals.";
        }
        return "I couldn't reach the online AI service just now, but I can still help with resort information, bookings, restaurant orders, and services.";
    }
}