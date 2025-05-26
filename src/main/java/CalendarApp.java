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

    static DatabaseManager db;
    static CalendarView    monthView;

    static DefaultListModel<Event> dayModel;
    static JList<Event>            dayList;

    static LocalDate selectedDate;
    static JFrame    frame;

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            db = new DatabaseManager();
            try { db.connect(); } catch (SQLException ex) { showError("DB-Fehler:\n"+ex); }

            createAndShowGUI();
            new ReminderService(db, frame).start();
        });
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Java-Kalender");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        dayModel = new DefaultListModel<>();
        dayList  = new JList<>(dayModel);
        dayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dayList.setCellRenderer((lst, ev, i, sel, foc) -> new JLabel(ev.toString()));
        dayList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){ if(e.getClickCount()==2) editSel(); }
        });
        dayList.setComponentPopupMenu(popup());

        YearMonth ym   = YearMonth.now();
        Set<LocalDate> busy = Set.of();
        try { busy = db.getEventDatesOfMonth(ym); } catch (SQLException ignored) {}
        monthView = new CalendarView(
                ym, busy,
                d->{selectedDate=d; refresh(d);},
                m->{try{monthView.updateBusyDays(db.getEventDatesOfMonth(m));}
                     catch(SQLException ex){showError("Ladefehler:\n"+ex.getMessage());}});

        JButton add = new JButton("Neuer Termin");
        add.addActionListener(e-> addDialog());

        JPanel right = new JPanel(new BorderLayout(0,10));
        right.setPreferredSize(new Dimension(280,0));
        right.add(new JLabel("Tagesansicht",SwingConstants.CENTER),BorderLayout.NORTH);
        right.add(new JScrollPane(dayList),                         BorderLayout.CENTER);
        right.add(add,                                              BorderLayout.SOUTH);

        frame.add(monthView,BorderLayout.CENTER);
        frame.add(right,     BorderLayout.EAST);
        frame.setLocationRelativeTo(null); frame.setVisible(true);
    }

    /* ---------- Tagesliste ---------- */
    static void refresh(LocalDate d){
        dayModel.clear();
        try { db.getOccurrencesForDate(d).forEach(dayModel::addElement); }
        catch(SQLException ex){ showError("Fehler:\n"+ex.getMessage()); }
    }

    /* ---------- Dialoge ---------- */
    private static void addDialog(){
        if(selectedDate==null){ showError("Datum wählen!"); return; }
        Event ev = EventDialog.show(frame,null);
        if(ev==null) return;
        try{
            db.addEvent(ev); refresh(selectedDate);
            monthView.updateBusyDays(db.getEventDatesOfMonth(YearMonth.from(selectedDate)));
        }catch(SQLException ex){ showError("Speichern fehlgeschlagen:\n"+ex.getMessage()); }
    }
    private static void editSel(){
        Event ev = dayList.getSelectedValue(); if(ev==null) return;
        Event edited = EventDialog.show(frame,ev); if(edited==null) return;
        try{
            db.updateEvent(edited); refresh(selectedDate);
            monthView.updateBusyDays(db.getEventDatesOfMonth(YearMonth.from(selectedDate)));
        }catch(SQLException ex){ showError("Update fehlgeschlagen:\n"+ex.getMessage()); }
    }
    private static void delSel(){
        Event ev = dayList.getSelectedValue(); if(ev==null) return;
        int res = JOptionPane.showConfirmDialog(frame,"Termin löschen?\n"+ev,
                "Löschen",JOptionPane.YES_NO_OPTION);
        if(res!=JOptionPane.YES_OPTION) return;
        try{
            db.deleteEvent(ev.getId()); refresh(selectedDate);
            monthView.updateBusyDays(db.getEventDatesOfMonth(YearMonth.from(selectedDate)));
        }catch(SQLException ex){ showError("Delete-Fehler:\n"+ex.getMessage()); }
    }

    private static JPopupMenu popup(){
        JPopupMenu pm = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Bearbeiten…");
        JMenuItem del  = new JMenuItem("Löschen");
        edit.addActionListener(e-> editSel());
        del .addActionListener(e-> delSel());
        pm.add(edit); pm.add(del);
        return pm;
    }

    static void showError(String m){
        JOptionPane.showMessageDialog(frame,m,"Fehler",JOptionPane.ERROR_MESSAGE);
    }
}

