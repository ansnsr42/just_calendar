import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class CalendarApp {

    /* ---------------- Feldvariablen ---------------- */
    private static DatabaseManager db;
    private static JTextArea dayArea;
    private static LocalDate selectedDate;

    public static void main(String[] args) {
        /* Modernes Look‑and‑Feel */
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            db = new DatabaseManager();
            try { db.connect(); } catch (SQLException ex) { showError("DB‑Verbindung fehlgeschlagen:\n" + ex); }

            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Java‑Kalender");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        /* ----------- rechter Bereich: Tagesansicht + Button ----------- */
        dayArea = new JTextArea("Wähle links ein Datum…");
        dayArea.setEditable(false);
        dayArea.setLineWrap(true);

        JButton addBtn = new JButton("Neuer Termin");
        addBtn.addActionListener(e -> quickAddDialog());

        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setPreferredSize(new Dimension(280, 0));
        right.add(new JLabel("Tagesansicht", SwingConstants.CENTER), BorderLayout.NORTH);
        right.add(new JScrollPane(dayArea), BorderLayout.CENTER);
        right.add(addBtn, BorderLayout.SOUTH);

        /* ----------- linker Bereich: Monatsübersicht ----------- */
        CalendarView monthView = new CalendarView(date -> {
            selectedDate = date;
            refreshDayView(date);
        });

        /* ----------- zusammensetzen ----------- */
        frame.add(monthView, BorderLayout.CENTER);
        frame.add(right,     BorderLayout.EAST);
        frame.setLocationRelativeTo(null);

        /* DB sauber schließen, wenn Fenster zu */
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try { db.close(); } catch (SQLException ignored) {}
            }
        });

        frame.setVisible(true);
    }

    /* ---------- Hilfsfunktionen ---------- */

    private static void refreshDayView(LocalDate date) {
        try {
            var events = db.getEventsForDate(date);
            if (events.isEmpty()) {
                dayArea.setText("Keine Termine am " + date + ".");
            } else {
                var sb = new StringBuilder("Termine am ").append(date).append(":\n\n");
                events.forEach(ev -> sb.append(ev).append("\n"));
                dayArea.setText(sb.toString());
            }
        } catch (SQLException ex) {
            showError("Fehler beim Laden der Termine:\n" + ex.getMessage());
        }
    }

    /** Schneller Eingabedialog (ohne eigenes JDialog‑Formular) */
    private static void quickAddDialog() {
        if (selectedDate == null) {
            showError("Bitte zuerst ein Datum in der Monatsansicht anklicken.");
            return;
        }
        JTextField titleF = new JTextField();
        JTextField timeF  = new JTextField("HH:MM");
        JTextField descF  = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Titel:"));        panel.add(titleF);
        panel.add(new JLabel("Uhrzeit (HH:MM):")); panel.add(timeF);
        panel.add(new JLabel("Beschreibung:")); panel.add(descF);

        int res = JOptionPane.showConfirmDialog(null, panel,
                "Neuen Termin am " + selectedDate + " anlegen", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                var ev = new Event(
                        titleF.getText().strip(),
                        selectedDate,
                        LocalTime.parse(timeF.getText().strip()),
                        descF.getText().strip());
                db.addEvent(ev);
                refreshDayView(selectedDate);
            } catch (Exception ex) {
                showError("Termin konnte nicht gespeichert werden:\n" + ex.getMessage());
            }
        }
    }

    private static void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Fehler", JOptionPane.ERROR_MESSAGE);
    }
}
