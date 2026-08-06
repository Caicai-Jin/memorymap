package com.memorymap.memorymap.repository;

import com.memorymap.memorymap.model.Media;
import com.memorymap.memorymap.model.Moment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByMoment(Moment moment);
}
