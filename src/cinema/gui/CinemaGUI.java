package cinema.gui;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.service.RezervareService;

import javax.swing.*;
import java.awt.*;

public class CinemaGUI extends JFrame {
    private RezervareService service;

    public CinemaGUI(RezervareService service) {
        this.service = service;

        setTitle("Cinema Reservation System");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel stânga pentru filme și ore
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

        JScrollPane scroll = new JScrollPane(filmePanel);
        add(scroll, BorderLayout.WEST);
    }

    private void deschideEcranScaune(Film film, String ora) {
        JFrame scauneFrame = new JFrame("Rezervare - " + film.getTitlu() + " (" + ora + ")");
        scauneFrame.setSize(900, 600);
        scauneFrame.setLayout(new BorderLayout());

        // Preia sala pentru ora selectată
        Sala sala = service.getSala(film, ora);
        Scaun[][] scaune = sala.getScaune();

        // Top: reprezentare ecran
        JPanel screenPanel = new JPanel();
        screenPanel.setBackground(Color.LIGHT_GRAY);
        screenPanel.setPreferredSize(new Dimension(0, 50));
        screenPanel.add(new JLabel("ECRAN"));
        scauneFrame.add(screenPanel, BorderLayout.NORTH);

        // Panel central pentru scaune
        JPanel scaunePanel = new JPanel(new GridLayout(
                sala.getRanduri(),
                sala.getColoane() + 2, // +2 coloane goale în mijloc pentru culoar
                5, 5));

        for (int r = 0; r < scaune.length; r++) {
            // Label pentru rând (A, B, C...)
            scaunePanel.add(new JLabel(Character.toString((char) ('A' + r)), SwingConstants.CENTER));

            for (int c = 0; c < sala.getColoane(); c++) {
                if (c == sala.getColoane() / 2) { // culoar de 2 locuri
                    scaunePanel.add(new JLabel(" "));
                    scaunePanel.add(new JLabel(" "));
                }

                JButton btn = new JButton(String.valueOf(c + 1));
                Scaun scaun = scaune[r][c];

                if (scaun.esteRezervat()) {
                    btn.setEnabled(false);
                    btn.setBackground(Color.RED);
                } else {
                    btn.setBackground(Color.GREEN);
                    btn.addActionListener(ae -> {
                        scaun.rezerva();
                        btn.setBackground(Color.ORANGE);
                    });
                }

                btn.setToolTipText("Rând " + (char) ('A' + r) + ", Loc " + (c + 1) + ", Ora " + ora);
                scaunePanel.add(btn);
            }
        }

        scauneFrame.add(new JScrollPane(scaunePanel), BorderLayout.CENTER);

        // Panel jos pentru email și buton rezervare
        JPanel bottom = new JPanel();
        JTextField emailField = new JTextField(20);
        JButton rezervaBtn = new JButton("Rezervă");

        rezervaBtn.addActionListener(e -> {
            if (emailField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(scauneFrame, "Introduceți un email valid!");
                return;
            }
            service.salveazaRezervare(film, ora, emailField.getText(), sala);
            JOptionPane.showMessageDialog(scauneFrame, "Rezervare efectuată!");
            scauneFrame.dispose();
        });

        bottom.add(new JLabel("Email:"));
        bottom.add(emailField);
        bottom.add(rezervaBtn);

        scauneFrame.add(bottom, BorderLayout.SOUTH);
        scauneFrame.setVisible(true);
    }
}
