package cinema.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class AnulareRezervareFrame extends JFrame {

    private JButton yesButton;
    private JButton noButton;

    public AnulareRezervareFrame(String mesaj, Runnable onYes) {
        setTitle("Anulare rezervare");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(45, 45, 45)); // fundal întunecat
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(panel);

        // Label mesaj
        JLabel label = new JLabel(mesaj, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(label, BorderLayout.CENTER);

        // Panel pentru butoane
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(45, 45, 45));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        yesButton = new JButton("Da");
        yesButton.setBackground(new Color(70, 130, 180));
        yesButton.setForeground(Color.WHITE);
        yesButton.setFocusPainted(false);
        yesButton.setFont(new Font("Arial", Font.BOLD, 14));

        noButton = new JButton("Nu");
        noButton.setBackground(new Color(128, 128, 128));
        noButton.setForeground(Color.WHITE);
        noButton.setFocusPainted(false);
        noButton.setFont(new Font("Arial", Font.BOLD, 14));

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Actiuni butoane
        yesButton.addActionListener(e -> {
            if (onYes != null) onYes.run();
            dispose();
        });

        noButton.addActionListener(e -> dispose());

        setVisible(true);
    }
}
