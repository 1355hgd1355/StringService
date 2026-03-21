package com.example.stringservice.service;

import com.example.stringservice.model.NewsItem;
import com.example.stringservice.model.NewsSource;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class RssParserService {
    
    public List<NewsItem> parseRssFeed(NewsSource source, int maxItems) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            URL feedUrl = new URL(source.getUrl());
            SyndFeedInput input = new SyndFeedInput();
            
            SyndFeed feed;
            try (java.io.InputStream inputStream = feedUrl.openStream()) {
                feed = input.build(new XmlReader(inputStream));
            }

            List<SyndEntry> entries = feed.getEntries();
            
            // Берем только последние maxItems новостей
            int count = 0;
            for (SyndEntry entry : entries) {
                if (count >= maxItems) break;
                
                NewsItem news = new NewsItem(); // Используем конструктор без параметров
                news.setTitle(entry.getTitle());
                news.setLink(entry.getLink());
                
                // Описание (может быть HTML)
                if (entry.getDescription() != null) {
                    news.setContent(entry.getDescription().getValue());
                }
                
                // Дата публикации
                if (entry.getPublishedDate() != null) {
                    news.setPublishedAt(entry.getPublishedDate().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime());
                } else if (entry.getUpdatedDate() != null) {
                    news.setPublishedAt(entry.getUpdatedDate().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime());
                } else {
                    news.setPublishedAt(LocalDateTime.now());
                }
                
                news.setFoundAt(LocalDateTime.now());
                
                // GUID
                if (entry.getUri() != null) {
                    news.setGuid(entry.getUri());
                } else {
                    news.setGuid(entry.getLink());
                }
                
                newsItems.add(news);
                count++;
            }
            
        } catch (Exception e) {
            System.err.println("Ошибка парсинга RSS для " + source.getName() + ": " + e.getMessage());
        }
        
        return newsItems;
    }
}