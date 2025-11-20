package com.postech.email;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailAttachment;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;

import java.util.List;

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
    public void sendEmail(List<String> recipients, byte[] pdfBytes) {
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("Lista de destinatários vazia");
        }

        EmailClient client = new EmailClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        EmailMessage message = new EmailMessage()
                .setSenderAddress(senderAddress)
                .setSubject("Relatório Semanal de Feedbacks")
                .setBodyPlainText("Segue o relatório semanal em anexo.")
                .setToRecipients(recipients.toArray(new String[0]));

        EmailAttachment attachment = new EmailAttachment(
                "weekly-report.pdf",
                "application/pdf",
                BinaryData.fromBytes(pdfBytes)
        );
        message.setAttachments(attachment);

        SyncPoller<EmailSendResult, EmailSendResult> poller = client.beginSend(message);
        PollResponse<EmailSendResult> response = poller.waitForCompletion();
        EmailSendResult result = response.getValue();

        if (result == null || result.getStatus() != EmailSendStatus.SUCCEEDED) {
            throw new RuntimeException("Falha ao enviar email via Azure. Status: " +
                    (result == null ? "null" : result.getStatus()));
        }
    }
}
