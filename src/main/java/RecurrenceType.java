public enum RecurrenceType {
    NONE        ("Einmalig"),
    DAILY       ("Täglich"),
    WEEKLY      ("Wöchentlich"),
    MONTHLY     ("Monatlich"),
    YEARLY      ("Jährlich");

    private final String display;
    RecurrenceType(String d) { this.display = d; }

    @Override public String toString() { return display; }

    public static RecurrenceType fromDisplay(String d) {
        for (RecurrenceType t : values())
            if (t.display.equals(d)) return t;
        return NONE;
    }

    public static RecurrenceType fromDb(String s) {
        try { return valueOf(s); } catch (Exception e) { return NONE; }
    }
}

