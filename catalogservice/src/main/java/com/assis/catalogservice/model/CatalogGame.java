package com.assis.catalogservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "catalog_games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long rawgId;

    @Column(nullable = false)
    private String name;

    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime cachedAt;

    @PrePersist
    public void prePersist() {
        if (cachedAt == null) cachedAt = LocalDateTime.now();
    }
}
