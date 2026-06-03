package com.example.demo.infrastructure.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "processed_events", schema = "events_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedEventJpaEntity {

	@Id
	private UUID id; // Event ID

	@Column(name = "processed_at", nullable = false)
	private LocalDateTime processedAt;
}
