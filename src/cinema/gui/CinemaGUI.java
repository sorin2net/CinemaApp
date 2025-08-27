package cinema.gui;

import cinema.model.Film;
import cinema.model.Scaun;
import cinema.service.RezervareService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class CinemaGUI extends JFrame {

    private RezervareService service;
    private JPanel filmePanel;
    private JTextField searchField;

    public CinemaGUI(RezervareService service) {
        this.service = service;

        setTitle("Cinema Reservation System");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel cu search
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        topPanel.add(new JLabel("Caută film sau oră:"));
        searchField = new JTextField(20);
        topPanel.add(searchField);
        JButton searchBtn = new JButton("Caută");
        searchBtn.addActionListener(e -> updateFilmePanel(searchField.getText()));
        topPanel.add(searchBtn);
        add(topPanel, BorderLayout.NORTH);

        // Panel stânga pentru filme și ore
        filmePanel = new JPanel();
        filmePanel.setLayout(new BoxLayout(filmePanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(filmePanel);
        scroll.setPreferredSize(new Dimension(300, 0));
        add(scroll, BorderLayout.WEST);

        // Inițializare filme
        updateFilmePanel("");

        setVisible(true);
    }

    private void updateFilmePanel(String filter) {
        filmePanel.removeAll();

        List<Film> filmeFiltrate = service.getFilme().stream()
                .filter(f -> f.getTitlu().toLowerCase().contains(filter.toLowerCase())
                        || f.getOre().stream().anyMatch(o -> o.contains(filter)))
                .collect(Collectors.toList());

        for (Film film : filmeFiltrate) {
            JPanel filmPanel = new JPanel();
            filmPanel.setLayout(new BoxLayout(filmPanel, BoxLayout.Y_AXIS));
            filmPanel.setBorder(BorderFactory.createTitledBorder(film.getTitlu()));

            for (String ora : film.getOre()) {
                JButton oraBtn = new JButton("Rezervă la " + ora);
                oraBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                oraBtn.addActionListener(e -> deschideEcranScaune(film, ora));
                filmPanel.add(oraBtn);
            }
            filmPanel.add(Box.createVerticalStrut(10));
            filmePanel.add(filmPanel);
        }

        filmePanel.revalidate();
        filmePanel.repaint();
    }

    private void deschideEcranScaune(Film film, String ora) {
        JFrame scauneFrame = new JFrame("Rezervare - " + film.getTitlu() + " (" + ora + ")");
        scauneFrame.setSize(900, 600);
        scauneFrame.setLayout(new BorderLayout());

        JPanel scaunePanel = new JPanel(new GridBagLayout());
        scaunePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        Scaun[][] scaune = service.getSala(film, ora);

        for (int r = 0; r < scaune.length; r++) {
            for (int c = 0; c < scaune[r].length; c++) {
                Scaun sc = scaune[r][c];
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(50, 50));

                // Iconițe sau culoare
                if (sc.esteRezervat()) {
                    btn.setBackground(Color.RED);
                    btn.setEnabled(false);
                } else {
                    btn.setBackground(Color.GREEN);
                    btn.addActionListener(e -> {
                        sc.rezerva();
                        btn.setBackground(Color.ORANGE); // selectat
                    });
                }

                btn.setToolTipText("Rând " + (r + 1) + ", Scaun " + (c + 1) + ", Ora " + ora);
                gbc.gridx = c;
                gbc.gridy = r;
                scaunePanel.add(btn, gbc);
            }
        }

        // Panel jos pentru email și buton rezervare
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JTextField emailField = new JTextField(20);
        JButton rezervaBtn = new JButton("Rezervă");

        rezervaBtn.addActionListener(e -> {
            if (emailField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(scauneFrame, "Introduceți un email valid!");
                return;
            }
            service.salveazaRezervare(film, ora, emailField.getText(), scaune);
            JOptionPane.showMessageDialog(scauneFrame, "Rezervare efectuată!");
            scauneFrame.dispose();
        });

        bottom.add(new JLabel("Email:"));
        bottom.add(emailField);
        bottom.add(rezervaBtn);

        scauneFrame.add(new JScrollPane(scaunePanel), BorderLayout.CENTER);
        scauneFrame.add(bottom, BorderLayout.SOUTH);
        scauneFrame.setVisible(true);
    }
}
