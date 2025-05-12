import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.*;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;

public class CalendarView extends JPanel {

    private YearMonth aktuellerMonat = YearMonth.now();
    private final Consumer<LocalDate> onDateSelected;

    /* Farbpalette */
    private static final Color PRIMARY      = new Color(0x3B82F6);  // blau
    private static final Color PRIMARY_TEXT = Color.WHITE;
    private static final Color HOVER_BG     = new Color(0xE0E7FF);  // hell‑blau
    private static final Color WEEKEND_TXT  = new Color(0x9CA3AF);  // grau

    public CalendarView(Consumer<LocalDate> onDateSelected) {
        this.onDateSelected = onDateSelected;
        setOpaque(false);
        setLayout(new BorderLayout(0, 10));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildGrid(),   BorderLayout.CENTER);
    }

    /* Kopf mit Monatstitel + Pfeilen */
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JButton prev = navButton("<");
        JButton next = navButton(">");

        JLabel title = new JLabel(formatMonth(aktuellerMonat),
                                  SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        prev.addActionListener(e -> changeMonth(-1, title));
        next.addActionListener(e -> changeMonth( 1, title));

        p.add(prev,  BorderLayout.WEST);
        p.add(title, BorderLayout.CENTER);
        p.add(next,  BorderLayout.EAST);
        return p;
    }

    /* Raster */
    private JPanel buildGrid() {
        JPanel grid = new JPanel(new GridLayout(0, 7, 4, 4));
        grid.setOpaque(false);

        // Wochentagsköpfe
        for (DayOfWeek d : DayOfWeek.values()) {
            JLabel lbl = new JLabel(d.getDisplayName(TextStyle.SHORT, Locale.getDefault()).toUpperCase(),
                                    SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
            lbl.setForeground(WEEKEND_TXT.darker());
            grid.add(lbl);
        }

        LocalDate firstDay = aktuellerMonat.atDay(1);
        int shift = firstDay.getDayOfWeek().getValue() - 1; // Sonntag = 0
        for (int i = 0; i < shift; i++) grid.add(new JLabel(""));

        LocalDate today = LocalDate.now();
        for (int d = 1; d <= aktuellerMonat.lengthOfMonth(); d++) {
            LocalDate date = aktuellerMonat.atDay(d);
            JButton btn = dayButton(date, today);
            grid.add(btn);
        }
        while (grid.getComponentCount() % 7 != 0) grid.add(new JLabel(""));
        return grid;
    }

    /* ---------- UI‑Hilfsfunktionen ---------- */

    private static String formatMonth(YearMonth ym) {
        return ym.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + ym.getYear();
    }

    private JButton navButton(String txt) {
        JButton b = new JButton(txt);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 16f));
        return b;
    }

    private JButton dayButton(LocalDate date, LocalDate today) {
        JButton b = new RoundButton(String.valueOf(date.getDayOfMonth()));
        b.setFocusPainted(false);
        b.setBorderPainted(false);

        // Farben
        if (date.equals(today)) {
            b.setBackground(PRIMARY);
            b.setForeground(PRIMARY_TEXT);
        } else {
            b.setBackground(new Color(0,0,0,0));   // transparent
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                b.setForeground(WEEKEND_TXT);
            }
        }

        b.addActionListener(e -> onDateSelected.accept(date));

        // Hover‑Effekt
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!date.equals(today)) b.setBackground(HOVER_BG);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!date.equals(today)) b.setBackground(new Color(0,0,0,0));
            }
        });
        return b;
    }

    private void changeMonth(int delta, JLabel title) {
        aktuellerMonat = aktuellerMonat.plusMonths(delta);
        title.setText(formatMonth(aktuellerMonat));
        remove(1);          // altes Grid
        add(buildGrid(), BorderLayout.CENTER);
        revalidate(); repaint();
    }

    /* ---------- Runder Button ---------- */
    static class RoundButton extends JButton {
        RoundButton(String text) { super(text); setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            Shape round = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30);

            if (getModel().isArmed()) {
                g2.setColor(getBackground().darker());
            } else {
                g2.setColor(getBackground());
            }
            g2.fill(round);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override public void setBackground(Color bg) {
            super.setBackground(bg);
            setContentAreaFilled(false);
        }
    }
}
