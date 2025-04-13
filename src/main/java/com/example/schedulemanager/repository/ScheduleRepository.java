// src/main/java/com/example/schedulemanager/repository/ScheduleRepository.java
package com.example.schedulemanager.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.schedulemanager.model.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByDate(LocalDate date);
    List<Schedule> findByDateBetween(LocalDate startDate, LocalDate endDate);
}