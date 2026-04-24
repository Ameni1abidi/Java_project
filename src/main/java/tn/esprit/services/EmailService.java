package tn.esprit.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.List;

public class EmailService {

    public void sendEmail(String to, String subject, String content) {

        Properties props = new Properties();

        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "1025");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");

        Session session = Session.getInstance(props);

        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress("no-reply@eduflex.com"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );

            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);

            System.out.println("✅ Mail envoyé à " + to);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔥 NEW: bulk emails
    public int sendToStudents(List<String> emails, String subject, String content) {

        int count = 0;

        for (String email : emails) {
            sendEmail(email, subject, content);
            count++;
        }

        return count;
    }

}
