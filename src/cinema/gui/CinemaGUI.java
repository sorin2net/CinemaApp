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
        topPanel.setBackground(new Color(45, 45, 45)); // dark mode
        searchField = new JTextField(15);
        searchField.setBackground(new Color(60, 60, 60));
        searchField.setForeground(Color.WHITE);
        topPanel.add(new JLabel("Caută film:") {{
            setForeground(Color.WHITE);
        }});
        topPanel.add(searchField);

        // ore disponibile
        Set<String> oreDisponibileSet = service.getFilme().stream()
                .flatMap(f -> f.getOre().stream())
                .collect(Collectors.toSet());

        oreCombo = new JComboBox<>();
        oreCombo.addItem("Oricând");
        oreDisponibileSet.stream().sorted().forEach(oreCombo::addItem);
        topPanel.add(new JLabel("Filtrează după ora:") {{
            setForeground(Color.WHITE);
        }});
        topPanel.add(oreCombo);

        // luna
        String[] luni = {"Septembrie", "Octombrie", "Noiembrie", "Decembrie"};
        lunaCombo = new JComboBox<>(luni);
        topPanel.add(new JLabel("Lună:") {{
            setForeground(Color.WHITE);
        }});
        topPanel.add(lunaCombo);

        // zi
        ziCombo = new JComboBox<>();
        topPanel.add(new JLabel("Zi:") {{
            setForeground(Color.WHITE);
        }});
        topPanel.add(ziCombo);

        add(topPanel, BorderLayout.NORTH);

        // ---------------- filme panel ----------------
        filmePanel = new JPanel();
        filmePanel.setBackground(new Color(45, 45, 45)); // dark mode
        JScrollPane scrollFilme = new JScrollPane(filmePanel);
        scrollFilme.getVerticalScrollBar().setUnitIncrement(20); // scroll mai rapid
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
            filmPanel.setBackground(new Color(45, 45, 45));

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
                imageLabel.setForeground(Color.WHITE);
            }
            filmPanel.add(imageLabel, BorderLayout.WEST);

            // Panel pentru titlu + gen + butoane
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            rightPanel.setBackground(new Color(60, 60, 60));

            // --- Panel pentru titlu + gen ---
            JPanel titluGenPanel = new JPanel();
            titluGenPanel.setLayout(new BoxLayout(titluGenPanel, BoxLayout.Y_AXIS));
            titluGenPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            titluGenPanel.setBackground(new Color(60, 60, 60));

            // JLabel pentru titlu + varsta (doar text colorat, fără casetă)
            int varsta = film.getRestrictieVarsta();
            String culoareVarsta;
            if (varsta == 3) culoareVarsta = "#4CAF50";      // verde
            else if (varsta == 7) culoareVarsta = "#FFEB3B"; // galben
            else if (varsta == 12) culoareVarsta = "#FF9800";// portocaliu
            else if (varsta == 15) culoareVarsta = "#F44336";// roșu
            else if (varsta == 18) culoareVarsta = "#FFFFFF";// alb pentru dark
            else culoareVarsta = "#9E9E9E";                  // gri

            String titluVarstaText = "<html><span style='color:white; font-weight:bold;'>"
                    + film.getTitlu() + "</span> "
                    + "(<span style='color:" + culoareVarsta + "; font-weight:bold;'>"
                    + varsta + "+</span>)</html>";
            JLabel titluVarstaLabel = new JLabel(titluVarstaText);
            titluVarstaLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titluGenPanel.add(titluVarstaLabel);

            // JLabel pentru gen
            JLabel genLabel = new JLabel(film.getGen() != null ? film.getGen() : "Gen necunoscut");
            genLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            genLabel.setForeground(Color.LIGHT_GRAY);
            genLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            titluGenPanel.add(genLabel);

            rightPanel.add(titluGenPanel);
            rightPanel.add(Box.createVerticalStrut(15)); // spațiu mic înainte de butoane

            // Butoane rezervare
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            buttonsPanel.setBackground(new Color(60, 60, 60));
            for (String ora : film.getOre()) {
                JButton oraBtn = new JButton("Rezervă la " + ora);
                oraBtn.setFont(new Font("Arial", Font.BOLD, 16));
                oraBtn.setPreferredSize(new Dimension(150, 40));
                oraBtn.addActionListener(e -> deschideEcranScaune(film, ora, data));
                buttonsPanel.add(oraBtn);
            }
            rightPanel.add(buttonsPanel);

            filmPanel.add(rightPanel, BorderLayout.CENTER);
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

        int rows = scaune.length;
        int cols = scaune[0].length;
        JPanel scaunePanel = new JPanel(new GridLayout(rows, cols, 5, 5));

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
        scroll.getVerticalScrollBar().setUnitIncrement(20); // scroll rapid
        scauneFrame.add(scroll, BorderLayout.CENTER);

        // jos: email + buton rezervare
        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(45, 45, 45));
        JTextField emailField = new JTextField(20);
        JButton rezervaBtn = new JButton("Rezervă");
        rezervaBtn.setFont(new Font("Arial", Font.BOLD, 16));
        rezervaBtn.setPreferredSize(new Dimension(120, 40));

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

        bottom.add(new JLabel("Email:") {{
            setForeground(Color.WHITE);
        }});
        bottom.add(emailField);
        bottom.add(rezervaBtn);
        scauneFrame.add(bottom, BorderLayout.SOUTH);

        scauneFrame.setVisible(true);
    }
}
