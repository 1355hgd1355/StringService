package com.example.stringservice.repository;

import com.example.stringservice.model.NewsSource;
import com.example.stringservice.model.UserSource;
import com.example.stringservice.model.UserSourceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSourceRepository extends JpaRepository<UserSource, UserSourceId> {
    
    @Query("SELECT us.source FROM UserSource us WHERE us.user.id = :userId AND us.isEnabled = true")
    List<NewsSource> findEnabledSourcesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT us FROM UserSource us WHERE us.user.id = :userId AND us.source.id = :sourceId")
    Optional<UserSource> findByUserIdAndSourceId(@Param("userId") Long userId, @Param("sourceId") Long sourceId);
    
    @Query("SELECT COUNT(us) > 0 FROM UserSource us WHERE us.user.id = :userId AND us.source.id = :sourceId AND us.isEnabled = true")
    boolean existsByUserIdAndSourceIdAndIsEnabledTrue(@Param("userId") Long userId, @Param("sourceId") Long sourceId);
    
    @Modifying
    @Transactional
    default void enableSource(Long userId, Long sourceId) {
        Optional<UserSource> existing = findByUserIdAndSourceId(userId, sourceId);
        
        if (existing.isPresent()) {
            UserSource userSource = existing.get();
            userSource.setIsEnabled(true);
            save(userSource);
        } else {
            // Используем native query вместо создания объекта
            enableSourceNative(userId, sourceId);
        }
    }
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_sources (user_id, source_id, is_enabled) VALUES (:userId, :sourceId, true) " +
                   "ON CONFLICT (user_id, source_id) DO UPDATE SET is_enabled = true", nativeQuery = true)
    void enableSourceNative(@Param("userId") Long userId, @Param("sourceId") Long sourceId);
    
    @Modifying
    @Transactional
    default void disableSource(Long userId, Long sourceId) {
        Optional<UserSource> existing = findByUserIdAndSourceId(userId, sourceId);
        
        if (existing.isPresent()) {
            UserSource userSource = existing.get();
            userSource.setIsEnabled(false);
            save(userSource);
        }
    }
    
    @Modifying
    @Transactional
    @Query("DELETE FROM UserSource us WHERE us.user.id = :userId AND us.source.id = :sourceId")
    void deleteByUserIdAndSourceId(@Param("userId") Long userId, @Param("sourceId") Long sourceId);
    
    @Query("SELECT us.source FROM UserSource us WHERE us.user.id = :userId ORDER BY us.source.name")
    List<NewsSource> findAllSourcesByUserId(@Param("userId") Long userId);
}