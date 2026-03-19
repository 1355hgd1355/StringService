package com.example.stringservice.service;

import com.example.stringservice.model.*;
import com.example.stringservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewsAggregatorService {
    
    @Autowired
    private NewsSourceRepository sourceRepository;
    
    @Autowired
    private NewsItemRepository newsItemRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private TgUserRepository userRepository;
    
    @Autowired
    private RssParserService rssParserService;
    
    @Autowired
    private TagMatchingService tagMatchingService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserSourceRepository userSourceRepository;
    
    @Autowired
    private UserTagRepository userTagRepository;
    
    /**
     * Планировщик сканирования новостей
     * Запускается каждую минуту, но для каждого пользователя проверяется его интервал
     */
    @Scheduled(fixedDelay = 60000) // Каждую минуту
    @Transactional
    public void scanAllSources() {
        System.out.println("Начинаем сканирование источников: " + LocalDateTime.now());
        
        // Получаем всех активных пользователей
        List<TgUser> activeUsers = userRepository.findByIsActiveTrue();
        
        // Для каждого пользователя проверяем, пора ли сканировать
        for (TgUser user : activeUsers) {
            checkAndScanForUser(user);
        }
    }
    
    /**
     * Проверяет, нужно ли сканировать для конкретного пользователя,
     * и если да - запускает сканирование
     */
    private void checkAndScanForUser(TgUser user) {
        // Здесь можно хранить время последнего сканирования для пользователя
        // Для простоты будем сканировать всегда, но в реальном проекте стоит добавить
        
        scanForUser(user);
    }
    
    /**
     * Сканирование источников для конкретного пользователя
     */
    @Async
    public void scanForUser(TgUser user) {
        // Получаем включенные источники пользователя
        List<NewsSource> userSources = userSourceRepository.findEnabledSourcesByUserId(user.getId());
        
        // Получаем теги пользователя
        List<Tag> userTags = tagRepository.findByUserId(user.getId());
        
        if (userSources.isEmpty() || userTags.isEmpty()) {
            // Нет источников или тегов - нечего сканировать
            return;
        }
        
        // Для каждого источника проверяем новости
        for (NewsSource source : userSources) {
            scanSourceForUser(source, user, userTags);
        }
    }
    
    /**
     * Сканирование одного источника для пользователя
     */
    private void scanSourceForUser(NewsSource source, TgUser user, List<Tag> userTags) {
        try {
            // Получаем последние N новостей из источника
            List<NewsItem> recentNews = rssParserService.parseRssFeed(source, user.getNewsCount());
            
            // Обновляем время последнего сканирования
            source.setLastScannedAt(LocalDateTime.now());
            sourceRepository.save(source);
            
            // Для каждой новости проверяем, есть ли она уже в БД
            for (NewsItem news : recentNews) {
                processNewsForUser(news, source, user, userTags);
            }
            
        } catch (Exception e) {
            System.err.println("Ошибка при сканировании источника " + source.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Обработка одной новости для пользователя
     */
    @Transactional
    public void processNewsForUser(NewsItem news, NewsSource source, TgUser user, List<Tag> userTags) {
        // Проверяем, есть ли уже такая новость в БД
        Optional<NewsItem> existingNews = newsItemRepository.findByLink(news.getLink());
        
        NewsItem savedNews;
        if (existingNews.isPresent()) {
            savedNews = existingNews.get();
        } else {
            // Сохраняем новую новость
            news.setSource(source);
            news.setFoundAt(LocalDateTime.now());
            savedNews = newsItemRepository.save(news);
            
            // Анализируем новость на все теги (для будущих пользователей)
            analyzeNewsTags(savedNews);
        }
        
        // Проверяем, отправляли ли уже эту новость пользователю
        if (notificationService.isNewsSentToUser(user, savedNews)) {
            return; // Уже отправляли
        }
        
        // Ищем совпадения с тегами пользователя в этой новости
        List<Tag> matchedTags = tagMatchingService.findMatchingTags(savedNews, userTags);
        
        if (!matchedTags.isEmpty()) {
            // Нашли совпадения - отправляем уведомление
            notificationService.sendNewsNotification(user, savedNews, matchedTags);
        }
    }
    
    /**
     * Анализирует новость и связывает с подходящими тегами
     */
    private void analyzeNewsTags(NewsItem news) {
        List<Tag> allTags = tagRepository.findAll();
        List<Tag> matchedTags = tagMatchingService.findMatchingTags(news, allTags);
        
        for (Tag tag : matchedTags) {
            newsItemRepository.addTagToNews(news.getId(), tag.getId());
        }
    }
}