package com.postech.queue;

import com.postech.domain.Feedback;

public interface FeedbackQueueSender {
    void sendFeedback(Feedback feedback);
}
