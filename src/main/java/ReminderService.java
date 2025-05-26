import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class ReminderService implements Runnable {

    private static final int LEAD_MIN   = 5;   // Vorlaufzeit
    private static final int PERIOD_SEC = 30;  // Prüfintervall

    private final DatabaseManager db;
    private final JFrame          parent;
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    private final Set<String> reminded = new HashSet<>(); // id-date Schlüssel

    public ReminderService(DatabaseManager db, JFrame parent) {
        this.db = db; this.parent = parent;
    }

    public void start() { exec.scheduleAtFixedRate(this, 0, PERIOD_SEC, TimeUnit.SECONDS); }

    @Override public void run() {
        try {
            LocalDateTime now   = LocalDateTime.now();
            LocalDateTime winTo = now.plusMinutes(LEAD_MIN);

            // Heute + Morgen prüfen
            for (int d = 0; d <= 1; d++) {
                LocalDate day = LocalDate.now().plusDays(d);
                for (Event ev : db.getOccurrencesForDate(day)) {
                    LocalDateTime occ = LocalDateTime.of(day, ev.getTime());
                    if (!occ.isAfter(now) || occ.isAfter(winTo)) continue;

                    String key = ev.getId() + "-" + day;
                    if (reminded.add(key)) notifyUser(ev, day);
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void notifyUser(Event ev, LocalDate day) {
        String msg = String.format("In %d Minuten: %s (%s) am %s",
                LEAD_MIN, ev.getTitle(), ev.getTime(), day);
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(parent, msg, "Erinnerung",
                                              JOptionPane.INFORMATION_MESSAGE));
    }
}

