package com.pr0maxx.bankapplication.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.pr0maxx.bankapplication.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
