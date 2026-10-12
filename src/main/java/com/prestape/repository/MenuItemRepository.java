package com.prestape.repository;

import com.prestape.dto.response.MenuByUserIdResponse;
import com.prestape.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    @Query("""
                select mi from MenuItem mi
                join mi.menu m
                where mi.deletedAt is null
                  and m.deletedAt is null
                  and exists (
                      select 1
                      from User u
                      join u.roles r, MenuItemRole mir
                      where u.id = :userId
                        and mir.role = r
                        and mir.menuItem = mi
                        and mir.deletedAt is null
                  )
                order by m.position, mi.position
            """)
    List<MenuByUserIdResponse> findVisibleByUserId(Long userId);
}