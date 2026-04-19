package com.example.stringservice.repository;

import com.example.stringservice.model.Tag;
import com.example.stringservice.model.UserTag;
import com.example.stringservice.model.UserTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTagRepository extends JpaRepository<UserTag, UserTagId> {
    
    @Query("SELECT ut.tag FROM UserTag ut WHERE ut.user.id = :userId ORDER BY ut.tag.name")
    List<Tag> findTagsByUserId(@Param("userId") Long userId);

    Optional<UserTag> findByUserIdAndTagName(Long userId, String tagName);

    boolean existsByUserIdAndTagName(Long userId, String tagName);

    List<UserTag> findAllByUserId(Long userId);

    boolean existsByTagId(Long tagId);
    
    @Query("SELECT ut.user.id FROM UserTag ut WHERE ut.tag.id = :tagId")
    List<Long> findUserIdsByTagId(@Param("tagId") Long tagId);
    
    @Query("SELECT COUNT(ut) > 0 FROM UserTag ut WHERE ut.user.id = :userId AND ut.tag.id = :tagId")
    boolean existsByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM UserTag ut WHERE ut.user.id = :userId AND ut.tag.id = :tagId")
    void deleteByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);
    
    @Modifying
    @Transactional
    void deleteAllByUserId(Long userId);
    
    long countByUserId(Long userId);
}