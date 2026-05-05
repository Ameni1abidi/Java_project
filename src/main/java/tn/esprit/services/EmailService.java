package tn.esprit.services;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import tn.esprit.config.LocalSecrets;

import java.util.List;
import java.util.Properties;

public final class EmailService {

    // ✅ Send single email using SMTP config
    public void sendEmail(String to, String subject, String content) {
        try {
            Session session = createSession();

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(getFromAddress()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject(subject, "UTF-8");
            message.setText(content, "UTF-8");

            Transport.send(message);

            System.out.println("✅ Email sent to " + to);

        } catch (Exception e) {
            System.out.println("⚠️ Email not sent (SMTP issue)");
            e.printStackTrace();
        }
    }

    // ✅ Alias method (for compatibility)
    public void sendTextEmail(String to, String subject, String body) {
        sendEmail(to, subject, body);
    }

    // ✅ Bulk email sending
    public int sendToStudents(List<String> emails, String subject, String content) {
        int count = 0;

        for (String email : emails) {
            sendEmail(email, subject, content);
            count++;
        }

        return count;
    }

    // ✅ Create SMTP session
    private Session createSession() {
        String host = required("SMTP_HOST");
        String port = LocalSecrets.get("SMTP_PORT");
        String user = required("SMTP_USER");
        String pass = required("SMTP_PASS");

        boolean startTls = "true".equalsIgnoreCase(LocalSecrets.get("SMTP_STARTTLS"));

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", (port == null || port.isBlank()) ? "587" : port.trim());
        props.put("mail.smtp.starttls.enable", String.valueOf(startTls));

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });
    }

    // ✅ Get sender address
    private String getFromAddress() {
        String from = LocalSecrets.get("SMTP_FROM");
        if (from == null || from.isBlank()) {
            from = required("SMTP_USER");
        }
        return from;
    }

    // ✅ Required config
    private static String required(String key) {
        String value = LocalSecrets.get(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing configuration: " + key);
        }

        return value.trim();
    }
}