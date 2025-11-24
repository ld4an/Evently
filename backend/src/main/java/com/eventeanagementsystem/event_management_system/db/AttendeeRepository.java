package com.eventeanagementsystem.event_management_system.db;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
public interface AttendeeRepository extends JpaRepository<Attendee, Integer> {
    List<Attendee> findByEvent(Event event);
    List<Attendee> findByEventIdAndStatus(Integer event_id, AttendanceStatus status);
    List<Attendee> findByUserEmail(String email);
    List<Attendee> findByUserId(Integer user_id);
    Optional<Attendee> findByUserEmailAndEventId(String email, Integer eventId);
    @Query("SELECT a.event, COUNT(a) FROM Attendee a GROUP BY a.event ORDER BY COUNT(a) DESC")
    List<Object[]> findEventsWithMostAttendees();
    List<Attendee> findByEventId(Integer event_Id);
}
