package com.cmpt276.studbuds.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface XpLogRepository extends JpaRepository<XpLog, Long> {

    List<XpLog> findByUser(User user);

    //
    @Query("SELECT x FROM XpLog x WHERE x.user = :user AND x.date >= :startDate ORDER BY x.date ASC")
    List<XpLog> findByUserAndDateAfter(@Param("user") User user, @Param("startDate") LocalDate startDate);
}
