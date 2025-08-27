package cinema.gui;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.service.RezervareService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class CinemaGUI extends JFrame {
    private RezervareService service;
    private JPanel filmePanel;
    private JComboBox<String> zileSaptCombo;
    private JComboBox<String> zileLunaCombo;

    public CinemaGUI(RezervareService service) {
        this.service = service;

        setTitle("Cinema Reservation System");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Bara de căutare
        JPanel topPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField();
        topPanel.add(new JLabel("Caută film: "), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
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

        // Split pane pentru filme și zile
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollFilme, zilePanel);
        splitPane.setDividerLocation(700);
        add(splitPane, BorderLayout.CENTER);

        Runnable afiseazaFilme = () -> {
            filmePanel.removeAll();
            String text = searchField.getText().toLowerCase();
            for (Film film : service.getFilme()) {
                if (film.getTitlu().toLowerCase().contains(text)) {
                    JPanel filmPanel = new JPanel();
                    filmPanel.setLayout(new BorderLayout());
                    filmPanel.setBorder(BorderFactory.createTitledBorder(film.getTitlu()));

                    JPanel contentPanel = new JPanel();
                    contentPanel.setLayout(new BorderLayout());

                    // Imagine film
                    JLabel imageLabel = new JLabel();
                    ImageIcon icon = null;
                    File imageFile = new File("resources/" + film.getImaginePath());
                    if (imageFile.exists()) {
                        icon = new ImageIcon(new ImageIcon(imageFile.getPath())
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

        afiseazaFilme.run();
    }

    private void deschideEcranScaune(Film film, String ora, String zi) {
        JFrame scauneFrame = new JFrame("Rezervare - " + film.getTitlu() + " (" + ora + ") Ziua: " + zi);
        scauneFrame.setSize(1000, 700);
        scauneFrame.setLayout(new BorderLayout());

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
        gbc.insets = new Insets(9, 9, 9, 9); // spatiere mai mare

        char rowLetter = 'A';
        for (int r = 0; r < scaune.length; r++) {
            gbc.gridy = r;

            // Etichetă rând
            gbc.gridx = 0;
            JLabel rowLabel = new JLabel("" + rowLetter++);
            scaunePanel.add(rowLabel, gbc);

            int middle = scaune[r].length / 2;
            for (int c = 0; c < scaune[r].length; c++) {
                gbc.gridy = r;
                gbc.gridx = (c < middle) ? c + 1 : c + 3;

                JButton btn = new JButton("" + (c + 1));
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

                btn.setToolTipText("Rând " + (r + 1) + ", Scaun " + (c + 1) + ", Ora " + ora);
                scaunePanel.add(btn, gbc);
            }
        }

        JScrollPane scroll = new JScrollPane(scaunePanel);
        scauneFrame.add(scroll, BorderLayout.CENTER);

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
