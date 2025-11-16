package com.eventeanagementsystem.event_management_system.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AttendeeRepository extends JpaRepository<Attendee, Integer> {
    List<Attendee> findByEvent(Event event);

    @Query("SELECT a.event, COUNT(a) FROM Attendee a GROUP BY a.event ORDER BY COUNT(a) DESC")
    List<Object[]> findEventsWithMostAttendees();
}
