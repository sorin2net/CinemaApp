package cinema.gui;
import javax.swing.*;
import java.awt.*;
import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.persistence.DatabaseManager;
import cinema.persistence.PersistentaRezervari;
import cinema.service.RezervareService;
import cinema.service.EmailService;
import cinema.network.ClientCinema;
import cinema.network.Mesaj;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
    private ClientCinema client;

    public CinemaGUI(RezervareService service) {
        this.service = service;

        setTitle("Cinema Reservation System");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ---------------- top panel ----------------
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(45, 45, 45));
        searchField = new JTextField(15);
        searchField.setBackground(new Color(60, 60, 60));
        searchField.setForeground(Color.WHITE);
        topPanel.add(new JLabel("Caută film:") {{ setForeground(Color.WHITE); }});
        topPanel.add(searchField);

        // ore disponibile
        Set<String> oreDisponibileSet = service.getFilme().stream()
                .flatMap(f -> f.getOre().stream())
                .collect(Collectors.toSet());

        oreCombo = new JComboBox<>();
        oreCombo.addItem("Oricând");
        oreDisponibileSet.stream().sorted().forEach(oreCombo::addItem);
        topPanel.add(new JLabel("Filtrează după ora:") {{ setForeground(Color.WHITE); }});
        topPanel.add(oreCombo);

        // luna
        String[] luni = {"Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie",
                "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"};
        lunaCombo = new JComboBox<>(luni);
        topPanel.add(new JLabel("Lună:") {{ setForeground(Color.WHITE); }});
        topPanel.add(lunaCombo);

        // zi
        ziCombo = new JComboBox<>();
        topPanel.add(new JLabel("Zi:") {{ setForeground(Color.WHITE); }});
        topPanel.add(ziCombo);

        // butonul Rezervările mele
        JButton rezervarileMeleBtn = new JButton("Rezervările mele");
        rezervarileMeleBtn.setFont(new Font("Arial", Font.BOLD, 14));
        rezervarileMeleBtn.addActionListener(e -> deschideRezervarileMele());
        topPanel.add(rezervarileMeleBtn);

        add(topPanel, BorderLayout.NORTH);

        // ---------------- filme panel ----------------
        filmePanel = new JPanel();
        filmePanel.setBackground(new Color(45, 45, 45));
        JScrollPane scrollFilme = new JScrollPane(filmePanel);
        scrollFilme.getVerticalScrollBar().setUnitIncrement(20);
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

        // ---------------- client server ----------------
        String host = "192.168.1.141";
        int port = 12345;
        try {
            client = new ClientCinema(host, port);

            // Setează callback-ul pentru mesaje de la server
            client.setOnMessageReceived(msg -> {
                SwingUtilities.invokeLater(() -> {
                    if ("update_sali".equals(msg.tip)) {
                        // Actualizează GUI fără pop-up
                        afiseazaFilme();
                        System.out.println("GUI actualizat din cauza modificării scaunelor");
                    } else if ("raspuns".equals(msg.tip) || "raspuns_anulare".equals(msg.tip)) {
                        // Răspunsurile sunt gestionate de GUI, nu mai afișăm nimic aici
                        afiseazaFilme();
                    }
                });
            });

        } catch (Exception e) {
            System.out.println("Nu s-a putut conecta la server, rezervarea va fi locala.");
            client = null;
        }

        actualizeazaZile();
        afiseazaFilme();
    }

    private void actualizeazaZile() {
        ziCombo.removeAllItems();
        int lunaIndex = lunaCombo.getSelectedIndex() + 1; // 1 = Ianuarie, 12 = Decembrie
        YearMonth ym = YearMonth.of(2025, lunaIndex);
        for (int zi = 1; zi <= ym.lengthOfMonth(); zi++) {
            ziCombo.addItem(zi);
        }
    }

    private void afiseazaFilme() {
        filmePanel.removeAll();

        int lunaIndex = lunaCombo.getSelectedIndex() + 1; // 1 = Ianuarie, 12 = Decembrie
        Integer zi = (Integer) ziCombo.getSelectedItem();
        if (zi == null) return;

        LocalDate data = LocalDate.of(2025, lunaIndex, zi);
        String text = searchField.getText().toLowerCase();

        // Filmele pentru ziua curenta
        List<Film> filmeZiFaraOra = service.getFilmePentruZi(data).stream()
                .filter(f -> f.getTitlu().toLowerCase().contains(text))
                .collect(Collectors.toList());

        // repopulez oreCombo doar cu orele disponibile în ziua curenta
        Object selectieAnterioara = oreCombo.getSelectedItem();
        java.awt.event.ActionListener[] ls = oreCombo.getActionListeners();
        for (java.awt.event.ActionListener l : ls) oreCombo.removeActionListener(l);

        oreCombo.removeAllItems();
        oreCombo.addItem("Oricând");

        Set<String> oreDisponibileSet = filmeZiFaraOra.stream()
                .flatMap(f -> f.getOre().stream())
                .collect(Collectors.toSet());

        oreDisponibileSet.stream().sorted().forEach(oreCombo::addItem);

        String oraSelectataTemp = selectieAnterioara != null ? selectieAnterioara.toString() : "Oricând";
        if (!"Oricând".equals(oraSelectataTemp) && !oreDisponibileSet.contains(oraSelectataTemp)) {
            oraSelectataTemp = "Oricând";
        }
        oreCombo.setSelectedItem(oraSelectataTemp);

        for (java.awt.event.ActionListener l : ls) oreCombo.addActionListener(l);

        final String oraFiltru = oraSelectataTemp;

        // aplic filtrul pe ora
        List<Film> filmeZi = filmeZiFaraOra.stream()
                .filter(f -> "Oricând".equals(oraFiltru) || f.getOre().contains(oraFiltru))
                .collect(Collectors.toList());

        // randare filme
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

            // Titlu + varsta
            int varsta = film.getRestrictieVarsta();
            String culoareVarsta;
            if (varsta == 3) culoareVarsta = "#4CAF50";
            else if (varsta == 7) culoareVarsta = "#FFEB3B";
            else if (varsta == 12) culoareVarsta = "#FF9800";
            else if (varsta == 15) culoareVarsta = "#F44336";
            else if (varsta == 18) culoareVarsta = "#FFFFFF";
            else culoareVarsta = "#9E9E9E";

            String titluVarstaText = "<html><span style='color:white; font-weight:bold;'>"
                    + film.getTitlu() + "</span> "
                    + "(<span style='color:" + culoareVarsta + "; font-weight:bold;'>"
                    + varsta + "+</span>)</html>";
            JLabel titluVarstaLabel = new JLabel(titluVarstaText);
            titluVarstaLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titluGenPanel.add(titluVarstaLabel);

            // Gen
            JLabel genLabel = new JLabel(
                    "<html><div style='margin-top:10px;'>"
                            + (film.getGen() != null ? film.getGen() : "Gen necunoscut")
                            + "</div></html>"
            );
            genLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            genLabel.setForeground(Color.LIGHT_GRAY);
            genLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            titluGenPanel.add(genLabel);

            rightPanel.add(titluGenPanel);
            rightPanel.add(Box.createVerticalStrut(15));

            // Butoane rezervare
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            buttonsPanel.setBackground(new Color(60, 60, 60));
            for (String oraBtnValue : film.getOre()) {
                JButton oraBtn = new JButton("Rezervă la " + oraBtnValue);
                oraBtn.setFont(new Font("Arial", Font.BOLD, 16));
                oraBtn.setMinimumSize(new Dimension(120, 40));
                oraBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                oraBtn.addActionListener(e -> deschideEcranScaune(film, oraBtnValue, data));
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
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scauneFrame.add(scroll, BorderLayout.CENTER);

        // email + buton rezervare
        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(45, 45, 45));
        JTextField emailField = new JTextField(20);
        JButton rezervaBtn = new JButton("Rezervă");
        rezervaBtn.setFont(new Font("Arial", Font.BOLD, 16));
        rezervaBtn.setPreferredSize(new Dimension(120, 40));

        rezervaBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            EmailService emailService = new EmailService();

            if (!emailService.esteEmailValid(email)) {
                JOptionPane.showMessageDialog(scauneFrame, "Introduceți un email valid!");
                return;
            }

            if (scauneSelectate.isEmpty()) {
                JOptionPane.showMessageDialog(scauneFrame, "Selectați cel puțin un scaun!");
                return;
            }

            Set<String> scauneStr = scauneSelectate.stream()
                    .map(s -> "R" + s.getRand() + "-C" + s.getNumar())
                    .collect(Collectors.toSet());

            if (client != null) {
                // Trimite la server
                client.trimiteRezervare(film.getTitlu(), ora, data.toString(), scauneStr, email);

                // Afișează UN SINGUR pop-up
                JOptionPane.showMessageDialog(
                        scauneFrame,
                        "Rezervare trimisă către server cu succes!",
                        "Succes",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                // Rezervare locală
                service.salveazaRezervare(film, ora, data, email, scauneSelectate, sala);

                JOptionPane.showMessageDialog(
                        scauneFrame,
                        "Rezervare efectuată local cu succes!",
                        "Succes",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            // Actualizează GUI
            afiseazaFilme();

            // Închide fereastra de scaune
            scauneFrame.dispose();
        });

        bottom.add(new JLabel("Email:") {{ setForeground(Color.WHITE); }});
        bottom.add(emailField);
        bottom.add(rezervaBtn);
        scauneFrame.add(bottom, BorderLayout.SOUTH);

        scauneFrame.setVisible(true);
    }

    private void actualizeazaScauneDinServer() {
        afiseazaFilme();
    }

    private void deschideRezervarileMele() {
        String email = JOptionPane.showInputDialog(this, "Introduceți adresa de email:");
        if (email == null || email.isBlank()) return;

        java.util.List<String> rezervari = new java.util.ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db");
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT titlu, data, ora_film, rand, coloana FROM rezervari WHERE email = ? ORDER BY data, ora_film"
             )) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String film = rs.getString("titlu");
                    String data = rs.getString("data");
                    String ora = rs.getString("ora_film");
                    int rand = rs.getInt("rand");
                    int coloana = rs.getInt("coloana");
                    rezervari.add(data + " - " + ora + " - " + film + " : R" + rand + "-C" + coloana);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Eroare la citirea bazei de date!");
            return;
        }

        if (rezervari.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu există rezervări pe această adresă.");
            return;
        }

        EmailService emailService = new EmailService();

        JFrame fereastra = new JFrame("Rezervările pentru " + email);
        fereastra.setSize(650, 500);
        fereastra.setLocationRelativeTo(this);

        JPanel panelRezervari = new JPanel();
        panelRezervari.setLayout(new BoxLayout(panelRezervari, BoxLayout.Y_AXIS));
        panelRezervari.setBackground(new Color(40, 40, 40));

        // Grupam rezervarile
        java.util.Map<String, java.util.List<String>> grupate = new java.util.LinkedHashMap<>();
        for (String r : rezervari) {
            String[] parts = r.split(" : ");
            String key = parts[0];
            String loc = parts[1];
            grupate.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(loc);
        }

        for (String key : grupate.keySet()) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(new Color(60, 63, 65));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(10, 10, 10, 10),
                    BorderFactory.createLineBorder(new Color(100, 100, 100), 1)
            ));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            String locuri = String.join(", ", grupate.get(key));
            JLabel lbl = new JLabel("<html><b style='color:#FFFFFF'>" + key + "</b><br><span style='color:#CCCCCC'>Locuri: " + locuri + "</span></html>");
            lbl.setFont(new Font("Arial", Font.PLAIN, 14));
            card.add(lbl, BorderLayout.CENTER);

            JButton anulareBtn = new JButton("Anulează");
            anulareBtn.setBackground(new Color(178, 34, 34));
            anulareBtn.setForeground(Color.WHITE);
            anulareBtn.setFocusPainted(false);
            anulareBtn.setFont(new Font("Arial", Font.BOLD, 14));

            anulareBtn.addActionListener(ev -> {
                int confirm = JOptionPane.showConfirmDialog(
                        fereastra,
                        "Sigur doriți să anulați rezervarea?",
                        "Confirmare anulare",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                boolean success = false;

                for (String loc : grupate.get(key)) {
                    String[] rc = loc.replace("R", "").replace("C", "").split("-");
                    int rand = Integer.parseInt(rc[0].trim());
                    int coloana = Integer.parseInt(rc[1].trim());

                    try {
                        String[] keyParts = key.split(" - ");
                        String dataRez = keyParts[0];
                        String oraFilm = keyParts[1];
                        String titluFilm = keyParts[2];

                        LocalDate dataF = LocalDate.parse(dataRez);
                        Set<String> scauneSet = new HashSet<>();
                        scauneSet.add("R" + rand + "-C" + coloana);

                        if (client != null) {
                            // Trimite cerere la server
                            client.trimiteAnulare(email, titluFilm, oraFilm, dataF.toString(), scauneSet);
                            success = true;
                        } else {
                            // Fallback local
                            DatabaseManager.stergeRezervare(email, titluFilm, dataRez, oraFilm, rand, coloana);

                            // Găsim filmul și ștergem din JSON
                            for (Film f : service.getFilme()) {
                                if (f.getTitlu().equals(titluFilm)) {
                                    PersistentaRezervari.stergeRezervare(email, titluFilm, dataF, oraFilm, f.getSala());
                                    emailService.trimiteAnulare(email, titluFilm, f.getSala().getNume(), oraFilm, dataF);
                                    success = true;
                                    break;
                                }
                            }
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (success) {
                    // Afișează UN SINGUR pop-up
                    JOptionPane.showMessageDialog(
                            fereastra,
                            client != null ? "Rezervare anulată cu succes!" : "Rezervare anulată local cu succes!",
                            "Succes",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    // Actualizează GUI-ul
                    afiseazaFilme();

                    // Închide fereastra de rezervări
                    fereastra.dispose();
                } else {
                    JOptionPane.showMessageDialog(
                            fereastra,
                            "Eroare la anularea rezervării!",
                            "Eroare",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            });

            card.add(anulareBtn, BorderLayout.EAST);
            panelRezervari.add(card);
            panelRezervari.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(panelRezervari);
        scroll.setBorder(null);
        fereastra.add(scroll, BorderLayout.CENTER);

        fereastra.setVisible(true);
    }
}