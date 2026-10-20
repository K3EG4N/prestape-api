package com.prestape.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.prestape.entity.Menu;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Query("""
                select m from Menu m
                where m.deletedAt is null
                  and exists (
                      select 1
                      from User u
                      join u.roles r
                      where u.id = :userId
                        and r member of m.roles
                  )
                order by m.position
            """)
    List<Menu> findVisibleByUserId(Long userId);
}
