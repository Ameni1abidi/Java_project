package tn.esprit.services;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import tn.esprit.config.LocalSecrets;

import java.util.Properties;

public final class EmailService {
    public void sendTextEmail(String to, String subject, String body) throws MessagingException {
        String host = required("SMTP_HOST");
        String port = LocalSecrets.get("SMTP_PORT");
        String user = required("SMTP_USER");
        String pass = required("SMTP_PASS");
        String from = LocalSecrets.get("SMTP_FROM");
        if (from == null || from.isBlank()) from = user;

        boolean startTls = "true".equalsIgnoreCase(LocalSecrets.get("SMTP_STARTTLS"));

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", (port == null || port.isBlank()) ? "587" : port.trim());
        props.put("mail.smtp.starttls.enable", String.valueOf(startTls));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        message.setSubject(subject, "UTF-8");
        message.setText(body, "UTF-8");

        Transport.send(message);
    }

    private static String required(String key) {
        String v = LocalSecrets.get(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Configuration manquante: " + key + " (local.secrets.properties / env / -D)");
        }
        return v.trim();
    }
}

