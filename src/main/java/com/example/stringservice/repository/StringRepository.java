package com.example.stringservice.repository;

import com.example.stringservice.model.StringEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StringRepository extends JpaRepository<StringEntity, Long> {
}