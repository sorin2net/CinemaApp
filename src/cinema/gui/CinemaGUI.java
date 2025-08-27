package cinema.gui;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.service.RezervareService;

import javax.swing.*;
import java.awt.*;

public class CinemaGUI extends JFrame {
    private RezervareService service;
    private JPanel filmePanel;

    public CinemaGUI(RezervareService service) {
        this.service = service;

        setTitle("Cinema Reservation System");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Bara de căutare + filtrare ore
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        topPanel.add(new JLabel("Caută film:"));
        topPanel.add(searchField);

        // ComboBox pentru filtrarea după ore
        JComboBox<String> oreFilter = new JComboBox<>();
        oreFilter.addItem("oricand"); // optiune default

        // Adaugăm toate orele disponibile în ComboBox
        for (Film film : service.getFilme()) {
            for (String ora : film.getOre()) {
                boolean exista = false;
                for (int i = 0; i < oreFilter.getItemCount(); i++) {
                    if (oreFilter.getItemAt(i).equals(ora)) {
                        exista = true;
                        break;
                    }
                }
                if (!exista) oreFilter.addItem(ora);
            }
        }

        topPanel.add(new JLabel("Filtrează după oră:"));
        topPanel.add(oreFilter);
        add(topPanel, BorderLayout.NORTH);

        // Panel filme
        filmePanel = new JPanel();
        filmePanel.setLayout(new BoxLayout(filmePanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(filmePanel);
        add(scroll, BorderLayout.WEST);

        // Runnable pentru afișarea filmelor cu filtrare după titlu și oră
        Runnable afiseazaFilme = () -> {
            filmePanel.removeAll();
            String text = searchField.getText().toLowerCase();
            String oraSelectata = (String) oreFilter.getSelectedItem();

            for (Film film : service.getFilme()) {
                // verificăm titlu
                if (!film.getTitlu().toLowerCase().contains(text)) continue;

                // verificăm ora
                boolean afiseaza = false;
                if ("oricand".equals(oraSelectata)) {
                    afiseaza = true;
                } else {
                    for (String ora : film.getOre()) {
                        if (ora.equals(oraSelectata)) {
                            afiseaza = true;
                            break;
                        }
                    }
                }
                if (!afiseaza) continue;

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

            filmePanel.revalidate();
            filmePanel.repaint();
        };

        // Legăm filtrarea la modificările din bara de căutare și dropdown
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { afiseazaFilme.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { afiseazaFilme.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { afiseazaFilme.run(); }
        });
        oreFilter.addActionListener(e -> afiseazaFilme.run());

        afiseazaFilme.run();
    }

    private void deschideEcranScaune(Film film, String ora) {
        JFrame scauneFrame = new JFrame("Rezervare - " + film.getTitlu() + " (" + ora + ")");
        scauneFrame.setSize(1000, 700);
        scauneFrame.setLayout(new BorderLayout());

        // Ecran centrat
        JPanel ecranPanel = new JPanel();
        ecranPanel.setPreferredSize(new Dimension(0, 40));
        ecranPanel.setBackground(Color.LIGHT_GRAY);
        JLabel ecranLabel = new JLabel("ECRAN", SwingConstants.CENTER);
        ecranLabel.setFont(new Font("Arial", Font.BOLD, 16));
        ecranPanel.setLayout(new BorderLayout());
        ecranPanel.add(ecranLabel, BorderLayout.CENTER);
        scauneFrame.add(ecranPanel, BorderLayout.NORTH);

        Sala sala = service.getSala(film, ora);
        Scaun[][] scaune = sala.getScaune();

        JPanel scaunePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(9, 9, 9, 9); // spațiu între scaune mărit de 3x

        char rowLetter = 'A';
        for (int r = 0; r < scaune.length; r++) {
            gbc.gridy = r;

            // Etichetă rând
            gbc.gridx = 0;
            JLabel rowLabel = new JLabel("" + rowLetter++);
            scaunePanel.add(rowLabel, gbc);

            int middle = scaune[r].length / 2; // punctul central al rândului
            for (int c = 0; c < scaune[r].length; c++) {
                gbc.gridy = r;

                // Dacă suntem după mijloc, sărim două coloane pentru alee
                if (c < middle) {
                    gbc.gridx = c + 1; // înainte de alee
                } else {
                    gbc.gridx = c + 3; // după alee
                }

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

            // adaugă două coloane goale ca alee centrală
            for (int empty = 0; empty < 2; empty++) {
                gbc.gridx = middle + 1 + empty;
                JLabel spacer = new JLabel();
                scaunePanel.add(spacer, gbc);
            }
        }

        JScrollPane scroll = new JScrollPane(scaunePanel);
        scauneFrame.add(scroll, BorderLayout.CENTER);

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
