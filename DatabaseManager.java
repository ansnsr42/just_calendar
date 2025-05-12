import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Verwaltet die Verbindung zu SQLite und führt CRUD‑Operationen
 * für die Tabelle „events“ aus.
 *
 * Tabelle:
 *   id          INTEGER  PRIMARY KEY AUTOINCREMENT
 *   title       TEXT     NOT NULL
 *   date        TEXT     NOT NULL   -- Format YYYY‑MM‑DD
 *   time        TEXT     NOT NULL   -- Format HH:MM
 *   description TEXT
 */
public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:calendar.db";
    private Connection conn;

    /* ---------- Verbindung herstellen / Tabelle anlegen ---------- */
    public void connect() throws SQLException {

        // Treiber explizit laden, falls IDE den Class‑Path nicht korrekt setzt
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite‑Treiber nicht im Class‑Path!", e);
        }

        conn = DriverManager.getConnection(URL);

        String createSql = """
            CREATE TABLE IF NOT EXISTS events (
              id          INTEGER PRIMARY KEY AUTOINCREMENT,
              title       TEXT NOT NULL,
              date        TEXT NOT NULL,
              time        TEXT NOT NULL,
              description TEXT
            );
            """;
        try (Statement st = conn.createStatement()) {
            st.execute(createSql);
        }
    }

    /* ---------- Termin hinzufügen ---------- */
    public void addEvent(Event ev) throws SQLException {
        String sql = "INSERT INTO events(title, date, time, description) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ev.getTitle());
            ps.setString(2, ev.getDate().toString());
            ps.setString(3, ev.getTime().toString());
            ps.setString(4, ev.getDescription());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) ev.setId(rs.getInt(1));
            }
        }
    }

    /* ---------- Termine für ein Datum abrufen ---------- */
    public List<Event> getEventsForDate(LocalDate date) throws SQLException {
        String sql = "SELECT * FROM events WHERE date = ? ORDER BY time";
        List<Event> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
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

    /* ---------- Termin löschen ---------- */
    public void deleteEvent(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM events WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /* ---------- Verbindung schließen ---------- */
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) conn.close();
    }
}