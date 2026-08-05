package com.memorymap.memorymap.repository;

import com.memorymap.memorymap.model.Moment;
import com.memorymap.memorymap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Moment — the entity type: which table/class this repository manages.
// Long — the type of that entity's @Id field: Moment.id is a Long, so this must match it.
public interface MomentRepository extends JpaRepository<Moment, Long> {
    List<Moment> findByOwner(User owner);
}
