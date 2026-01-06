package com.postech.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.postech.db.DatabaseConnector;
import com.postech.domain.Feedback;
import com.postech.dto.FeedbackInputDTO;
import com.postech.logging.AppInsightsLogger;
import com.postech.logging.AppLogger;
import com.postech.queue.FeedbackQueueSenderFactory;
import com.postech.repository.FeedbackRepository;
import com.postech.service.FeedbackPersistenceService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PersistAndSendFeedbackFunction {

    private static final AppLogger LOGGER = new AppInsightsLogger();
    private static final DatabaseConnector DB_CONNECTOR = new DatabaseConnector(LOGGER);
    private static final FeedbackRepository FEEDBACK_REPOSITORY = new FeedbackRepository(DB_CONNECTOR);
    private static final FeedbackPersistenceService PERSISTENCE_SERVICE =
            new FeedbackPersistenceService(FEEDBACK_REPOSITORY, FeedbackQueueSenderFactory.create(LOGGER));
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @FunctionName("PersistAndSendFeedback")
        public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                dataType = "string"
            ) HttpRequestMessage<String> request,
            final ExecutionContext context) {

        LOGGER.info("Iniciando processamento de feedback...");

        try {
            String body = request.getBody();

            if (body == null || body.isBlank()) {
                LOGGER.warn("Body da requisição está vazio");
                return createErrorResponse(request, HttpStatus.BAD_REQUEST, "Corpo da requisição não pode estar vazio");
            }

            FeedbackInputDTO feedbackInput = objectMapper.readValue(body, FeedbackInputDTO.class);

            if (!isValidFeedback(feedbackInput)) {
                LOGGER.warn("Dados de feedback inválidos");
                return createErrorResponse(request, HttpStatus.BAD_REQUEST, "Dados inválidos: description não pode estar vazio e note deve estar entre 0 e 10");
            }

            if (!DB_CONNECTOR.testConnection()) {
                LOGGER.error("Banco de dados indisponível", null);
                return createErrorResponse(request, HttpStatus.SERVICE_UNAVAILABLE, "Banco de dados indisponível");
            }

            Feedback feedback = PERSISTENCE_SERVICE.persistAndSend(
                    feedbackInput.description,
                    feedbackInput.note,
                    feedbackInput.urgency
            );

            LOGGER.info("Feedback salvo com sucesso. ID: " + feedback.id);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", feedback.id);
            responseData.put("description", feedback.description);
            responseData.put("note", feedback.note);
            responseData.put("urgency", feedback.urgency);
            responseData.put("sendDate", feedback.sendDate.toString());

            String responseBody = objectMapper.writeValueAsString(responseData);

                return request.createResponseBuilder(HttpStatus.CREATED)
                    .body(responseBody)
                    .header("Content-Type", "application/json")
                    .build();

        } catch (Exception e) {
            LOGGER.error("Erro ao processar feedback: " + e.getMessage(), e);
            return createErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar feedback: " + e.getMessage());
        }
    }

    private boolean isValidFeedback(FeedbackInputDTO feedback) {
        if (feedback.description == null || feedback.description.isBlank()) {
            return false;
        }
        return feedback.note >= 0 && feedback.note <= 10;
    }

    private HttpResponseMessage createErrorResponse(HttpRequestMessage<String> request, HttpStatus status, String message) {
        try {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", message);
            String errorBody = objectMapper.writeValueAsString(errorData);

            return request.createResponseBuilder(status)
                    .body(errorBody)
                    .header("Content-Type", "application/json")
                    .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno")
                    .build();
        }
    }
}
