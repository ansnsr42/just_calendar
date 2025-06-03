/**
 * Enumeration of possible recurrence patterns.
 */
public enum RecurrenceType {
    NONE    ("One-time"),
    DAILY   ("Daily"),
    WEEKLY  ("Weekly"),
    MONTHLY ("Monthly"),
    YEARLY  ("Yearly");

    private final String display;
    RecurrenceType(String d) { this.display = d; }

    /** Human-readable label (used in combo boxes, etc.). */
    @Override public String toString() { return display; }

    /** Lookup by display string (UI → enum). */
    public static RecurrenceType fromDisplay(String d) {
        for (RecurrenceType t : values())
            if (t.display.equals(d)) return t;
        return NONE;
    }

    /** Lookup by database value (column text → enum). */
    public static RecurrenceType fromDb(String s) {
        try { return valueOf(s); }
        catch (Exception ignored) { return NONE; }
    }
}

