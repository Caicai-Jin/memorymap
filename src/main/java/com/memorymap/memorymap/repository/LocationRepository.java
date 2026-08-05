package com.memorymap.memorymap.repository;

import com.memorymap.memorymap.model.Location;
import com.memorymap.memorymap.model.LocationType;
import com.memorymap.memorymap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByOwnerAndType(User owner, LocationType type);
}
