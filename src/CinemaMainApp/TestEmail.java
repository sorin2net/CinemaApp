package CinemaMainApp;

import cinema.service.EmailService;

public class TestEmail {
    public static void main(String[] args) {
        EmailService emailService = new EmailService();
        emailService.trimiteConfirmare(
                "tlhchronomark@gmail.com",
                "Inception",
                "Sala 1",
                "18:00",
                "R1-C1; R1-C2"
        );
    }
}
