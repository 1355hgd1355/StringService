package com.example.stringservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "news_sources")
public class NewsSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 512)
    private String url;

    @Column(name = "feed_type")
    private String feedType = "RSS";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_scanned_at")
    private LocalDateTime lastScannedAt;

    @OneToMany(mappedBy = "source")
    private Set<NewsItem> newsItems;
}
