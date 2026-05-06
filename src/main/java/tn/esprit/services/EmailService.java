package tn.esprit.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.List;
import java.util.Properties;

public class EmailService {

    public void sendEmail(String to, String subject, String htmlContent) {

        try {
            Session session = createSession();

            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress("no-reply@eduflex.com"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to, false)
            );

            message.setSubject(subject, "UTF-8");

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);

            Transport.send(message);

            System.out.println("✅ Email sent to " + to);

        } catch (Exception e) {
            System.out.println("⚠️ Email not sent (SMTP issue)");
            e.printStackTrace();
        }
    }

    public void sendTextEmail(String to, String subject, String body) {
        sendEmail(to, subject, "<pre>" + body + "</pre>");
    }

    public int sendToStudents(List<String> emails, String subject, String content) {
        int count = 0;

        for (String email : emails) {
            sendEmail(email, subject, content);
            count++;
        }

        return count;
    }

    private Session createSession() {

        Properties props = new Properties();
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "1025");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");

        return Session.getInstance(props);
    }
}