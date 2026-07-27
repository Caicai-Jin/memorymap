package com.memorymap.memorymap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "moments")
@Getter
@Setter
public class Moment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = true)
    private String content;
    private String mood;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
