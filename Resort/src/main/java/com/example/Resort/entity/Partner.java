package com.example.Resort.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String partnerName;

    private String partnerUrl;

    private String restaurantId;

    @Column(unique = true)
    private String apiKey;

    private boolean active = true;

    private Instant createdAt = Instant.now();

    public Partner() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public String getPartnerUrl() { return partnerUrl; }
    public void setPartnerUrl(String partnerUrl) { this.partnerUrl = partnerUrl; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
