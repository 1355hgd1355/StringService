package com.example.stringservice.controller;

import com.example.stringservice.model.StringEntity;
import com.example.stringservice.service.StringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/strings")
public class StringController {
    
    private final StringService stringService;
    
    @Autowired
    public StringController(StringService stringService) {
        this.stringService = stringService;
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> receiveString(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String data = request.get("data");
            if (data == null) {
                response.put("error", "Поле 'data' обязательно");
                return ResponseEntity.badRequest().body(response);
            }
            
            StringEntity savedEntity = stringService.saveString(data);
            
            response.put("id", savedEntity.getId());
            response.put("message", "Строка успешно сохранена");
            response.put("dataType", savedEntity.getDataType());
            response.put("receivedTime", savedEntity.getReceivedTime());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            response.put("error", "Ошибка при сохранении строки: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StringEntity> getString(@PathVariable Long id) {
       Optional<StringEntity> stringEntity = stringService.getStringById(id);
        return stringEntity.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}