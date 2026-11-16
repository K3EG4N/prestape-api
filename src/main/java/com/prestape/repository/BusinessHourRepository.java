package com.prestape.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prestape.entity.BusinessHour;

public interface BusinessHourRepository extends JpaRepository<BusinessHour, Short> {
    List<BusinessHour> findAllByOrderByDayOfWeekAsc();
}
