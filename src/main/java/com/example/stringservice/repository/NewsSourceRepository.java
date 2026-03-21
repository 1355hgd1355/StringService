package com.example.stringservice.repository;

import com.example.stringservice.model.NewsSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsSourceRepository extends JpaRepository<NewsSource, Long> {
    
    Optional<NewsSource> findByNameIgnoreCase(String name);
    
    List<NewsSource> findByIsActiveTrue();
    
    @Modifying
    @Transactional
    @Query("UPDATE NewsSource s SET s.isActive = :active WHERE s.id = :id")
    void setSourceActive(@Param("id") Long id, @Param("active") Boolean active);
    
    @Query("SELECT s FROM NewsSource s WHERE s.isActive = true ORDER BY s.name")
    List<NewsSource> findAllActiveSorted();
    
    Optional<NewsSource> findByUrl(String url);
}