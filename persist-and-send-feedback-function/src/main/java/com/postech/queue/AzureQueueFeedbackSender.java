package com.postech.queue;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.postech.domain.Feedback;
import com.postech.logging.AppLogger;

import java.util.Base64;

public class AzureQueueFeedbackSender implements FeedbackQueueSender {

    private final QueueClient queueClient;
    private final AppLogger logger;
    private static final ObjectMapper objectMapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public AzureQueueFeedbackSender(AppLogger logger) {
        this.logger = logger;
        
        String connectionString = getEnvOrThrow("STORAGE_CONNECTION_STRING");
        String queueName = getEnvOrThrow("FEEDBACK_QUEUE_NAME");

        this.queueClient = new QueueClientBuilder()
                .connectionString(connectionString)
                .queueName(queueName)
                .buildClient();
    }

    @Override
    public void sendFeedback(Feedback feedback) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(feedback);
            String encodedMessage = Base64.getEncoder().encodeToString(jsonMessage.getBytes());
            
            queueClient.sendMessage(encodedMessage);
            
            logger.info("Feedback enviado para a fila com sucesso. ID: " + feedback.id);
        } catch (Exception e) {
            logger.error("Erro ao enviar feedback para a fila", e);
            throw new RuntimeException("Erro ao enviar feedback para a fila", e);
        }
    }

    private String getEnvOrThrow(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Variável de ambiente obrigatória não definida: " + key);
        }
        return value;
    }
}
