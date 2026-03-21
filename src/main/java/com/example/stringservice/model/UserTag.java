package com.example.stringservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_tags")
@IdClass(UserTagId.class)
public class UserTag {
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private TgUser user;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Конструкторы
    public UserTag() {}
    
    public UserTag(TgUser user, Tag tag) {
        this.user = user;
        this.tag = tag;
        this.createdAt = LocalDateTime.now();
    }
    
    // Геттеры и сеттеры
    public TgUser getUser() {
        return user;
    }
    
    public void setUser(TgUser user) {
        this.user = user;
    }
    
    public Tag getTag() {
        return tag;
    }
    
    public void setTag(Tag tag) {
        this.tag = tag;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}