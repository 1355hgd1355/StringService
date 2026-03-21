package com.example.stringservice.repository;

import com.example.stringservice.model.NewsSource;
import com.example.stringservice.model.TgUser;
import com.example.stringservice.model.UserSource;
import com.example.stringservice.model.UserSourceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserSourceRepository extends JpaRepository<UserSource, UserSourceId> {
    
    @Query("SELECT us.source FROM UserSource us WHERE us.user.id = :userId AND us.isEnabled = true")
    List<NewsSource> findEnabledSourcesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT us.user FROM UserSource us WHERE us.source.id = :sourceId AND us.isEnabled = true")
    List<TgUser> findEnabledUsersBySourceId(@Param("sourceId") Long sourceId);
    
    @Query("SELECT COUNT(us) > 0 FROM UserSource us WHERE us.user.id = :userId AND us.source.id = :sourceId AND us.isEnabled = true")
    boolean existsByUserIdAndSourceIdAndIsEnabledTrue(@Param("userId") Long userId, @Param("sourceId") Long sourceId);
    
    @Modifying
    @Transactional
    @Query("UPDATE UserSource us SET us.isEnabled = true WHERE us.user.id = :userId AND us.source.id = :sourceId")
    void enableSource(@Param("userId") Long userId, @Param("sourceId") Long sourceId);
    
    @Modifying
    @Transactional
    @Query("UPDATE UserSource us SET us.isEnabled = false WHERE us.user.id = :userId AND us.source.id = :sourceId")
    void disableSource(@Param("userId") Long userId, @Param("sourceId") Long sourceId);
    
    @Modifying
    @Transactional
    void deleteByUserIdAndSourceId(Long userId, Long sourceId);
    
    @Query("SELECT us.source FROM UserSource us WHERE us.user.id = :userId ORDER BY us.source.name")
    List<NewsSource> findAllSourcesByUserId(@Param("userId") Long userId);
}