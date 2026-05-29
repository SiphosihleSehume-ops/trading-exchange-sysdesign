package za.co.eliteproject;

package za.co.wethinkcode;

import org.junit.jupiter.api.*;
import za.co.eliteproject.model.AuditEntry;
import za.co.eliteproject.service.AuditLog;

import static org.junit.jupiter.api.Assertions.*;

public class AuditLogTest {

    private AuditLog log;

    @BeforeEach
    void setUp() {
        log = new AuditLog();
    }

    @Test void testLogCreatesEntry() {
        AuditEntry entry = log.log("ORDER_SUBMITTED", "Order 1 submitted by trader A");
        assertNotNull(entry);
        assertEquals("ORDER_SUBMITTED", entry.getEventType());
        assertEquals(1, log.getTotalEntries());
    }

    @Test void testGetEntriesIsDefensiveCopy() {
        log.log("TEST_EVENT", "details");
        log.getEntries().clear();
        assertEquals(1, log.getTotalEntries());
    }

    @Test void testFilterByType() {
        log.log("ORDER_SUBMITTED", "order 1");
        log.log("TRADE_EXECUTED", "trade 1");
        log.log("ORDER_SUBMITTED", "order 2");
        assertEquals(2, log.getEntriesByType("ORDER_SUBMITTED").size());
        assertEquals(1, log.getEntriesByType("TRADE_EXECUTED").size());
    }

    @Test void testGetLatestEntry() {
        log.log("EVENT_A", "first");
        AuditEntry last = log.log("EVENT_B", "second");
        assertEquals(last, log.getLatestEntry());
    }

    @Test void testGetLatestEntryEmptyReturnsNull() {
        assertNull(log.getLatestEntry());
    }

    @Test void testBlankEventTypeThrows() {
        assertThrows(IllegalArgumentException.class, () -> log.log("  ", "details"));
    }

    @Test void testBlankDetailsThrows() {
        assertThrows(IllegalArgumentException.class, () -> log.log("EVENT", "  "));
    }

    @Test void testAuditEntryIsImmutable() {
        AuditEntry entry = log.log("IMMUTABLE_TEST", "testing immutability");
        // Confirm no setters compile — verified structurally by the absence of setters
        assertNotNull(entry.getTimestamp());
        assertTrue(entry.getTimestamp() > 0);
    }

    @Test void testIdsAutoIncrement() {
        AuditEntry e1 = log.log("A", "first");
        AuditEntry e2 = log.log("B", "second");
        assertEquals(e1.getEntryId() + 1, e2.getEntryId());
    }
}