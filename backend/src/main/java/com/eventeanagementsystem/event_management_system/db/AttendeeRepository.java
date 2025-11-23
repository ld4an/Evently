package com.eventeanagementsystem.event_management_system.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AttendeeRepository extends JpaRepository<Attendee, Integer> {
    List<Attendee> findByEvent(Event event);
    List<Attendee> findByEventIdAndStatus(Integer event_id, AttendanceStatus status);
    List<Attendee> findByUserEmail(String email);
    List<Attendee> findByUserId(Integer user_id);
    @Query("SELECT a.event, COUNT(a) FROM Attendee a GROUP BY a.event ORDER BY COUNT(a) DESC")
    List<Object[]> findEventsWithMostAttendees();

    List<Attendee> findByEventId(Integer event_Id);
}
