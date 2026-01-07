package br.com.tc4.notify_critical_feedback.email;

import java.util.List;

import br.com.tc4.notify_critical_feedback.domain.Feedback;

public interface EmailSender {
    void sendEmail(List<String> recipients, Feedback feedback);
}
