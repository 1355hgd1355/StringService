package com.example.stringservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

public class TgUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;

    private String username;
    private String firstName;
    private String lastName;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "scan_interval_minutes")
    private Integer scanIntervalMinutes = 60;

    @Column(name = "news_count")
    private Integer newsCount = 5;
}