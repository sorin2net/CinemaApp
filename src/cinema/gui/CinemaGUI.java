package cinema.gui;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.service.RezervareService;

import javax.swing.*;
import java.awt.*;

public class CinemaGUI extends JFrame {
    private RezervareService rezervareService;

    public CinemaGUI(RezervareService service) {
        this.rezervareService = service;

        service.incarcaRezervari(); // încarcă rezervările existente

        setTitle("Cinema App");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

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
        scauneFrame.setSize(700, 500);
        scauneFrame.setLayout(new BorderLayout());

        Sala sala = film.getSala();
        Scaun[][] scaune = sala.getScaune();

        JPanel scaunePanel = new JPanel(new GridLayout(sala.getRanduri(), sala.getColoane(), 5, 5));
        JButton[][] butoaneScaune = new JButton[sala.getRanduri()][sala.getColoane()];

        for (int r = 0; r < sala.getRanduri(); r++) {
            for (int c = 0; c < sala.getColoane(); c++) {
                JButton scaunBtn = new JButton((r+1) + "-" + (c+1));
                final int row = r;
                final int col = c;

                if (scaune[r][c].esteRezervat()) {
                    scaunBtn.setEnabled(false);
                    scaunBtn.setBackground(Color.RED);
                } else {
                    scaunBtn.setBackground(Color.GREEN);
                    scaunBtn.addActionListener(ev -> {
                        if (scaunBtn.getBackground() == Color.GREEN) {
                            scaunBtn.setBackground(Color.ORANGE);
                        } else if (scaunBtn.getBackground() == Color.ORANGE) {
                            scaunBtn.setBackground(Color.GREEN);
                        }
                    });
                }

                butoaneScaune[r][c] = scaunBtn;
                scaunePanel.add(scaunBtn);
            }
        }

        JPanel bottomPanel = new JPanel();
        JTextField emailField = new JTextField(20);
        JButton rezervaBtn = new JButton("Rezervă");

        rezervaBtn.addActionListener(ev -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(scauneFrame, "Introdu email-ul!");
                return;
            }

            for (int r = 0; r < sala.getRanduri(); r++) {
                for (int c = 0; c < sala.getColoane(); c++) {
                    JButton btn = butoaneScaune[r][c];
                    if (btn.getBackground() == Color.ORANGE) {
                        rezervareService.rezervaScaun(film, ora, r, c, email);
                        btn.setBackground(Color.RED);
                        btn.setEnabled(false);
                    }
                }
            }

            JOptionPane.showMessageDialog(scauneFrame, "Rezervare efectuată!");
        });

        bottomPanel.add(new JLabel("Email:"));
        bottomPanel.add(emailField);
        bottomPanel.add(rezervaBtn);

        scauneFrame.add(scaunePanel, BorderLayout.CENTER);
        scauneFrame.add(bottomPanel, BorderLayout.SOUTH);

        scauneFrame.setVisible(true);
    }
}
