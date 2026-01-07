package br.com.tc4.notify_critical_feedback.email;

import com.azure.communication.email.*;
import com.azure.communication.email.models.*;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;

import br.com.tc4.notify_critical_feedback.domain.Feedback;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class AzureEmailSender implements EmailSender {

    private final String connectionString;
    private final String senderAddress;

    public AzureEmailSender() {
        this.connectionString = getRequiredEnv("ACS_CONNECTION_STRING");
        this.senderAddress = getRequiredEnv("ACS_SENDER_ADDRESS");
    }

    private String getRequiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Variável de ambiente obrigatória não definida: " + key);
        }
        return value;
    }

    @Override
    public void sendEmail(List<String> recipients, Feedback feedback) {
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("Lista de destinatários vazia");
        }

        EmailClient client = new EmailClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        EmailMessage message = new EmailMessage()
                .setSenderAddress(senderAddress)
                .setSubject("Feedback Crítico Recebido")
                .setBodyHtml(
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
                        )
                )
                .setToRecipients(recipients.toArray(new String[0]));

        SyncPoller<EmailSendResult, EmailSendResult> poller = client.beginSend(message);
        PollResponse<EmailSendResult> response = poller.waitForCompletion();
        EmailSendResult result = response.getValue();

        if (result == null || result.getStatus() != EmailSendStatus.SUCCEEDED) {
            throw new RuntimeException(
                    "Falha ao enviar email via Azure. Status: %".formatted(
                            result == null ? "null" : result.getStatus()
                    )
            );
        }
    }

    private Object getFormattedDate(LocalDateTime sendDate) {
        return Objects.nonNull(sendDate)
                ? DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(sendDate)
                : "N/A";
    }
}
