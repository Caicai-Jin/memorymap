package com.memorymap.memorymap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToMany(mappedBy = "moment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> media;

    //  @ElementCollection — tells JPA "this isn't a relationship to another entity,
    //  it's just a list of plain values (strings) that belong to this Moment"
    @ElementCollection
    @CollectionTable(name = "moment_moods", joinColumns = @JoinColumn(name = "moment_id"))
    @Column(name = "mood")
    private List<String> mood;
}
