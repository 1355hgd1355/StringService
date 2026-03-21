package com.example.stringservice.service;

import com.example.stringservice.model.NewsItem;
import com.example.stringservice.model.Tag;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TagMatchingService {
    
    public List<Tag> findMatchingTags(NewsItem news, List<Tag> tags) {
        List<Tag> matchedTags = new ArrayList<>();
        
        String searchText = (news.getTitle() + " " + news.getContent()).toLowerCase();
        
        for (Tag tag : tags) {
            if (isTagInText(tag.getName().toLowerCase(), searchText)) {
                matchedTags.add(tag);
            }
        }
        
        return matchedTags;
    }
    
    private boolean isTagInText(String tag, String text) {
        // Простой поиск по слову
        // В реальном проекте можно использовать более сложные алгоритмы
        
        // Экранируем специальные символы regex
        String escapedTag = Pattern.quote(tag);
        
        // Ищем как отдельное слово
        Pattern pattern = Pattern.compile("\\b" + escapedTag + "\\b", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(text).find();
    }
}