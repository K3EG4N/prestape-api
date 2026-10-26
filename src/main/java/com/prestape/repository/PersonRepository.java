package com.prestape.repository;

import com.prestape.entity.Person;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<Person> findByUserId(Long user_id);

    List<Person> findByUserIdInAndDeletedAtIsNull(Collection<Long> userIds);

    boolean existsByCode(String code);

    boolean existsByDocumentNumberAndDeletedAtIsNull(String documentNumber);

    boolean existsByDocumentNumberAndDeletedAtIsNullAndIdNot(String documentNumber, Long id);

    Optional<Person> findByUserIdAndDeletedAtIsNull(Long userId);
}