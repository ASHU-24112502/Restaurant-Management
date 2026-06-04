package com.example.Resort.controller;

import com.example.Resort.entity.Partner;
import com.example.Resort.repository.PartnerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/partners")
public class PartnerController {

    private final PartnerRepository repository;

    public PartnerController(PartnerRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/pair")
    public ResponseEntity<Partner> pairPartner(@RequestBody Partner partner) {
        if (partner.getApiKey() == null || partner.getApiKey().isBlank()) {
            partner.setApiKey(java.util.UUID.randomUUID().toString().replace("-", ""));
        }
        Partner saved = repository.save(partner);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<Map<String, Object>> list(@RequestHeader(name = "X-API-KEY", required = false) String apiKey) {
        authorize(apiKey);
        return repository.findAll().stream()
                .map(this::partnerSummary)
                .collect(Collectors.toList());
    }

    @GetMapping("/me")
    public Map<String, Object> me(@RequestHeader(name = "X-API-KEY", required = false) String apiKey) {
        Partner partner = authorize(apiKey);
        return partnerSummary(partner);
    }

    @GetMapping("/validate")
    public Map<String, Object> validate(@RequestHeader(name = "X-API-KEY", required = false) String apiKey) {
        Partner partner = authorize(apiKey);
        return Map.of("valid", true, "partnerId", partner.getId(), "partnerName", partner.getPartnerName());
    }

    private Partner authorize(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing API key");
        }
        return repository.findByApiKey(apiKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key"));
    }

    private Map<String, Object> partnerSummary(Partner partner) {
        return Map.of(
                "id", partner.getId(),
                "partnerName", partner.getPartnerName(),
                "partnerUrl", partner.getPartnerUrl(),
                "restaurantId", partner.getRestaurantId(),
                "createdAt", partner.getCreatedAt(),
                "active", partner.isActive()
        );
    }
}
