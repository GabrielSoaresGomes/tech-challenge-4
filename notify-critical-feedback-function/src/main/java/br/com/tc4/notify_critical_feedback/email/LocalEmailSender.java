package br.com.tc4.notify_critical_feedback.email;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import br.com.tc4.notify_critical_feedback.domain.Feedback;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class LocalEmailSender implements EmailSender {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String from;

    public LocalEmailSender() {
        this.host = getEnvOrDefault("SMTP_HOST", "localhost");
        this.port = Integer.parseInt(getEnvOrDefault("SMTP_PORT", "1025"));
        this.username = System.getenv("SMTP_USERNAME"); // pode ser null em ambiente local
        this.password = System.getenv("SMTP_PASSWORD");
        this.from = getEnvOrDefault("SMTP_FROM", "no-reply@local.test");
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public void sendEmail(List<String> recipients, Feedback feedback) {
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("Lista de destinatários vazia");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        boolean auth = username != null && !username.isBlank();
        props.put("mail.smtp.auth", String.valueOf(auth));
        props.put("mail.smtp.starttls.enable", "false");

        Session session;
        if (auth) {
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            };
            session = Session.getInstance(props, authenticator);
        } else {
            session = Session.getInstance(props);
        }

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            for (String recipient : recipients) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            }

            message.setSubject("Relatório Semanal de Feedbacks");

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Segue o relatório semanal em anexo.");

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);

            message.setContent(
                    """
                    <html>
                        <body>
                            <h1>Novo Feedback Crítico Recebido</h1>
                            <p><strong>Descrição:</strong> %s</p>
                            <p><strong>Nota:</strong> %d</p>
                            <p><strong>Data de envio:</strong> %s</p>
                        </body>
                    </html>
                    """.formatted(
                        feedback.description,
                        feedback.note,
                        getFormattedDate(feedback.sendDate)
                    ),
                    "text/html; charset=UTF-8"
            );

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar email via SMTP local", e);
        }
    }

    private Object getFormattedDate(LocalDateTime sendDate) {
        return Objects.nonNull(sendDate)
                ? DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(sendDate)
                : "N/A";
    }
}
