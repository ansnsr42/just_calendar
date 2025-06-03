import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Plain event data plus basic recurrence logic.
 */
public class Event {

    private int            id;          // 0 → not yet stored in DB
    private String         title;
    private LocalDate      date;        // start date
    private LocalTime      time;
    private String         description;
    private RecurrenceType recurrence;  // NONE, DAILY, …
    private LocalDate      until;       // optional end date (null → unlimited)

    /* ---------- constructors ---------- */
    public Event(int id, String title, LocalDate date, LocalTime time,
                 String description, RecurrenceType recurrence, LocalDate until) {
        this.id = id; this.title = title; this.date = date; this.time = time;
        this.description = description;  this.recurrence = recurrence; this.until = until;
    }
    public Event(String title, LocalDate date, LocalTime time,
                 String description, RecurrenceType recurrence, LocalDate until) {
        this(0, title, date, time, description, recurrence, until);
    }

    /* ---------- getters ---------- */
    public int            getId()          { return id; }
    public String         getTitle()       { return title; }
    public LocalDate      getDate()        { return date; }
    public LocalTime      getTime()        { return time; }
    public String         getDescription() { return description; }
    public RecurrenceType getRecurrence()  { return recurrence; }
    public LocalDate      getUntil()       { return until; }

    /* ---------- setters ---------- */
    public void setId(int id)                    { this.id = id; }
    public void setTitle(String t)               { this.title = t; }
    public void setDate(LocalDate d)             { this.date = d; }
    public void setTime(LocalTime t)             { this.time = t; }
    public void setDescription(String d)         { this.description = d; }
    public void setRecurrence(RecurrenceType r)  { this.recurrence = r; }
    public void setUntil(LocalDate u)            { this.until = u; }

    /* ---------- recurrence logic ---------- */
    public boolean occursOn(LocalDate target) {
        if (target.isBefore(date)) return false;
        if (until != null && target.isAfter(until)) return false;

        switch (recurrence) {
            case DAILY -> {
                long days = ChronoUnit.DAYS.between(date, target);
                return days % 1 == 0;
            }
            case WEEKLY -> {
                long w = ChronoUnit.WEEKS.between(date, target);
                return w % 1 == 0;
            }
            case MONTHLY -> {
                long m = ChronoUnit.MONTHS.between(date.withDayOfMonth(1),
                                                   target.withDayOfMonth(1));
                return m % 1 == 0 && date.getDayOfMonth() == target.getDayOfMonth();
            }
            case YEARLY -> {
                return date.getMonth() == target.getMonth()
                        && date.getDayOfMonth() == target.getDayOfMonth();
            }
            default -> { return target.equals(date); }   // NONE
        }
    }

    @Override
    public String toString() {
        String recur = (recurrence == RecurrenceType.NONE) ? ""
                     : " [" + recurrence
                       + (until != null ? " until " + until : "") + "]";
        return String.format("[%s %s] %s — %s%s",
                date, time, title, description, recur);
    }
}

