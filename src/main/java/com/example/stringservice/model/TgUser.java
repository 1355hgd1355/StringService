package com.example.stringservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tg_users")
public class TgUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chat_id", unique = true, nullable = false)
    private Long chatId;
    
    @Column(length = 255)
    private String username;
    
    @Column(name = "first_name", length = 255)
    private String firstName;
    
    @Column(name = "last_name", length = 255)
    private String lastName;
    
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "scan_interval_minutes")
    private Integer scanIntervalMinutes = 60;
    
    @Column(name = "news_count")
    private Integer newsCount = 5;
    
    // Конструкторы
    public TgUser() {}
    
    public TgUser(Long chatId, String username) {
        this.chatId = chatId;
        this.username = username;
        this.registeredAt = LocalDateTime.now();
        this.isActive = true;
        this.scanIntervalMinutes = 60;
        this.newsCount = 5;
    }
    
    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getChatId() {
        return chatId;
    }
    
    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
    
    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Integer getScanIntervalMinutes() {
        return scanIntervalMinutes;
    }
    
    public void setScanIntervalMinutes(Integer scanIntervalMinutes) {
        this.scanIntervalMinutes = scanIntervalMinutes;
    }
    
    public Integer getNewsCount() {
        return newsCount;
    }
    
    public void setNewsCount(Integer newsCount) {
        this.newsCount = newsCount;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TgUser)) return false;
        TgUser tgUser = (TgUser) o;
        return id != null && id.equals(tgUser.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}