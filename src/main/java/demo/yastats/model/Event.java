package demo.yastats.model;

import java.time.LocalDateTime;

/**
 * Model to store event information. LocalDateTime in our case
 */
public class Event {

    private LocalDateTime localDateTime;

    public Event(LocalDateTime localDateTime) throws NullPointerException {
        this.localDateTime = localDateTime;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    @Override
    public String toString() {
        return "Date and time: " + localDateTime.toString();
    }
}
