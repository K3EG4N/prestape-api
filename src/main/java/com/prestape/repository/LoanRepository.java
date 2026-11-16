package com.prestape.repository;

import com.prestape.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    boolean existsByCode(String code);

    @Override
    @EntityGraph(attributePaths = {"requester", "deliveredBy", "receivedBy"})
    Page<Loan> findAll(Specification<Loan> spec, Pageable pageable);
}
