-- Keep DB constraint in sync with AttendanceStatus enum.
-- Older DBs may have a CHECK constraint that doesn't include new values (e.g. BANNED).
ALTER TABLE attendee DROP CONSTRAINT IF EXISTS attendee_status_check;
ALTER TABLE attendee
  ADD CONSTRAINT attendee_status_check
  CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELED', 'BANNED'));
