package com.example.stringservice.service;

import com.example.stringservice.model.*;
import com.example.stringservice.repository.SentNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    
    @Autowired
    private TelegramBotService telegramBotService;
    
    @Autowired
    private SentNotificationRepository sentNotificationRepository;
    
    @Transactional
    public void sendNewsNotification(TgUser user, NewsItem news, List<Tag> matchedTags) {
        // Отправляем через Telegram бота
        telegramBotService.sendNewsNotification(user.getChatId(), news, matchedTags);
        
        // Запоминаем, что отправили
        SentNotification notification = new SentNotification();
        notification.setUser(user);
        notification.setNews(news);
        notification.setSentAt(LocalDateTime.now());
        
        sentNotificationRepository.save(notification);
    }
    
    public boolean isNewsSentToUser(TgUser user, NewsItem news) {
        return sentNotificationRepository.existsByUserIdAndNewsId(user.getId(), news.getId());
    }
}