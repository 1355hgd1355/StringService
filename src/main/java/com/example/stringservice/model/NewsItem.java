package com.example.stringservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "news_items")
public class NewsItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private NewsSource source;
    
    @Column(nullable = false, length = 1024)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false, length = 512, unique = true)
    private String link;
    
    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;
    
    @Column(name = "found_at", nullable = false)
    private LocalDateTime foundAt;
    
    @Column(length = 512)
    private String guid;
    
    // ВАЖНО: правильная связь ManyToMany
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "news_tags",
        joinColumns = @JoinColumn(name = "news_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
    
    // Конструкторы
    public NewsItem() {}
    
    public NewsItem(String title, String link, LocalDateTime publishedAt) {
        this.title = title;
        this.link = link;
        this.publishedAt = publishedAt;
        this.foundAt = LocalDateTime.now();
    }
    
    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public NewsSource getSource() {
        return source;
    }
    
    public void setSource(NewsSource source) {
        this.source = source;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getLink() {
        return link;
    }
    
    public void setLink(String link) {
        this.link = link;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public LocalDateTime getFoundAt() {
        return foundAt;
    }
    
    public void setFoundAt(LocalDateTime foundAt) {
        this.foundAt = foundAt;
    }
    
    public String getGuid() {
        return guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
    }
    
    public Set<Tag> getTags() {
        return tags;
    }
    
    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewsItem)) return false;
        NewsItem newsItem = (NewsItem) o;
        return link != null && link.equals(newsItem.link);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}