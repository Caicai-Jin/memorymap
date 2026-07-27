package com.memorymap.memorymap.repository;

import com.memorymap.memorymap.model.Moment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MomentRepository extends JpaRepository<Moment, Long> {
}
