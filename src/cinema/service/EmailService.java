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

import java.util.Properties;

public class EmailService {

    // pune aici adresa ta Gmail
    private final String fromEmail = "denisdumitriu95@gmail.com";

    // pune aici App Password-ul generat în contul Google (nu parola normală!)
    private final String appPassword = "mnak amow knob lqkm";

    /** Validare sigură folosind Jakarta Mail */
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

    /** Trimite email text simplu cu detaliile rezervării */
    public void trimiteConfirmare(String toEmail, String numeFilm, String numeSala,
                                  String ora, String scaune) {

        if (!esteEmailValid(toEmail)) {
            System.out.println("Adresa de email invalidă: " + toEmail);
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // autentificare cu App Password
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
            msg.setSubject("Confirmare rezervare la cinema");
            msg.setText(
                    "Rezervarea ta a fost înregistrată!\n\n" +
                            "Film: " + numeFilm + "\n" +
                            "Sală: " + numeSala + "\n" +
                            "Ora: " + ora + "\n" +
                            "Scaune: " + scaune + "\n\n" +
                            "Mulțumim! Te așteptăm la film."
            );

            Transport.send(msg);
            System.out.println("Email de confirmare trimis către " + toEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}