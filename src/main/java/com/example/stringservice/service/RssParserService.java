package com.example.stringservice.service;

import com.example.stringservice.model.NewsItem;
import com.example.stringservice.model.NewsSource;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Service;

import java.net.URI;
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
            // Используем URI вместо прямого создания URL
            URI uri = new URI(source.getUrl());
            URL feedUrl = uri.toURL();
            
            SyndFeedInput input = new SyndFeedInput();
            
            try (java.io.InputStream inputStream = feedUrl.openStream()) {
                SyndFeed feed = input.build(new XmlReader(inputStream));
                
                List<SyndEntry> entries = feed.getEntries();
                
                int count = 0;
                for (SyndEntry entry : entries) {
                    if (count >= maxItems) break;
                    
                    NewsItem news = new NewsItem();
                    news.setTitle(entry.getTitle());
                    news.setLink(entry.getLink());
                    
                    if (entry.getDescription() != null) {
                        news.setContent(entry.getDescription().getValue());
                    }
                    
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
                    
                    if (entry.getUri() != null) {
                        news.setGuid(entry.getUri());
                    } else {
                        news.setGuid(entry.getLink());
                    }
                    
                    newsItems.add(news);
                    count++;
                }
            }
            
        } catch (Exception e) {
            System.err.println("Ошибка парсинга RSS для " + source.getName() + ": " + e.getMessage());
        }
        
        return newsItems;
    }
}