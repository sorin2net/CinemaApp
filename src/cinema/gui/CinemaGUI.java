package cinema.gui;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.service.RezervareService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

public class CinemaGUI extends JFrame {
    private RezervareService service;
    private JPanel filmePanel;
    private JComboBox<String> lunaCombo;
    private JComboBox<Integer> ziCombo;
    private JComboBox<String> oreCombo;
    private JTextField searchField;

    public CinemaGUI(RezervareService service) {
        this.service = service;

        setTitle("Cinema Reservation System");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ---------------- top panel ----------------
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        topPanel.add(new JLabel("Caută film:"));
        topPanel.add(searchField);

        // ore disponibile
        Set<String> oreDisponibileSet = service.getFilme().stream()
                .flatMap(f -> f.getOre().stream())
                .collect(Collectors.toSet());

        oreCombo = new JComboBox<>();
        oreCombo.addItem("Oricând");
        oreDisponibileSet.stream().sorted().forEach(oreCombo::addItem);

        topPanel.add(new JLabel("Filtrează după ora:"));
        topPanel.add(oreCombo);

        // luna
        String[] luni = {"Septembrie", "Octombrie", "Noiembrie", "Decembrie"};
        lunaCombo = new JComboBox<>(luni);
        topPanel.add(new JLabel("Lună:"));
        topPanel.add(lunaCombo);

        // zi
        ziCombo = new JComboBox<>();
        topPanel.add(new JLabel("Zi:"));
        topPanel.add(ziCombo);

        add(topPanel, BorderLayout.NORTH);

        // ---------------- filme panel ----------------
        filmePanel = new JPanel();
        JScrollPane scrollFilme = new JScrollPane(filmePanel);
        add(scrollFilme, BorderLayout.CENTER);

        // ---------------- listeners ----------------
        lunaCombo.addActionListener(e -> actualizeazaZile());
        ziCombo.addActionListener(e -> afiseazaFilme());
        oreCombo.addActionListener(e -> afiseazaFilme());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { afiseazaFilme(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { afiseazaFilme(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { afiseazaFilme(); }
        });

        actualizeazaZile();
        afiseazaFilme();
    }

    private void actualizeazaZile() {
        ziCombo.removeAllItems();
        int lunaIndex = lunaCombo.getSelectedIndex() + 9; // septembrie = 9
        YearMonth ym = YearMonth.of(2025, lunaIndex);
        for (int zi = 1; zi <= ym.lengthOfMonth(); zi++) {
            ziCombo.addItem(zi);
        }
    }

    private void afiseazaFilme() {
        filmePanel.removeAll();

        int lunaIndex = lunaCombo.getSelectedIndex() + 9;
        Integer zi = (Integer) ziCombo.getSelectedItem();
        if (zi == null) return;

        LocalDate data = LocalDate.of(2025, lunaIndex, zi);
        String text = searchField.getText().toLowerCase();
        String oraSelectata = (String) oreCombo.getSelectedItem();

        List<Film> filmeZi = service.getFilmePentruZi(data).stream()
                .filter(f -> f.getTitlu().toLowerCase().contains(text))
                .filter(f -> "Oricând".equals(oraSelectata) || f.getOre().contains(oraSelectata))
                .collect(Collectors.toList());

        filmePanel.setLayout(new GridLayout(filmeZi.size(), 1, 0, 10));

        for (Film film : filmeZi) {
            JPanel filmPanel = new JPanel(new BorderLayout(20, 10));
            filmPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Poster
            JLabel imageLabel = new JLabel();
            File imageFile = new File("resources/" + film.getImaginePath());
            if (imageFile.exists()) {
                ImageIcon icon = new ImageIcon(imageFile.getPath());
                Image scaled = icon.getImage().getScaledInstance(220, 320, Image.SCALE_FAST);
                imageLabel.setIcon(new ImageIcon(scaled));
                imageLabel.setPreferredSize(new Dimension(220, 320));
            } else {
                imageLabel.setText("Imagine lipsă");
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                imageLabel.setPreferredSize(new Dimension(220, 320));
            }
            filmPanel.add(imageLabel, BorderLayout.WEST);

            // Panel titlu + gen + varsta + butoane
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

            // Titlu + varsta pe acelasi rand
            JPanel titluVarstaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            titluVarstaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Titlu
            JLabel titluLabel = new JLabel(film.getTitlu());
            titluLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titluVarstaPanel.add(titluLabel);

            // Varsta
            JLabel varstaLabel = new JLabel("(" + film.getRestrictieVarsta() + "+)");
            varstaLabel.setFont(new Font("Arial", Font.BOLD, 20));
            switch (film.getRestrictieVarsta()) {
                case 3 -> varstaLabel.setForeground(Color.GREEN);
                case 7 -> varstaLabel.setForeground(Color.YELLOW);
                case 12 -> varstaLabel.setForeground(Color.ORANGE);
                case 15 -> varstaLabel.setForeground(Color.RED);
                case 18 -> varstaLabel.setForeground(Color.BLACK);
                default -> varstaLabel.setForeground(Color.GRAY);
            }
            titluVarstaPanel.add(varstaLabel);

            rightPanel.add(titluVarstaPanel);

            // Genul filmului sub titlu
            JLabel genLabel = new JLabel(film.getGen() != null ? film.getGen() : "Gen necunoscut");
            genLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            genLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            rightPanel.add(genLabel);

            rightPanel.add(Box.createVerticalStrut(15));

            // Butoane rezervare
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (String ora : film.getOre()) {
                JButton oraBtn = new JButton("Rezervă la " + ora);
                oraBtn.addActionListener(e -> deschideEcranScaune(film, ora, data));
                buttonsPanel.add(oraBtn);
            }
            rightPanel.add(buttonsPanel);

            filmPanel.add(rightPanel, BorderLayout.CENTER);
            filmPanel.setBackground(Color.WHITE);

            filmePanel.add(filmPanel);
        }

        filmePanel.revalidate();
        filmePanel.repaint();
    }


    private void deschideEcranScaune(Film film, String ora, LocalDate data) {
        JFrame scauneFrame = new JFrame("Rezervare - " + film.getTitlu() + " (" + ora + ") Ziua: " + data);
        scauneFrame.setSize(1000, 700);
        scauneFrame.setLayout(new BorderLayout());

        // ecran
        JPanel ecranPanel = new JPanel();
        ecranPanel.setPreferredSize(new Dimension(0, 40));
        ecranPanel.setBackground(Color.LIGHT_GRAY);
        JLabel ecranLabel = new JLabel("ECRAN", SwingConstants.CENTER);
        ecranLabel.setFont(new Font("Arial", Font.BOLD, 16));
        ecranPanel.setLayout(new BorderLayout());
        ecranPanel.add(ecranLabel, BorderLayout.CENTER);
        scauneFrame.add(ecranPanel, BorderLayout.NORTH);

        Sala sala = service.getSala(film, ora, data);
        Scaun[][] scaune = sala.getScaune();

        Set<Scaun> scauneSelectate = new HashSet<>();

        // Folosim GridLayout rapid
        int rows = scaune.length;
        int cols = scaune[0].length;
        JPanel scaunePanel = new JPanel(new GridLayout(rows, cols, 5, 5));

        char rowLetter = 'A';
        for (int r = 0; r < scaune.length; r++) {
            int displayNum = 1;
            int middle1 = scaune[r].length / 2 - 1;
            int middle2 = scaune[r].length / 2;

            for (int c = 0; c < scaune[r].length; c++) {
                JButton btn = new JButton();
                Scaun scaun = scaune[r][c];

                if (c == middle1 || c == middle2) {
                    btn.setEnabled(false);
                    btn.setBackground(Color.GRAY);
                    btn.setText("-");
                } else {
                    btn.setText(String.valueOf(displayNum++));
                    if (scaun.esteRezervat()) {
                        btn.setEnabled(false);
                        btn.setBackground(Color.RED);
                    } else {
                        btn.setBackground(Color.GREEN);
                        btn.addActionListener(ae -> {
                            if (scauneSelectate.contains(scaun)) {
                                scauneSelectate.remove(scaun);
                                btn.setBackground(Color.GREEN);
                            } else {
                                scauneSelectate.add(scaun);
                                btn.setBackground(Color.ORANGE);
                            }
                        });
                    }
                }

                scaunePanel.add(btn);
            }
        }

        JScrollPane scroll = new JScrollPane(scaunePanel);
        scauneFrame.add(scroll, BorderLayout.CENTER);

        // jos: email + buton rezervare
        JPanel bottom = new JPanel();
        JTextField emailField = new JTextField(20);
        JButton rezervaBtn = new JButton("Rezervă");

        rezervaBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(scauneFrame, "Introduceți un email valid!");
                return;
            }

            service.salveazaRezervare(film, ora, data, email, scauneSelectate, sala);

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
