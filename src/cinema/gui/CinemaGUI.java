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
        Set<String> oreDisponibileSet = new HashSet<>();
        for (Film film : service.getFilme()) {
            oreDisponibileSet.addAll(film.getOre());
        }
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
        filmePanel.setLayout(new BoxLayout(filmePanel, BoxLayout.Y_AXIS));
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

        // setăm zilele pentru prima lună
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

        List<Film> filmeZi = service.getFilmePentruZi(data);

        for (Film film : filmeZi) {
            if (!film.getTitlu().toLowerCase().contains(text)) continue;
            if (!"Oricând".equals(oraSelectata) && !film.getOre().contains(oraSelectata)) continue;

            // Panel exterior pentru separare între filme
            JPanel filmContainer = new JPanel();
            filmContainer.setLayout(new BorderLayout());
            filmContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

            // Panel intern pentru poster + titlu + butoane
            JPanel filmPanel = new JPanel();
            filmPanel.setLayout(new BoxLayout(filmPanel, BoxLayout.X_AXIS));
            filmPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Poster
            JLabel imageLabel = new JLabel();
            File imageFile = new File("resources/" + film.getImaginePath());
            if (imageFile.exists()) {
                ImageIcon icon = new ImageIcon(new ImageIcon(imageFile.getPath())
                        .getImage().getScaledInstance(220, 330, Image.SCALE_SMOOTH));
                imageLabel.setIcon(icon);
            } else {
                imageLabel.setText("Imagine lipsă");
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                imageLabel.setPreferredSize(new Dimension(220, 330));
            }
            filmPanel.add(imageLabel);

            // Spatiu între poster și detalii
            filmPanel.add(Box.createHorizontalStrut(20));

            // Panel dreapta pentru titlu + butoane
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

            rightPanel.add(Box.createVerticalGlue()); // centru vertical

            // Titlu
            JLabel titluLabel = new JLabel(film.getTitlu());
            titluLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titluLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            rightPanel.add(titluLabel);
            rightPanel.add(Box.createVerticalStrut(15));

            // Butoane ore
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (String ora : film.getOre()) {
                JButton oraBtn = new JButton("Rezervă la " + ora);
                oraBtn.addActionListener(e -> deschideEcranScaune(film, ora, data));
                buttonsPanel.add(oraBtn);
            }
            rightPanel.add(buttonsPanel);

            rightPanel.add(Box.createVerticalGlue()); // centru vertical

            filmPanel.add(rightPanel);
            filmContainer.add(filmPanel, BorderLayout.CENTER);

            filmePanel.add(filmContainer);
            filmePanel.add(Box.createVerticalStrut(10));
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

        JPanel scaunePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        char rowLetter = 'A';
        for (int r = 0; r < scaune.length; r++) {
            gbc.gridy = r;

            gbc.gridx = 0;
            JLabel rowLabel = new JLabel("" + rowLetter++);
            scaunePanel.add(rowLabel, gbc);

            int middle1 = scaune[r].length / 2 - 1;
            int middle2 = scaune[r].length / 2;
            int displayNum = 1;

            for (int c = 0; c < scaune[r].length; c++) {
                gbc.gridy = r;

                JButton btn = new JButton();

                if (c == middle1 || c == middle2) {
                    btn.setEnabled(false);
                    btn.setBackground(Color.GRAY);
                    btn.setText("-");
                } else {
                    btn.setText(String.valueOf(displayNum++));
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
                }

                gbc.gridx = c + 1;
                scaunePanel.add(btn, gbc);
            }
        }

        JScrollPane scroll = new JScrollPane(scaunePanel);
        scauneFrame.add(scroll, BorderLayout.CENTER);

        // jos: email + buton rezervare
        JPanel bottom = new JPanel();
        JTextField emailField = new JTextField(20);
        JButton rezervaBtn = new JButton("Rezervă");

        rezervaBtn.addActionListener(e -> {
            if (emailField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(scauneFrame, "Introduceți un email valid!");
                return;
            }
            service.salveazaRezervare(film, ora, data, emailField.getText(), sala);
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
