package cinema.gui;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.service.RezervareService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class CinemaGUI extends JFrame {
    private RezervareService service;
    private JPanel filmePanel;
    private JComboBox<String> zileSaptCombo;
    private JComboBox<String> zileLunaCombo;
    private JComboBox<String> oreCombo;
    private JTextField searchField;

    public CinemaGUI(RezervareService service) {
        this.service = service;

        setTitle("Cinema Reservation System");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Bara de căutare și dropdown ore
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        topPanel.add(new JLabel("Caută film:"));
        topPanel.add(searchField);

        // Construim setul de ore disponibile
        Set<String> oreDisponibileSet = new HashSet<>();
        for (Film film : service.getFilme()) {
            oreDisponibileSet.addAll(film.getOre());
        }

        oreCombo = new JComboBox<>();
        oreCombo.addItem("Oricând");
        oreDisponibileSet.stream().sorted().forEach(oreCombo::addItem);

        topPanel.add(new JLabel("Filtrează după ora:"));
        topPanel.add(oreCombo);

        add(topPanel, BorderLayout.NORTH);

        // Panel filme
        filmePanel = new JPanel();
        filmePanel.setLayout(new BoxLayout(filmePanel, BoxLayout.Y_AXIS));
        JScrollPane scrollFilme = new JScrollPane(filmePanel);

        // Panel zile
        JPanel zilePanel = new JPanel();
        zilePanel.setLayout(new BoxLayout(zilePanel, BoxLayout.Y_AXIS));
        zilePanel.setBorder(BorderFactory.createTitledBorder("Selectează ziua"));

        String[] zileSaptamana = {"Luni", "Marți", "Miercuri", "Joi", "Vineri", "Sâmbătă", "Duminică"};
        zileSaptCombo = new JComboBox<>(zileSaptamana);
        zilePanel.add(new JLabel("Zi săptămână:"));
        zilePanel.add(zileSaptCombo);

        String[] zileLuna = new String[31];
        for (int i = 0; i < 31; i++) zileLuna[i] = String.valueOf(i + 1);
        zileLunaCombo = new JComboBox<>(zileLuna);
        zilePanel.add(new JLabel("Zi lună:"));
        zilePanel.add(zileLunaCombo);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollFilme, zilePanel);
        splitPane.setDividerLocation(700);
        add(splitPane, BorderLayout.CENTER);

        Runnable afiseazaFilme = () -> {
            filmePanel.removeAll();
            String text = searchField.getText().toLowerCase();
            String oraSelectata = (String) oreCombo.getSelectedItem();

            for (Film film : service.getFilme()) {
                if (film.getTitlu().toLowerCase().contains(text)) {
                    if (!"Oricând".equals(oraSelectata) && !film.getOre().contains(oraSelectata)) {
                        continue;
                    }

                    JPanel filmPanel = new JPanel(new BorderLayout());
                    filmPanel.setBorder(BorderFactory.createTitledBorder(film.getTitlu()));

                    JPanel contentPanel = new JPanel(new BorderLayout());

                    // Imagine film
                    JLabel imageLabel = new JLabel();
                    File imageFile = new File("resources/" + film.getImaginePath());
                    if (imageFile.exists()) {
                        ImageIcon icon = new ImageIcon(new ImageIcon(imageFile.getPath())
                                .getImage().getScaledInstance(120, 180, Image.SCALE_SMOOTH));
                        imageLabel.setIcon(icon);
                    } else {
                        imageLabel.setText("Imagine lipsă");
                        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        imageLabel.setPreferredSize(new Dimension(120, 180));
                    }
                    contentPanel.add(imageLabel, BorderLayout.WEST);

                    // Butoane ore
                    JPanel buttonsPanel = new JPanel();
                    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
                    for (String ora : film.getOre()) {
                        JButton oraBtn = new JButton("Rezervă la " + ora);
                        oraBtn.addActionListener(e -> {
                            String ziSelectata = zileSaptCombo.getSelectedItem() + "-" + zileLunaCombo.getSelectedItem();
                            deschideEcranScaune(film, ora, ziSelectata);
                        });
                        buttonsPanel.add(oraBtn);
                    }
                    contentPanel.add(buttonsPanel, BorderLayout.CENTER);

                    filmPanel.add(contentPanel, BorderLayout.CENTER);
                    filmePanel.add(filmPanel);
                }
            }

            filmePanel.revalidate();
            filmePanel.repaint();
        };

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { afiseazaFilme.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { afiseazaFilme.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { afiseazaFilme.run(); }
        });

        oreCombo.addActionListener(e -> afiseazaFilme.run());

        afiseazaFilme.run();
    }

    private void deschideEcranScaune(Film film, String ora, String zi) {
        JFrame scauneFrame = new JFrame("Rezervare - " + film.getTitlu() + " (" + ora + ") Ziua: " + zi);
        scauneFrame.setSize(1000, 700);
        scauneFrame.setLayout(new BorderLayout());

        // Ecran
        JPanel ecranPanel = new JPanel();
        ecranPanel.setPreferredSize(new Dimension(0, 40));
        ecranPanel.setBackground(Color.LIGHT_GRAY);
        JLabel ecranLabel = new JLabel("ECRAN", SwingConstants.CENTER);
        ecranLabel.setFont(new Font("Arial", Font.BOLD, 16));
        ecranPanel.setLayout(new BorderLayout());
        ecranPanel.add(ecranLabel, BorderLayout.CENTER);
        scauneFrame.add(ecranPanel, BorderLayout.NORTH);

        Sala sala = service.getSala(film, ora, zi);
        Scaun[][] scaune = sala.getScaune();

        JPanel scaunePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        char rowLetter = 'A';
        for (int r = 0; r < scaune.length; r++) {
            gbc.gridy = r;

            // Etichetă rând
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

        // Panel jos pentru email și rezervare
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
