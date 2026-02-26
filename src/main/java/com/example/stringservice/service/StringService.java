package com.example.stringservice.service;

import com.example.stringservice.model.StringEntity;
import com.example.stringservice.repository.StringRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StringService {
    
    private final StringRepository stringRepository;
    
    @Autowired
    public StringService(StringRepository stringRepository) {
        this.stringRepository = stringRepository;
    }
    
    @Transactional
    public StringEntity saveString(String data) {
        StringEntity stringEntity = new StringEntity();
        stringEntity.setData(data);
        stringEntity.setDataType(determineDataType(data));
        stringEntity.setReceivedTime(LocalDateTime.now());
        
        return stringRepository.save(stringEntity);
    }

    public Optional<StringEntity> getStringById(Long id) {
        return stringRepository.findById(id);
    }
    
    private String determineDataType(String data) {
        if (data == null || data.isEmpty()) {
            return "EMPTY";
        }
        
        if (data.matches("-?\\d+(\\.\\d+)?")) {
            return "NUMERIC";
        }
        
        if (data.trim().startsWith("{") || data.trim().startsWith("[")) {
            try {
                if ((data.trim().startsWith("{") && data.trim().endsWith("}")) ||
                    (data.trim().startsWith("[") && data.trim().endsWith("]"))) {
                    return "JSON";
                }
            } catch (Exception e) {}
        }
        
        if (data.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return "DATE";
        }
        
        return "TEXT";
    }
}