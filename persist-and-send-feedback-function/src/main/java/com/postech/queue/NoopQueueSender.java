package com.postech.queue;

import com.postech.domain.Feedback;
import com.postech.logging.AppLogger;

public class NoopQueueSender implements FeedbackQueueSender {

    private final AppLogger logger;

    public NoopQueueSender(AppLogger logger) {
        this.logger = logger;
    }

    @Override
    public void sendFeedback(Feedback feedback) {
        logger.warn("Modo NOOP: Feedback não será enviado para a fila. ID: " + feedback.id);
    }
}
