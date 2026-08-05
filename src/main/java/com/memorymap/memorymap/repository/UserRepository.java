package com.memorymap.memorymap.repository;

import com.memorymap.memorymap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //Optional<User> is the return type because a lookup by email might not find anyone
    Optional<User> findByEmail(String email);

}
