import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Dialog zum Anlegen oder Bearbeiten eines Termins.
 *  • ev == null  →  neuer Termin
 *  • ev != null  →  vorhandenen Termin bearbeiten
 * Liefert das erzeugte/aktualisierte Event oder null bei Abbruch.
 */
public class EventDialog {

    public static Event show(Window parent, Event ev) {
        boolean isNew = (ev == null);

        /* Eingabefelder vorbereiten */
        JTextField titleF = new JTextField(isNew ? "" : ev.getTitle());
        JTextField timeF  = new JTextField(isNew ? "HH:MM" : ev.getTime().toString());
        JTextField descF  = new JTextField(isNew ? "" : ev.getDescription());

        JComboBox<RecurrenceType> recurC = new JComboBox<>(RecurrenceType.values());
        recurC.setSelectedItem(isNew ? RecurrenceType.NONE : ev.getRecurrence());

        JTextField untilF = new JTextField(
                isNew || ev.getUntil() == null ? "" : ev.getUntil().toString());

        /* Layout */
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel("Titel:"));            p.add(titleF);
        p.add(new JLabel("Uhrzeit (HH:MM):"));  p.add(timeF);
        p.add(new JLabel("Beschreibung:"));     p.add(descF);
        p.add(new JLabel("Wiederkehr:"));       p.add(recurC);
        p.add(new JLabel("Bis (YYYY-MM-DD, optional):")); p.add(untilF);

        int res = JOptionPane.showConfirmDialog(parent, p,
                isNew ? "Neuer Termin" : "Termin bearbeiten",
                JOptionPane.OK_CANCEL_OPTION);

        if (res != JOptionPane.OK_OPTION) return null;

        /* Eingaben validieren */
        LocalTime t;
        try { t = LocalTime.parse(timeF.getText().strip()); }
        catch (Exception ex) { error(parent, "Zeitformat HH:MM!"); return null; }

        LocalDate until = null;
        String untilTxt = untilF.getText().strip();
        if (!untilTxt.isEmpty()) {
            try { until = LocalDate.parse(untilTxt); }
            catch (Exception ex) { error(parent, "Datumformat YYYY-MM-DD!"); return null; }
        }

        RecurrenceType rec = (RecurrenceType) recurC.getSelectedItem();

        /* Neues Event anlegen oder bestehendes aktualisieren */
        if (isNew) {
            return new Event(
                    titleF.getText().strip(),
                    CalendarApp.selectedDate,   // Startdatum = aktuell gewählter Tag
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
        JOptionPane.showMessageDialog(w, msg, "Eingabefehler",
                                      JOptionPane.ERROR_MESSAGE);
    }
}

