import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Dialog for creating or editing an event.
 *  • ev == null  →  create a new event
 *  • ev != null  →  edit an existing event
 * Returns the new / updated Event, or null if the user cancels.
 */
public class EventDialog {

    public static Event show(Window parent, Event ev) {
        boolean isNew = (ev == null);

        /* input fields */
        JTextField titleF = new JTextField(isNew ? "" : ev.getTitle());
        JTextField timeF  = new JTextField(isNew ? "HH:MM" : ev.getTime().toString());
        JTextField descF  = new JTextField(isNew ? "" : ev.getDescription());

        JComboBox<RecurrenceType> recurC = new JComboBox<>(RecurrenceType.values());
        recurC.setSelectedItem(isNew ? RecurrenceType.NONE : ev.getRecurrence());

        JTextField untilF = new JTextField(
                isNew || ev.getUntil() == null ? "" : ev.getUntil().toString());

        /* form layout */
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel("Title:"));                 p.add(titleF);
        p.add(new JLabel("Time (HH:MM):"));          p.add(timeF);
        p.add(new JLabel("Description:"));           p.add(descF);
        p.add(new JLabel("Recurrence:"));            p.add(recurC);
        p.add(new JLabel("End date (YYYY-MM-DD, optional):")); p.add(untilF);

        int res = JOptionPane.showConfirmDialog(parent, p,
                isNew ? "New Event" : "Edit Event",
                JOptionPane.OK_CANCEL_OPTION);

        if (res != JOptionPane.OK_OPTION) return null;

        /* validate inputs */
        LocalTime t;
        try { t = LocalTime.parse(timeF.getText().strip()); }
        catch (Exception ex) { error(parent, "Time format must be HH:MM!"); return null; }

        LocalDate until = null;
        String untilTxt = untilF.getText().strip();
        if (!untilTxt.isEmpty()) {
            try { until = LocalDate.parse(untilTxt); }
            catch (Exception ex) { error(parent, "Date format must be YYYY-MM-DD!"); return null; }
        }

        RecurrenceType rec = (RecurrenceType) recurC.getSelectedItem();

        /* create or update */
        if (isNew) {
            return new Event(
                    titleF.getText().strip(),
                    CalendarApp.selectedDate,      // start date = currently selected day
                    t,
                    descF.getText().strip(),
                    rec,
                    until);
        } else {
            ev.setTitle(titleF.getText().strip());
            ev.setTime(t);
            ev.setDescription(descF.getText().strip());
            ev.setRecurrence(rec);
            ev.setUntil(until);
            return ev;
        }
    }

    private static void error(Window w, String msg) {
        JOptionPane.showMessageDialog(w, msg, "Input error",
                                      JOptionPane.ERROR_MESSAGE);
    }
}

