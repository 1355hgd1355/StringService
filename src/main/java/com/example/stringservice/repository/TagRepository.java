package com.example.stringservice.repository;

import com.example.stringservice.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    Optional<Tag> findByNameIgnoreCase(String name);
    
    List<Tag> findByNameContainingIgnoreCase(String name);
    
    // ПРАВИЛЬНЫЙ СПОСОБ - через связь userTags
    @Query("SELECT t FROM Tag t JOIN t.userTags ut WHERE ut.user.id = :userId ORDER BY t.name")
    List<Tag> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT t FROM Tag t WHERE t.name IN :names")
    List<Tag> findByNames(@Param("names") List<String> names);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Tag t WHERE t.name = :name")
    void deleteByName(@Param("name") String name);
    
    boolean existsByNameIgnoreCase(String name);
}