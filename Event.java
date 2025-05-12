import java.time.LocalDate;
import java.time.LocalTime;

public class Event {

    private int id;                    // 0 → noch nicht in DB
    private String title;
    private LocalDate date;
    private LocalTime time;
    private String description;

    public Event(int id, String title, LocalDate date, LocalTime time, String description) {
        this.id          = id;
        this.title       = title;
        this.date        = date;
        this.time        = time;
        this.description = description;
    }

    // Overload ohne ID (für INSERT)
    public Event(String title, LocalDate date, LocalTime time, String description) {
        this(0, title, date, time, description);
    }

    /* ---------- Getter ---------- */
    public int getId() { return id; }
    public String getTitle() { return title; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public String getDescription() { return description; }

    /* ---------- Setter für ID nach INSERT ---------- */
    public void setId(int id) { this.id = id; }

    @Override public String toString() {
        return String.format("[%s %s] %s — %s", date, time, title, description);
    }
}

