import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Zeigt einen Monat als Raster und meldet Klicks auf einzelne Tage.
 */
public class CalendarView extends JPanel {

    private YearMonth aktuellerMonat;
    private final Consumer<LocalDate> onDateSelected;

    public CalendarView(Consumer<LocalDate> onDateSelected) {
        this.onDateSelected = onDateSelected;
        this.aktuellerMonat = YearMonth.now();
        baueUI();
    }

    /* ---------------- GUI‑Aufbau ---------------- */
    private void baueUI() {
        setLayout(new BorderLayout());
        add(baueKopf(),  BorderLayout.NORTH);
        add(baueRaster(), BorderLayout.CENTER);
    }

    /* Kopfzeile mit Monatstitel + Navigation */
    private JPanel baueKopf() {
        JPanel kopf = new JPanel(new BorderLayout());

        JButton vorher = new JButton("<");
        JButton naechst = new JButton(">");
        JLabel  titel  = new JLabel(
                aktuellerMonat.getMonth()
                              .getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + aktuellerMonat.getYear(),
                SwingConstants.CENTER);

        vorher.addActionListener(e -> wechsleMonat(-1, titel));
        naechst.addActionListener(e -> wechsleMonat( 1, titel));

        kopf.add(vorher, BorderLayout.WEST);
        kopf.add(titel,  BorderLayout.CENTER);
        kopf.add(naechst,BorderLayout.EAST);
        return kopf;
    }

    private void wechsleMonat(int delta, JLabel titel) {
        aktuellerMonat = aktuellerMonat.plusMonths(delta);
        titel.setText(
                aktuellerMonat.getMonth()
                              .getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + aktuellerMonat.getYear());
        remove(1);                         // altes Raster löschen
        add(baueRaster(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /* Raster mit Wochentagen + Buttons */
    private JPanel baueRaster() {
        JPanel raster = new JPanel(new GridLayout(0, 7, 2, 2));

        // Wochentags‑Überschriften
        for (DayOfWeek d : DayOfWeek.values()) {
            JLabel lbl = new JLabel(d.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                    SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
            raster.add(lbl);
        }

        LocalDate erster      = aktuellerMonat.atDay(1);
        int       verschiebung = erster.getDayOfWeek().getValue() % 7; // Sonntag = 0
        for (int i = 0; i < verschiebung; i++) raster.add(new JLabel(""));

        LocalDate heute = LocalDate.now();
        for (int tag = 1; tag <= aktuellerMonat.lengthOfMonth(); tag++) {
            LocalDate datum = aktuellerMonat.atDay(tag);
            JButton   btn   = new JButton(String.valueOf(tag));
            if (datum.equals(heute)) {
                btn.setBackground(Color.LIGHT_GRAY);              // Heute hervorheben
            }
            btn.addActionListener(e -> onDateSelected.accept(datum));
            raster.add(btn);
        }

        while (raster.getComponentCount() % 7 != 0) raster.add(new JLabel(""));
        return raster;
    }
}
