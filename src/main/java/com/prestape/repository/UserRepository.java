package com.prestape.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.prestape.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    @EntityGraph(attributePaths = "roles")
    List<User> findTop5ByOrderByCreatedAtDesc();

    @Query(value = """
            SELECT count(*) FROM users
            WHERE created_at < :at AND (deleted_at IS NULL OR deleted_at >= :at)
            """, nativeQuery = true)
    long countExistingAt(@Param("at") Instant at);
}
