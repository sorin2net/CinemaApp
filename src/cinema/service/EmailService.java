package cinema.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Properties;

public class EmailService {

    private final String fromEmail;
    private final String appPassword;



    public EmailService() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config/email.properties")) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Nu am putut încărca fișierul email.properties!");
        }

        fromEmail = props.getProperty("fromEmail");
        appPassword = props.getProperty("appPassword");
    }

    public boolean esteEmailValid(String email) {
        if (email == null || email.isBlank()) return false;
        try {
            InternetAddress addr = new InternetAddress(email);
            addr.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }

    public void trimiteConfirmare(String toEmail, String numeFilm, String numeSala,
                                  String ora, String scaune) {

        if (!esteEmailValid(toEmail)) {
            System.out.println("Adresa de email invalidă: " + toEmail);
            return;
        }

        sendEmail(toEmail,
                "Confirmare rezervare la cinema",
                "Rezervarea ta a fost înregistrată!\n\n" +
                        "Film: " + numeFilm + "\n" +
                        "Sală: " + numeSala + "\n" +
                        "Ora: " + ora + "\n" +
                        "Scaune: " + scaune + "\n\n" +
                        "Mulțumim! Te așteptăm la film."
        );
    }

    public void trimiteAnulare(String toEmail, String numeFilm, String numeSala,
                               String ora, LocalDate data) {

        if (!esteEmailValid(toEmail)) {
            System.out.println("Adresa de email invalidă: " + toEmail);
            return;
        }

        sendEmail(toEmail,
                "Anulare rezervare la cinema",
                "Rezervarea ta pentru filmul \"" + numeFilm + "\" din data " + data +
                        ", ora " + ora + " la sala \"" + numeSala + "\" a fost anulată.\n\n" +
                        "Mulțumim!"
        );
    }


    private void sendEmail(String toEmail, String subject, String text) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject(subject);
            msg.setText(text);

            Transport.send(msg);
            System.out.println("Email trimis către " + toEmail + " cu subiect: " + subject);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
