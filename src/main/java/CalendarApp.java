import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

public class CalendarApp {

    /* ---------- Felder ---------- */
    static DatabaseManager db;
    static CalendarView    monthView;

    static DefaultListModel<Event> dayModel;
    static JList<Event>            dayList;

    static LocalDate selectedDate;
    static JFrame    frame;

    /* ---------- Einstieg ---------- */
    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            db = new DatabaseManager();
            try { db.connect(); }
            catch (SQLException ex) { showError("DB-Verbindung fehlgeschlagen:\n" + ex); }

            createAndShowGUI();

            /* ⏰ Reminder-Thread starten */
            new ReminderService(db, frame).start();
        });
    }

    /* ---------- GUI-Aufbau ---------- */
    private static void createAndShowGUI() {
        frame = new JFrame("Java-Kalender");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        /* Tagesansicht */
        dayModel = new DefaultListModel<>();
        dayList  = new JList<>(dayModel);
        dayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dayList.setCellRenderer((lst, ev, i, sel, foc) -> new JLabel(ev.toString()));
        dayList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelectedEvent();
            }
        });
        dayList.setComponentPopupMenu(buildPopup());

        /* Monatsansicht */
        YearMonth ym = YearMonth.now();
        Set<LocalDate> busy = Set.of();
        try { busy = db.getEventDatesOfMonth(ym); } catch (SQLException ignored) {}

        monthView = new CalendarView(
                ym,
                busy,
                date -> { selectedDate = date; refreshDayView(date); },
                newMonth -> {
                    try {
                        monthView.updateBusyDays(db.getEventDatesOfMonth(newMonth));
                    } catch (SQLException ex) {
                        showError("Fehler beim Laden der Monatsdaten:\n" + ex.getMessage());
                    }
                });

        /* rechte Spalte */
        JButton addBtn = new JButton("Neuer Termin");
        addBtn.addActionListener(e -> quickAddDialog());

        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setPreferredSize(new Dimension(280, 0));
        right.add(new JLabel("Tagesansicht", SwingConstants.CENTER), BorderLayout.NORTH);
        right.add(new JScrollPane(dayList),                          BorderLayout.CENTER);
        right.add(addBtn,                                            BorderLayout.SOUTH);

        frame.add(monthView, BorderLayout.CENTER);
        frame.add(right,     BorderLayout.EAST);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /* ---------- Helper ---------- */
    static void refreshDayView(LocalDate date) {
        dayModel.clear();
        try { db.getEventsForDate(date).forEach(dayModel::addElement); }
        catch (SQLException ex) { showError("Fehler beim Laden der Termine:\n" + ex.getMessage()); }
    }

    private static void quickAddDialog() {
        if (selectedDate == null) { showError("Bitte zuerst ein Datum auswählen."); return; }
        Event neu = EventDialog.show(frame, null);
        if (neu == null) return;
        try {
            db.addEvent(neu);
            refreshDayView(selectedDate);
            monthView.updateBusyDays(db.getEventDatesOfMonth(YearMonth.from(selectedDate)));
        } catch (SQLException ex) { showError("Termin konnte nicht gespeichert werden:\n" + ex.getMessage()); }
    }

    private static void editSelectedEvent() {
        Event ev = dayList.getSelectedValue();
        if (ev == null) return;
        Event edited = EventDialog.show(frame, ev);
        if (edited == null) return;
        try {
            db.updateEvent(edited);
            refreshDayView(selectedDate);
            monthView.updateBusyDays(db.getEventDatesOfMonth(YearMonth.from(selectedDate)));
        } catch (SQLException ex) { showError("Aktualisieren fehlgeschlagen:\n" + ex.getMessage()); }
    }

    private static void deleteSelectedEvent() {
        Event ev = dayList.getSelectedValue();
        if (ev == null) return;
        int res = JOptionPane.showConfirmDialog(frame,
                "Termin wirklich löschen?\n" + ev,
                "Löschen bestätigen", JOptionPane.YES_NO_OPTION);
        if (res != JOptionPane.YES_OPTION) return;
        try {
            db.deleteEvent(ev.getId());
            refreshDayView(selectedDate);
            monthView.updateBusyDays(db.getEventDatesOfMonth(YearMonth.from(selectedDate)));
        } catch (SQLException ex) { showError("Löschen fehlgeschlagen:\n" + ex.getMessage()); }
    }

    private static JPopupMenu buildPopup() {
        JPopupMenu pm = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Bearbeiten…");
        JMenuItem del  = new JMenuItem("Löschen");
        edit.addActionListener(e -> editSelectedEvent());
        del .addActionListener(e -> deleteSelectedEvent());
        pm.add(edit); pm.add(del);
        return pm;
    }

    static void showError(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Fehler", JOptionPane.ERROR_MESSAGE);
    }
}

