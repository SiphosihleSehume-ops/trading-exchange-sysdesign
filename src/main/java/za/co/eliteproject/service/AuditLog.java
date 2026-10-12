package za.co.eliteproject.service;

import za.co.eliteproject.model.AuditEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe, append-only log of AuditEntry records.
 */
public class AuditLog {

    private final List<AuditEntry> entries = new ArrayList<>();
    private int nextId = 1;

    /** Creates, stores, and returns a new AuditEntry. */
    public synchronized AuditEntry log(String eventType, String details) {
        AuditEntry entry = new AuditEntry(nextId, eventType, details);
        nextId++; // only advances if the entry was valid
        entries.add(entry);
        return entry;
    }

    public synchronized List<AuditEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public synchronized List<AuditEntry> getEntriesByType(String eventType) {
        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry entry : entries) {
            if (entry.getEventType().equals(eventType)) {
                result.add(entry);
            }
        }
        return result;
    }

    public synchronized int getTotalEntries() {
        return entries.size();
    }

    /** @return the most recently added entry, or null if the log is empty. */
    public synchronized AuditEntry getLatestEntry() {
        return entries.isEmpty() ? null : entries.get(entries.size() - 1);
    }
}
