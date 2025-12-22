package com.postech.service;

import com.postech.domain.Feedback;
import com.postech.queue.FeedbackQueueSender;
import com.postech.repository.FeedbackRepository;

public class FeedbackPersistenceService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackQueueSender queueSender;

    public FeedbackPersistenceService(FeedbackRepository feedbackRepository, FeedbackQueueSender queueSender) {
        this.feedbackRepository = feedbackRepository;
        this.queueSender = queueSender;
    }

    public Feedback persistAndSend(String description, int note, boolean urgency) {
        Feedback feedback = feedbackRepository.save(description, note, urgency);

        if (urgency) {
            try {
                queueSender.sendFeedback(feedback);
            } catch (Exception e) {
                System.err.println("AVISO: Feedback salvo mas não foi enviado para a fila: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return feedback;
    }
}
