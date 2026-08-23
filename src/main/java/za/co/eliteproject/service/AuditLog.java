package za.co.eliteproject.service;

import za.co.eliteproject.model.AuditEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe, append-only log of AuditEntry records.
 */
public class AuditLog {

    private final List<AuditEntry> entries;
    private int nextId;
    private final ReentrantLock lock = new ReentrantLock();

    public AuditLog() {
        this.entries = Collections.synchronizedList(new ArrayList<>());
        this.nextId = 1;
    }

    /**
     * Creates, stores, and returns a new AuditEntry.
     */
    public AuditEntry log(String eventType, String details) {
        lock.lock();
        try {
            AuditEntry entry = new AuditEntry(nextId, eventType, details);
            nextId++;
            entries.add(entry);
            return entry;
        } finally {
            lock.unlock();
        }
    }

    public List<AuditEntry> getEntries() {
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    public List<AuditEntry> getEntriesByType(String eventType) {
        List<AuditEntry> result = new ArrayList<>();
        synchronized (entries) {
            for (AuditEntry entry : entries) {
                if (entry.getEventType().equals(eventType)) {
                    result.add(entry);
                }
            }
        }
        return result;
    }

    public int getTotalEntries() {
        synchronized (entries) {
            return entries.size();
        }
    }

    public AuditEntry getLatestEntry() {
        synchronized (entries) {
            if (entries.isEmpty()) {
                return null;
            }
            return entries.get(entries.size() - 1);
        }
    }
}
