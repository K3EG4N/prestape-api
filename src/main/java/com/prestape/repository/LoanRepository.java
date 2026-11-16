package com.prestape.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import com.prestape.entity.Loan;
import com.prestape.entity.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    boolean existsByCode(String code);

    long countByRequesterIdAndStatusIn(Long requesterId, Collection<LoanStatus> statuses);

    List<Loan> findAllByStatusAndScheduledPickupAtBefore(LoanStatus status, Instant before);

    @Override
    @EntityGraph(attributePaths = {"requester", "deliveredBy", "receivedBy"})
    Page<Loan> findAll(Specification<Loan> spec, Pageable pageable);
}
