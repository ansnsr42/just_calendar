import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;

/**
 * Reusable Dialog zum Anlegen oder Bearbeiten eines Termins.
 * Bei OK liefert er den neuen/aktualisierten Event,
 * sonst {@code null}.
 */
public class EventDialog {

    public static Event show(Window parent, Event ev) {
        boolean isNew = (ev == null);

        JTextField titleF = new JTextField(isNew ? "" : ev.getTitle());
        JTextField timeF  = new JTextField(isNew ? "HH:MM" : ev.getTime().toString());
        JTextField descF  = new JTextField(isNew ? "" : ev.getDescription());

        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel("Titel:"));            p.add(titleF);
        p.add(new JLabel("Uhrzeit (HH:MM):"));  p.add(timeF);
        p.add(new JLabel("Beschreibung:"));     p.add(descF);

        int res = JOptionPane.showConfirmDialog(parent, p,
                isNew ? "Neuer Termin" : "Termin bearbeiten",
                JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return null;

        /* Zeit validieren */
        LocalTime t;
        try { t = LocalTime.parse(timeF.getText().strip()); }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                    "Zeitformat muss HH:MM sein.", "Eingabefehler",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (isNew) {                                  // Neuer Termin
            return new Event(
                    titleF.getText().strip(),
                    CalendarApp.selectedDate,
                    t,
                    descF.getText().strip());
        } else {                                      // Bearbeitung
            ev.setTitle      (titleF.getText().strip());
            ev.setTime       (t);
            ev.setDescription(descF.getText().strip());
            return ev;
        }
    }
}

