package com.memorymap.memorymap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "medias")
@Getter
@Setter
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MediaType type;

    private String url;

    // Cloudinary's id for this file, needed to delete it from Cloudinary storage
    // when the media is removed. Null for any media uploaded before this field existed.
    private String publicId;

    @ManyToOne
    @JoinColumn(name = "moment_id", nullable = false)
    private Moment moment;
}
