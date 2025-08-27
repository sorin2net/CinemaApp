package cinema.gui;

import cinema.model.Film;
import cinema.model.Scaun;
import cinema.model.Sala;
import cinema.service.RezervareService;

import javax.swing.*;
import java.awt.*;

public class CinemaGUI extends JFrame {
    private RezervareService rezervareService;

    public CinemaGUI(RezervareService service) {
        this.rezervareService = service;

        setTitle("Cinema App");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel filme + ore
        JPanel filmePanel = new JPanel();
        filmePanel.setLayout(new BoxLayout(filmePanel, BoxLayout.Y_AXIS));

        for (Film film : service.getFilme()) {
            JPanel filmPanel = new JPanel();
            filmPanel.setLayout(new BoxLayout(filmPanel, BoxLayout.Y_AXIS));
            filmPanel.setBorder(BorderFactory.createTitledBorder(film.getTitlu()));

            for (String ora : film.getOre()) {
                JButton oraBtn = new JButton("Rezervă la " + ora);
                oraBtn.addActionListener(e -> deschideEcranScaune(film, ora));
                filmPanel.add(oraBtn);
            }

            filmePanel.add(filmPanel);
        }

        add(new JScrollPane(filmePanel), BorderLayout.WEST);
    }

    private void deschideEcranScaune(Film film, String ora) {
        JFrame scauneFrame = new JFrame("Rezervare - " + film.getTitlu() + " (" + ora + ")");
        scauneFrame.setSize(600, 500);
        scauneFrame.setLayout(new BorderLayout());

        Sala sala = rezervareService.getSala(film);
        Scaun[][] scaune = new Scaun[sala.getRanduri()][sala.getColoane()];

        // copiem scaunele reale
        for (int r = 0; r < sala.getRanduri(); r++) {
            for (int c = 0; c < sala.getColoane(); c++) {
                scaune[r][c] = sala.getScaun(r, c);
            }
        }

        JPanel scaunePanel = new JPanel(new GridLayout(sala.getRanduri(), sala.getColoane(), 5, 5));
        JButton[][] seatButtons = new JButton[sala.getRanduri()][sala.getColoane()];

        JTextField emailField = new JTextField(20);
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(new JLabel("Email:"));
        bottomPanel.add(emailField);

        JButton rezervaBtn = new JButton("Rezervă");
        bottomPanel.add(rezervaBtn);

        // creăm butoanele pentru scaune
        for (int r = 0; r < sala.getRanduri(); r++) {
            for (int c = 0; c < sala.getColoane(); c++) {
                JButton btn = new JButton((r + 1) + "-" + (c + 1));
                final int row = r;
                final int col = c;
                if (scaune[r][c].esteRezervat()) {
                    btn.setBackground(Color.RED);
                    btn.setEnabled(false);
                } else {
                    btn.setBackground(Color.GREEN);
                    btn.addActionListener(e -> {
                        if (btn.getBackground() == Color.GREEN) {
                            btn.setBackground(Color.ORANGE);
                        } else {
                            btn.setBackground(Color.GREEN);
                        }
                    });
                }
                seatButtons[r][c] = btn;
                scaunePanel.add(btn);
            }
        }

        rezervaBtn.addActionListener(e -> {
            String email = emailField.getText();
            for (int r = 0; r < sala.getRanduri(); r++) {
                for (int c = 0; c < sala.getColoane(); c++) {
                    if (seatButtons[r][c].getBackground() == Color.ORANGE) {
                        rezervareService.rezervaScaun(film, r, c, email, ora);
                        seatButtons[r][c].setBackground(Color.RED);
                        seatButtons[r][c].setEnabled(false);
                    }
                }
            }
            JOptionPane.showMessageDialog(scauneFrame, "Rezervare efectuată!");
        });

        scauneFrame.add(scaunePanel, BorderLayout.CENTER);
        scauneFrame.add(bottomPanel, BorderLayout.SOUTH);
        scauneFrame.setVisible(true);
    }
}
