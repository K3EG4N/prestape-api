package com.prestape.repository;

import com.prestape.entity.Person;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<Person> findByUserId(Long user_id);
}