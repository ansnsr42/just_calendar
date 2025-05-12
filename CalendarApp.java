import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import com.formdev.flatlaf.FlatLightLaf;   

public class CalendarApp {

    

    public static void main(String[] args) {
        FlatLightLaf.setup();               // << hier einfügen

        SwingUtilities.invokeLater(CalendarApp::erstelleUndZeigeGUI);
    }

    private static void erstelleUndZeigeGUI() {
        JFrame frame = new JFrame("Java‑Kalender");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        /* linke Seite: Monatsraster */
        JTextArea tagesAnsicht = new JTextArea();
        tagesAnsicht.setEditable(false);
        tagesAnsicht.setLineWrap(true);

        CalendarView monatView = new CalendarView(datum -> {
            // — Platzhalter, später Daten aus DatabaseManager holen —
            tagesAnsicht.setText("Termine am " + datum + ":\n(demnächst …)");
        });

        /* rechte Seite: Tagesdetails */
        JPanel rechts = new JPanel(new BorderLayout());
        rechts.setPreferredSize(new Dimension(250, 0));
        rechts.add(new JLabel("Tagesansicht", SwingConstants.CENTER),
                   BorderLayout.NORTH);
        rechts.add(new JScrollPane(tagesAnsicht), BorderLayout.CENTER);

        /* Hauptaufbau */
        frame.setLayout(new BorderLayout());
        frame.add(monatView, BorderLayout.CENTER);
        frame.add(rechts,    BorderLayout.EAST);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
