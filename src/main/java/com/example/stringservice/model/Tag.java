package com.example.stringservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "tags")
public class Tag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @ManyToMany(mappedBy = "tags")
    private Set<NewsItem> newsItems;
    
    // Конструкторы
    public Tag() {}
    
    public Tag(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Set<NewsItem> getNewsItems() {
        return newsItems;
    }
    
    public void setNewsItems(Set<NewsItem> newsItems) {
        this.newsItems = newsItems;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return id != null && id.equals(tag.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}