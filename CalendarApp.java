import javax.swing.*;
import java.awt.*;
public class CalendarApp {

    // Main driver method
    public static void main(String[] args)
    {
        // Event-Dispatch-Thread (EDT) 
        SwingUtilities.invokeLater(CalendarApp::createAndShowGUI);
    }
        /// GUI 
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Kalender");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        JLabel label = new JLabel("Willkommen zum Kalender", SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        
        JButton addButton = new JButton("Neuer Termin");
        panel.add(addButton, BorderLayout.SOUTH);
        
        frame.add(panel);
        frame.setVisible(true);
    }
}
    /// Db anbindung
