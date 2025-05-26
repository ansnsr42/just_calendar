import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Prüft alle 30 Sekunden, ob in 5 Minuten ein Termin beginnt,
 * und zeigt eine Desktop-Benachrichtigung an.
 */
public class ReminderService implements Runnable {

    private static final int LEAD_MINUTES = 5;          // Vorlaufzeit
    private static final int PERIOD_SEC   = 30;         // Prüfintervall

    private final DatabaseManager db;
    private final JFrame          parent;
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    /* zum Verhindern doppelter Popups */
    private final Set<Integer> remindedToday = new HashSet<>();
    private LocalDate currentDay = LocalDate.now();

    public ReminderService(DatabaseManager db, JFrame parent) {
        this.db     = db;
        this.parent = parent;
    }

    public void start() { exec.scheduleAtFixedRate(this, 0, PERIOD_SEC, TimeUnit.SECONDS); }

    @Override public void run() {
        try {
            /* Set täglich zurücksetzen */
            if (!LocalDate.now().equals(currentDay)) {
                remindedToday.clear();
                currentDay = LocalDate.now();
            }

            LocalDateTime now      = LocalDateTime.now();
            LocalDateTime windowTo = now.plusMinutes(LEAD_MINUTES);

            /* alle Termine heute & morgen, dann filtern */
            for (int delta = 0; delta <= 1; delta++) {
                var list = db.getEventsForDate(currentDay.plusDays(delta));
                for (Event ev : list) {
                    LocalDateTime evtTime = LocalDateTime.of(ev.getDate(), ev.getTime());
                    if (evtTime.isAfter(now) && !evtTime.isAfter(windowTo)
                        && remindedToday.add(ev.getId())) {
                        notifyUser(ev);
                    }
                }
            }
        } catch (Exception ex) {
            // Logging wäre schöner – für Demo reicht Out-Print
            ex.printStackTrace();
        }
    }

    /* Swing-sicheres Popup */
    private void notifyUser(Event ev) {
        String msg = String.format("In %d Minuten: %s (%s)",
                LEAD_MINUTES, ev.getTitle(), ev.getTime());
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(parent, msg, "Erinnerung",
                        JOptionPane.INFORMATION_MESSAGE));
    }
}

