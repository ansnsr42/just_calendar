import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.*;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Month panel with selectable day buttons.
 * Busy days get a red underline.
 */
public class CalendarView extends JPanel {

    private YearMonth month;
    private Set<LocalDate> busyDays;
    private final Consumer<LocalDate>  onSelect;
    private final Consumer<YearMonth>  onMonthChange;

    /* --- color palette --- */
    private static final Color PRIMARY       = new Color(0x3B82F6);
    private static final Color PRIMARY_TEXT  = Color.WHITE;
    private static final Color HOVER_BG      = new Color(0xE0E7FF);
    private static final Color WEEKEND_TEXT  = new Color(0x9CA3AF);
    private static final Color UNDERLINE     = new Color(0xEF4444);   // red

    /** 2-pixel red underline for busy days */
    private static final Border BUSY_BORDER =
            BorderFactory.createMatteBorder(0, 0, 2, 0, UNDERLINE);

    /* --- constructor --- */
    public CalendarView(YearMonth   month,
                        Set<LocalDate> busyDays,
                        Consumer<LocalDate>  onSelect,
                        Consumer<YearMonth>  onMonthChange) {

        this.month         = month;
        this.busyDays      = busyDays;
        this.onSelect      = onSelect;
        this.onMonthChange = onMonthChange;

        setOpaque(false);
        setLayout(new BorderLayout(0, 10));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildGrid(),   BorderLayout.CENTER);
    }

    /* --- header with month title + arrows --- */
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JButton prev = navBtn("<");
        JButton next = navBtn(">");

        JLabel title = new JLabel(formatMonth(month), SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        prev.addActionListener(e -> changeMonth(-1, title));
        next.addActionListener(e -> changeMonth( 1, title));

        p.add(prev,  BorderLayout.WEST);
        p.add(title, BorderLayout.CENTER);
        p.add(next,  BorderLayout.EAST);
        return p;
    }

    /* --- month grid --- */
    private JPanel buildGrid() {
        JPanel g = new JPanel(new GridLayout(0, 7, 4, 4));
        g.setOpaque(false);

        // weekday header (Mon–Sun)
        for (DayOfWeek d : DayOfWeek.values()) {
            JLabel lbl = new JLabel(
                    d.getDisplayName(TextStyle.SHORT, Locale.getDefault()).toUpperCase(),
                    SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
            lbl.setForeground(WEEKEND_TEXT.darker());
            g.add(lbl);
        }

        // leading blanks until Monday
        LocalDate first = month.atDay(1);
        int shift = first.getDayOfWeek().getValue() - 1; // Monday = 0
        for (int i = 0; i < shift; i++) g.add(new JLabel(""));

        // day buttons
        LocalDate today = LocalDate.now();
        for (int d = 1; d <= month.lengthOfMonth(); d++) {
            LocalDate date = month.atDay(d);
            g.add(createDayButton(date, today));
        }

        // trailing blanks to fill the last row
        while (g.getComponentCount() % 7 != 0) g.add(new JLabel(""));
        return g;
    }

    /* --- create a single day button --- */
    private JButton createDayButton(LocalDate date, LocalDate today) {
        JButton b = new RoundButton(String.valueOf(date.getDayOfMonth()));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBackground(new Color(0, 0, 0, 0));     // start transparent

        // red underline if events exist
        if (busyDays.contains(date)) {
            b.setBorder(BUSY_BORDER);
            b.setBorderPainted(true);
        } else {
            b.setBorder(null);
        }

        // highlight today / weekends
        if (date.equals(today)) {
            b.setBackground(PRIMARY);
            b.setForeground(PRIMARY_TEXT);
        } else if (date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                   date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            b.setForeground(WEEKEND_TEXT);
        }

        // simple hover effect
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!date.equals(today)) b.setBackground(HOVER_BG);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!date.equals(today)) b.setBackground(new Color(0, 0, 0, 0));
            }
        });

        b.addActionListener(e -> onSelect.accept(date));
        return b;
    }

    /* --- change month via arrows --- */
    private void changeMonth(int delta, JLabel title) {
        month = month.plusMonths(delta);
        title.setText(formatMonth(month));
        onMonthChange.accept(month);          // notify parent
    }

    /* update busy set and rebuild the grid */
    public void updateBusyDays(Set<LocalDate> newBusy) {
        busyDays = newBusy;
        remove(1);
        add(buildGrid(), BorderLayout.CENTER);
        revalidate(); repaint();
    }

    /* --- helper methods --- */
    private static String formatMonth(YearMonth ym) {
        return ym.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
               + ' ' + ym.getYear();
    }
    private static JButton navBtn(String text) {
        JButton b = new JButton(text);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 16f));
        return b;
    }

    /* --- inner class: rounded button --- */
    static class RoundButton extends JButton {
        RoundButton(String t) { super(t); setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape r = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30);
            g2.setColor(getModel().isArmed() ? getBackground().darker() : getBackground());
            g2.fill(r); g2.dispose();
            super.paintComponent(g);
        }
        @Override public void setBackground(Color c) {
            super.setBackground(c);
            setContentAreaFilled(false);
        }
    }
}

