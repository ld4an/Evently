package com.eventeanagementsystem.event_management_system.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    @Query("SELECT e, COUNT(a) FROM Event e JOIN e.attendees a GROUP BY e ORDER BY COUNT(a) DESC")
    List<Object[]> findEventsWithMostAttendees();
}
