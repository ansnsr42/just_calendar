import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

/**
 * SQLite-Zugriff: Verbindung, CRUD, Busy-Day-Abfrage.
 */
public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:calendar.db";
    private Connection conn;

    /* ---------- Verbindung ---------- */
    public void connect() throws SQLException {
        try { Class.forName("org.sqlite.JDBC"); }
        catch (ClassNotFoundException e) { throw new SQLException("SQLite-Treiber fehlt!", e); }

        conn = DriverManager.getConnection(URL);

        String create = """
            CREATE TABLE IF NOT EXISTS events (
              id          INTEGER PRIMARY KEY AUTOINCREMENT,
              title       TEXT NOT NULL,
              date        TEXT NOT NULL,
              time        TEXT NOT NULL,
              description TEXT
            );
        """;
        try (Statement st = conn.createStatement()) { st.execute(create); }
    }

    /* ---------- CRUD ---------- */
    public void addEvent(Event ev) throws SQLException {
        String q = "INSERT INTO events(title,date,time,description) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ev.getTitle());
            ps.setString(2, ev.getDate().toString());
            ps.setString(3, ev.getTime().toString());
            ps.setString(4, ev.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) ev.setId(rs.getInt(1)); }
        }
    }

    public void updateEvent(Event ev) throws SQLException {
        String sql = """
            UPDATE events
            SET title = ?, date = ?, time = ?, description = ?
            WHERE id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ev.getTitle());
            ps.setString(2, ev.getDate().toString());
            ps.setString(3, ev.getTime().toString());
            ps.setString(4, ev.getDescription());
            ps.setInt   (5, ev.getId());
            ps.executeUpdate();
        }
    }

    public List<Event> getEventsForDate(LocalDate d) throws SQLException {
        String q = "SELECT * FROM events WHERE date = ? ORDER BY time";
        List<Event> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setString(1, d.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Event(
                            rs.getInt("id"),
                            rs.getString("title"),
                            LocalDate.parse(rs.getString("date")),
                            LocalTime.parse(rs.getString("time")),
                            rs.getString("description")));
                }
            }
        }
        return list;
    }

    public void deleteEvent(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM events WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    /* ---------- Busy-Days eines Monats ---------- */
    public Set<LocalDate> getEventDatesOfMonth(YearMonth m) throws SQLException {
        LocalDate first = m.atDay(1);
        LocalDate last  = m.atEndOfMonth();
        String sql = "SELECT DISTINCT date FROM events WHERE date BETWEEN ? AND ?";
        Set<LocalDate> set = new HashSet<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, first.toString());
            ps.setString(2, last.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(LocalDate.parse(rs.getString(1)));
            }
        }
        return set;
    }

    public void close() throws SQLException { if (conn != null) conn.close(); }
}

