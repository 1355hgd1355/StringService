package com.example.stringservice.repository;

import com.example.stringservice.model.SentNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SentNotificationRepository extends JpaRepository<SentNotification, Long> {
    
    @Query("SELECT COUNT(sn) > 0 FROM SentNotification sn WHERE sn.user.id = :userId AND sn.news.id = :newsId")
    boolean existsByUserIdAndNewsId(@Param("userId") Long userId, @Param("newsId") Long newsId);
    
    @Query("SELECT sn FROM SentNotification sn WHERE sn.user.id = :userId ORDER BY sn.sentAt DESC")
    List<SentNotification> findByUserIdOrderBySentAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT sn FROM SentNotification sn WHERE sn.sentAt < :before")
    List<SentNotification> findBySentAtBefore(@Param("before") LocalDateTime before);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM SentNotification sn WHERE sn.sentAt < :before")
    void deleteOldNotifications(@Param("before") LocalDateTime before);
    
    long countByUserIdAndSentAtAfter(Long userId, LocalDateTime since);
}