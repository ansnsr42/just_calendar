import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

public class CalendarApp {

    /* ---------- singletons ---------- */
    static DatabaseManager db;
    static CalendarView    monthView;

    /* ---------- day list & search ---------- */
    static DefaultListModel<Event> dayModel;
    static JList<Event>            dayList;

    static JTextField searchF;
    static boolean    searchMode = false;

    /* ---------- selection / frame ---------- */
    static LocalDate selectedDate;
    static JFrame    frame;

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            db = new DatabaseManager();
            try { db.connect(); }
            catch (SQLException ex) { showError("Database error:\n" + ex); }

            createAndShowGUI();
            new ReminderService(db, frame).start();    // desktop pop-ups
        });
    }

    /* ---------- build GUI ---------- */
    private static void createAndShowGUI() {
        frame = new JFrame("Java Calendar");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        /* --- day list (right column) --- */
        dayModel = new DefaultListModel<>();
        dayList  = new JList<>(dayModel);
        dayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dayList.setCellRenderer((lst, ev, i, sel, foc) -> new JLabel(ev.toString()));
        dayList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSel();
            }
        });
        dayList.setComponentPopupMenu(popup());

        /* --- live search field (min. 2 chars) --- */
        searchF = new JTextField();
        searchF.setToolTipText("Search (min. 2 chars) …");
        searchF.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String term = searchF.getText().trim();
                if (term.length() >= 2) {
                    performSearch(term);
                } else {
                    searchMode = false;
                    if (selectedDate != null) refresh(selectedDate); else dayModel.clear();
                }
            }
            public void insertUpdate(DocumentEvent e){ update(); }
            public void removeUpdate(DocumentEvent e){ update(); }
            public void changedUpdate(DocumentEvent e){ update(); }
        });

        /* --- month view (center) --- */
        YearMonth ym = YearMonth.now();
        Set<LocalDate> busy = Set.of();
        try { busy = db.getEventDatesOfMonth(ym); } catch (SQLException ignored) {}
        monthView = new CalendarView(
                ym, busy,
                d -> { selectedDate = d; refresh(d); },
                m -> {
                    try   { monthView.updateBusyDays(db.getEventDatesOfMonth(m)); }
                    catch (SQLException ex) { showError("Load error:\n" + ex.getMessage()); }
                });

        /* --- right column (search + list + add-button) --- */
        JButton add = new JButton("New Event");
        add.addActionListener(e -> addDialog());

        JPanel right = new JPanel(new BorderLayout(0,10));
        right.setPreferredSize(new Dimension(280,0));
        right.add(searchF,                   BorderLayout.NORTH);
        right.add(new JScrollPane(dayList),  BorderLayout.CENTER);
        right.add(add,                       BorderLayout.SOUTH);

        frame.add(monthView, BorderLayout.CENTER);
        frame.add(right,     BorderLayout.EAST);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /* ---------- show day list (or search results) ---------- */
    static void refresh(LocalDate d){
        if (searchMode) return;          // keep current search results
        dayModel.clear();
        try { db.getOccurrencesForDate(d).forEach(dayModel::addElement); }
        catch (SQLException ex){ showError("Error:\n" + ex.getMessage()); }
    }

    private static void performSearch(String term) {
        dayModel.clear();
        try {
            db.searchEvents(term).forEach(dayModel::addElement);
            searchMode = true;
        } catch (SQLException ex) { showError("Search failed:\n" + ex.getMessage()); }
    }

    /* ---------- dialogs ---------- */
    private static void addDialog(){
        if (selectedDate == null) { showError("Please choose a date!"); return; }
        Event ev = EventDialog.show(frame, null); if (ev == null) return;
        try {
            db.addEvent(ev);
            refresh(selectedDate);
            monthView.updateBusyDays(db.getEventDatesOfMonth(YearMonth.from(selectedDate)));
        } catch (SQLException ex) { showError("Save failed:\n" + ex.getMessage()); }
    }

    private static void editSel(){
        Event ev = dayList.getSelectedValue(); if (ev == null) return;
        Event edited = EventDialog.show(frame, ev); if (edited == null) return;
        try {
            db.updateEvent(edited);
            if (searchMode) performSearch(searchF.getText().trim());
            else            refresh(selectedDate);
            monthView.updateBusyDays(db.getEventDatesOfMonth(YearMonth.from(selectedDate)));
        } catch (SQLException ex) { showError("Update failed:\n" + ex.getMessage()); }
    }

    private static void delSel(){
        Event ev = dayList.getSelectedValue(); if (ev == null) return;
        int res = JOptionPane.showConfirmDialog(frame,
                "Delete this event?\n" + ev,
                "Delete", JOptionPane.YES_NO_OPTION);
        if (res != JOptionPane.YES_OPTION) return;
        try {
            db.deleteEvent(ev.getId());
            if (searchMode) performSearch(searchF.getText().trim());
            else            refresh(selectedDate);
            monthView.updateBusyDays(db.getEventDatesOfMonth(YearMonth.from(selectedDate)));
        } catch (SQLException ex) { showError("Delete failed:\n" + ex.getMessage()); }
    }

    /* ---------- context menu ---------- */
    private static JPopupMenu popup(){
        JPopupMenu pm = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Edit…");
        JMenuItem del  = new JMenuItem("Delete");
        edit.addActionListener(e -> editSel());
        del .addActionListener(e -> delSel());
        pm.add(edit); pm.add(del);
        return pm;
    }

    /* ---------- error helper ---------- */
    static void showError(String msg){
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}

