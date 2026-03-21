package com.example.stringservice.repository;

import com.example.stringservice.model.TgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TgUserRepository extends JpaRepository<TgUser, Long> {
    
    Optional<TgUser> findByChatId(Long chatId);
    
    List<TgUser> findByIsActiveTrue();
    
    @Modifying
    @Transactional
    @Query("UPDATE TgUser u SET u.isActive = :active WHERE u.chatId = :chatId")
    void setUserActive(@Param("chatId") Long chatId, @Param("active") Boolean active);
    
    @Query("SELECT u FROM TgUser u WHERE u.scanIntervalMinutes <= :interval AND u.isActive = true")
    List<TgUser> findUsersReadyForScan(@Param("interval") Integer interval);
    
    boolean existsByChatId(Long chatId);
}