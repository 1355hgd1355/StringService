package com.example.stringservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sent_notifications")
public class SentNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private TgUser user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id")
    private NewsItem news;
    
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
    
    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TgUser getUser() {
        return user;
    }
    
    public void setUser(TgUser user) {
        this.user = user;
    }
    
    public NewsItem getNews() {
        return news;
    }
    
    public void setNews(NewsItem news) {
        this.news = news;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}