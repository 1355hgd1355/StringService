package com.example.stringservice.repository;

import com.example.stringservice.model.NewsItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsItemRepository extends JpaRepository<NewsItem, Long> {
    
    Optional<NewsItem> findByLink(String link);
    
    Optional<NewsItem> findByGuid(String guid);
    
    List<NewsItem> findBySourceIdOrderByPublishedAtDesc(Long sourceId, Pageable pageable);
    
    @Query("SELECT n FROM NewsItem n WHERE n.publishedAt > :since ORDER BY n.publishedAt DESC")
    List<NewsItem> findRecentNews(@Param("since") LocalDateTime since, Pageable pageable);
    
    @Query("SELECT n FROM NewsItem n JOIN n.tags t WHERE t.id = :tagId ORDER BY n.publishedAt DESC")
    List<NewsItem> findByTagId(@Param("tagId") Long tagId, Pageable pageable);
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO news_tags (news_id, tag_id) VALUES (:newsId, :tagId)", nativeQuery = true)
    void addTagToNews(@Param("newsId") Long newsId, @Param("tagId") Long tagId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM news_tags WHERE news_id = :newsId AND tag_id = :tagId", nativeQuery = true)
    void removeTagFromNews(@Param("newsId") Long newsId, @Param("tagId") Long tagId);
    
    long countBySourceIdAndPublishedAtAfter(Long sourceId, LocalDateTime since);
    
    @Query("SELECT n FROM NewsItem n WHERE n.source.id = :sourceId AND n.publishedAt > :since ORDER BY n.publishedAt DESC")
    List<NewsItem> findNewsBySourceSince(@Param("sourceId") Long sourceId, 
                                          @Param("since") LocalDateTime since, 
                                          Pageable pageable);
}