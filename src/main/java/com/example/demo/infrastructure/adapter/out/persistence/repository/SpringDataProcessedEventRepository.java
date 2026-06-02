package com.example.demo.infrastructure.adapter.out.persistence.repository;

import com.example.demo.infrastructure.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataProcessedEventRepository extends JpaRepository<ProcessedEventJpaEntity, UUID> {
}
