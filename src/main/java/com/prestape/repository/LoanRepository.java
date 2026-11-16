package com.prestape.repository;

import java.util.List;

import com.prestape.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    boolean existsByCode(String code);

    @Override
    @EntityGraph(attributePaths = {"requester", "deliveredBy", "receivedBy", "registeredBy", "kit"})
    Page<Loan> findAll(Specification<Loan> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"requester", "deliveredBy", "receivedBy", "registeredBy", "kit"})
    List<Loan> findAll(Specification<Loan> spec, Sort sort);
}
