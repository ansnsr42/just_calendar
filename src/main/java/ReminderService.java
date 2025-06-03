import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Periodically checks upcoming events and shows pop-up reminders.
 */
public class ReminderService implements Runnable {

    private static final int LEAD_MIN   = 5;   // minutes before start
    private static final int PERIOD_SEC = 30;  // polling interval

    private final DatabaseManager db;
    private final JFrame          parent;
    private final ScheduledExecutorService exec =
            Executors.newSingleThreadScheduledExecutor();

    /** Keeps track of events that have already triggered a reminder (id-date key). */
    private final Set<String> reminded = new HashSet<>();

    public ReminderService(DatabaseManager db, JFrame parent) {
        this.db = db;
        this.parent = parent;
    }

    /** Starts the background scheduler. */
    public void start() {
        exec.scheduleAtFixedRate(this, 0, PERIOD_SEC, TimeUnit.SECONDS);
    }

    @Override public void run() {
        try {
            LocalDateTime now   = LocalDateTime.now();
            LocalDateTime winTo = now.plusMinutes(LEAD_MIN);

            // look at today and tomorrow
            for (int d = 0; d <= 1; d++) {
                LocalDate day = LocalDate.now().plusDays(d);
                for (Event ev : db.getOccurrencesForDate(day)) {
                    LocalDateTime occ = LocalDateTime.of(day, ev.getTime());

                    // only within the lead window
                    if (!occ.isAfter(now) || occ.isAfter(winTo)) continue;

                    String key = ev.getId() + "-" + day;
                    if (reminded.add(key)) notifyUser(ev, day);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Show a Swing pop-up on the EDT. */
    private void notifyUser(Event ev, LocalDate day) {
        String msg = String.format("In %d minutes: %s (%s) on %s",
                                   LEAD_MIN, ev.getTitle(), ev.getTime(), day);
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(parent, msg,
                        "Reminder", JOptionPane.INFORMATION_MESSAGE));
    }
}

