package br.com.tc4.notify_critical_feedback;

import com.microsoft.azure.functions.annotation.*;

import br.com.tc4.notify_critical_feedback.deserializer.Deserializer;
import br.com.tc4.notify_critical_feedback.domain.Feedback;
import br.com.tc4.notify_critical_feedback.logging.AppInsightsLogger;
import br.com.tc4.notify_critical_feedback.logging.AppLogger;
import br.com.tc4.notify_critical_feedback.service.NotifyCriticalFeedbackService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.microsoft.azure.functions.*;

public class QueueFunction {
    private static final AppLogger LOGGER = new AppInsightsLogger();
    private static final NotifyCriticalFeedbackService SERVICE = new NotifyCriticalFeedbackService();

    @FunctionName("QueueFunction")
    public void run(
            @QueueTrigger(name = "message", queueName = "%QueueName%", connection = "AzureWebJobsStorage") String message,
            final ExecutionContext context
    ) {
        log(context, "Iniciando envio do e-mail do feedback de urgência.");
        log(context, "Java Queue trigger function processed a message: " + message);

        List<String> admins = getAdminEmailsFromEnv();
        log(context, "Enviando e-mail sobre feedback crítico para os administradores: " + String.join(", ", admins));
        if (!admins.isEmpty()) {
            try {
                Feedback feedback = new Deserializer().deserialize(message, Feedback.class);

                SERVICE.notify(admins, feedback);

                String emailProvider = System.getenv("EMAIL_PROVIDER") == null ? "local" : System.getenv("EMAIL_PROVIDER");
                log(context, "Notifação enviada por e-mail via provider: " + emailProvider);
            } catch (Exception e) {
                log(context, "Erro ao enviar notificação: " + e.getMessage(), e);
                for (var trace : e.getStackTrace()) {
                    log(context, "\tat " + trace.toString());
                }
            }
        }

        log(context, "Finalização da execução da função");
    }

    private List<String> getAdminEmailsFromEnv() {
        String rawEmails = System.getenv("CRITICAL_FEEDBACK_NOTIFICATION_ADMINS");
        if (rawEmails == null || rawEmails.isBlank()) {
            LOGGER.warn("Variável CRITICAL_FEEDBACK_NOTIFICATION_ADMINS não definida no env, nenhum administrador configurado para receber email");
            return Collections.emptyList();
        }

        return Arrays.stream(rawEmails.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private void log(final ExecutionContext context, String message) {
        context.getLogger().info(message);
        LOGGER.info(message);
    }

    private void log(final ExecutionContext context, String message, Throwable error) {
        context.getLogger().severe(message);
        LOGGER.error(message, error);
    }
}
