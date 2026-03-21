package com.example.stringservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_sources")
@IdClass(UserSourceId.class)
public class UserSource {
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private TgUser user;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private NewsSource source;
    
    @Column(name = "is_enabled")
    private Boolean isEnabled = true;
    
    // Конструкторы
    public UserSource() {}
    
    public UserSource(TgUser user, NewsSource source) {
        this.user = user;
        this.source = source;
        this.isEnabled = true;
    }
    
    // Геттеры и сеттеры
    public TgUser getUser() {
        return user;
    }
    
    public void setUser(TgUser user) {
        this.user = user;
    }
    
    public NewsSource getSource() {
        return source;
    }
    
    public void setSource(NewsSource source) {
        this.source = source;
    }
    
    public Boolean getIsEnabled() {
        return isEnabled;
    }
    
    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}