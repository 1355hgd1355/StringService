package com.example.stringservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "news_sources")
public class NewsSource {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(nullable = false, length = 512)
    private String url;
    
    @Column(name = "feed_type", length = 50)
    private String feedType = "RSS";
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_scanned_at")
    private LocalDateTime lastScannedAt;
    
    // Конструкторы
    public NewsSource() {}
    
    public NewsSource(String name, String url) {
        this.name = name;
        this.url = url;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.feedType = "RSS";
    }
    
    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getFeedType() {
        return feedType;
    }
    
    public void setFeedType(String feedType) {
        this.feedType = feedType;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastScannedAt() {
        return lastScannedAt;
    }
    
    public void setLastScannedAt(LocalDateTime lastScannedAt) {
        this.lastScannedAt = lastScannedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewsSource)) return false;
        NewsSource that = (NewsSource) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}