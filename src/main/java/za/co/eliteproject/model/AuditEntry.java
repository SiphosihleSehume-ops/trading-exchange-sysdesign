package za.co.eliteproject.model;

/**
 * Immutable record of a single audit event.
 */
public final class AuditEntry {

    private final int entryId;
    private final String eventType;
    private final String details;
    private final long timestamp;

    public AuditEntry(int entryId, String eventType, String details) {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("Event type cannot be blank");
        }
        if (details == null || details.isBlank()) {
            throw new IllegalArgumentException("Details cannot be blank");
        }
        this.entryId = entryId;
        this.eventType = eventType;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
    }

    public int getEntryId() {
        return entryId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDetails() {
        return details;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "AuditEntry{" +
                "entryId=" + entryId +
                ", eventType='" + eventType + '\'' +
                ", details='" + details + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
