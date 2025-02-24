package esprit.tn.pidevrh.email;

import javafx.scene.control.Alert;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {
    private static final String EMAIL = "mohamedhaddaji68@gmail.com";
    private static final String PASSWORD = "dchj cjgb rsdr srcn";

    public static boolean sendEmail(String recipient, String subject, String content) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL, PASSWORD);
            }
        });

        try {
            Message message = prepareMessage(session, EMAIL, recipient, subject, content);

            if (message != null) {
                Transport.send(message);
                new Alert(Alert.AlertType.INFORMATION, "Email sent successfully").showAndWait();
                return true;
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to prepare email").showAndWait();
                return false;
            }
        } catch (MessagingException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to send email: " + e.getMessage()).showAndWait();
            return false;
        }
    }

    private static Message prepareMessage(Session session, String from, String to, String subject, String content) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);
            return message;
        } catch (MessagingException e) {
            e.printStackTrace();
            return null;
        }
    }
}