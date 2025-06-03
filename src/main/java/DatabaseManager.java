import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

/**
 * Handles the SQLite connection and all CRUD / query logic.
 */
public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:calendar.db";
    private Connection conn;

    /* ---------- connection & one-time migrations ---------- */
    public void connect() throws SQLException {
        try { Class.forName("org.sqlite.JDBC"); }
        catch (ClassNotFoundException e) {
            throw new SQLException("SQLite driver missing!", e);
        }
        conn = DriverManager.getConnection(URL);

        // create table (first run) …
        String create = """
            CREATE TABLE IF NOT EXISTS events (
              id          INTEGER PRIMARY KEY AUTOINCREMENT,
              title       TEXT NOT NULL,
              date        TEXT NOT NULL,
              time        TEXT NOT NULL,
              description TEXT,
              recurrence  TEXT DEFAULT 'NONE',
              until       TEXT
            );
        """;
        try (Statement st = conn.createStatement()) { st.execute(create); }

        // … and add columns if the DB is older
        try (Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE events ADD COLUMN recurrence TEXT DEFAULT 'NONE'");
        } catch (SQLException ignored) {}
        try (Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE events ADD COLUMN until TEXT");
        } catch (SQLException ignored) {}
    }

    /* ---------- CRUD ---------- */
    public void addEvent(Event ev) throws SQLException {
        String q = """
            INSERT INTO events(title,date,time,description,recurrence,until)
            VALUES (?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
            bindEvent(ps, ev);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) ev.setId(rs.getInt(1));
            }
        }
    }

    public void updateEvent(Event ev) throws SQLException {
        String q = """
            UPDATE events
            SET title=?,date=?,time=?,description=?,recurrence=?,until=?
            WHERE id=?
        """;
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            bindEvent(ps, ev);
            ps.setInt(7, ev.getId());
            ps.executeUpdate();
        }
    }

    public void deleteEvent(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM events WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /* ---------- full-text search ---------- */
    public List<Event> searchEvents(String term) throws SQLException {
        String q = """
            SELECT * FROM events
            WHERE LOWER(title)       LIKE ?
               OR LOWER(description) LIKE ?
            ORDER BY date, time
        """;
        String pat = "%" + term.toLowerCase() + "%";
        List<Event> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setString(1, pat);
            ps.setString(2, pat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /* ---------- expand recurrences ---------- */
    public List<Event> getOccurrencesForDate(LocalDate d) throws SQLException {
        List<Event> list = new ArrayList<>();
        for (Event ev : getAllEvents())
            if (ev.occursOn(d)) list.add(ev);
        list.sort(Comparator.comparing(Event::getTime));
        return list;
    }

    /** Busy-day set for one month (used by the red underline). */
    public Set<LocalDate> getEventDatesOfMonth(YearMonth m) throws SQLException {
        Set<LocalDate> set = new HashSet<>();
        LocalDate first = m.atDay(1), last = m.atEndOfMonth();
        for (Event ev : getAllEvents()) {
            LocalDate cur = first;
            while (!cur.isAfter(last)) {
                if (ev.occursOn(cur)) set.add(cur);
                cur = cur.plusDays(1);
            }
        }
        return set;
    }

    /* ---------- helpers ---------- */
    private List<Event> getAllEvents() throws SQLException {
        List<Event> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM events");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        return new Event(
                rs.getInt("id"),
                rs.getString("title"),
                LocalDate.parse(rs.getString("date")),
                LocalTime.parse(rs.getString("time")),
                rs.getString("description"),
                RecurrenceType.fromDb(rs.getString("recurrence")),
                rs.getString("until") == null ? null : LocalDate.parse(rs.getString("until"))
        );
    }

    /** Bind an Event object to a prepared statement (common to insert & update). */
    private void bindEvent(PreparedStatement ps, Event ev) throws SQLException {
        ps.setString(1, ev.getTitle());
        ps.setString(2, ev.getDate().toString());
        ps.setString(3, ev.getTime().toString());
        ps.setString(4, ev.getDescription());
        ps.setString(5, ev.getRecurrence().name());
        ps.setString(6, ev.getUntil() == null ? null : ev.getUntil().toString());
    }

    public void close() throws SQLException { if (conn != null) conn.close(); }
}

