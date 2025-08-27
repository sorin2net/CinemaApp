package cinema.gui;

import cinema.model.Film;
import cinema.service.RezervareService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CinemaGUI extends JFrame {
    private RezervareService rezervareService;
    private JList<String> listaFilme;
    private JTextField randInput;
    private JTextField scaunInput;
    private JTextArea outputArea;

    public CinemaGUI(RezervareService service) {
        this.rezervareService = service;

        setTitle("Cinema Reservation System");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Lista filme
        DefaultListModel<String> model = new DefaultListModel<>();
        for (Film f : service.getFilme()) {
            model.addElement(f.getTitlu());
        }
        listaFilme = new JList<>(model);
        add(new JScrollPane(listaFilme), BorderLayout.WEST);

        // Panel rezervare
        JPanel rezervarePanel = new JPanel(new GridLayout(3, 2));
        rezervarePanel.add(new JLabel("Rand:"));
        randInput = new JTextField();
        rezervarePanel.add(randInput);

        rezervarePanel.add(new JLabel("Numar scaun:"));
        scaunInput = new JTextField();
        rezervarePanel.add(scaunInput);

        JButton rezervareButton = new JButton("Rezerva");
        rezervareButton.addActionListener(this::rezervaScaun);
        rezervarePanel.add(rezervareButton);

        add(rezervarePanel, BorderLayout.CENTER);

        // Output
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        add(new JScrollPane(outputArea), BorderLayout.SOUTH);
    }

    private void rezervaScaun(ActionEvent e) {
        int index = listaFilme.getSelectedIndex();
        if (index == -1) {
            outputArea.append("Selecteaza un film!\n");
            return;
        }
        Film film = rezervareService.getFilme().get(index);

        try {
            int rand = Integer.parseInt(randInput.getText());
            int nr = Integer.parseInt(scaunInput.getText());
            boolean succes = rezervareService.rezervaScaun(film, rand, nr);
            if (succes) {
                outputArea.append("Rezervare reusita la filmul " + film.getTitlu() +
                        " - Rand " + rand + ", Scaun " + nr + "\n");
            } else {
                outputArea.append("Scaunul este deja rezervat sau nu exista!\n");
            }
        } catch (NumberFormatException ex) {
            outputArea.append("Randul și numarul scaunului trebuie sa fie numere!\n");
        }
    }
}
