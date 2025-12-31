package br.com.tc4.notify_critical_feedback.service;

import java.util.List;

import br.com.tc4.notify_critical_feedback.domain.Feedback;
import br.com.tc4.notify_critical_feedback.email.EmailSender;
import br.com.tc4.notify_critical_feedback.email.EmailSenderFactory;

public class NotifyCriticalFeedbackService {
    private final EmailSender emailSender = EmailSenderFactory.createFromEnv();

    public NotifyCriticalFeedbackService() {
    }

    public void notify(List<String> recipients, Feedback feedback) {
        this.emailSender.sendEmail(
            recipients,
            feedback
        );
    }
}
