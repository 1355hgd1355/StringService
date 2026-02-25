package com.example.stringservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "strings")
public class StringEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "data", nullable = false, columnDefinition = "TEXT")
    private String data;
    
    @Column(name = "data_type", nullable = false)
    private String dataType;
    
    @Column(name = "received_time", nullable = false)
    private LocalDateTime receivedTime;
    
    // Конструкторы
    public StringEntity() {} //Пустой конструктор, если выдаст ошибку, то надо будет попробовать удалить его.
    
    public StringEntity(String data, String dataType, LocalDateTime receivedTime) {
        this.data = data;
        this.dataType = dataType;
        this.receivedTime = receivedTime;
    }
    
    // Геттеры и сеттеры
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getData() {return data;}
    public void setData(String data) {this.data = data;}
    public String getDataType() {return dataType;}
    public void setDataType(String dataType) {this.dataType = dataType;}
    public LocalDateTime getReceivedTime() {return receivedTime;}
    public void setReceivedTime(LocalDateTime receivedTime) {this.receivedTime = receivedTime;}
}