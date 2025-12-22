package com.postech.queue;

import com.postech.logging.AppLogger;

public class FeedbackQueueSenderFactory {

    public static FeedbackQueueSender create(AppLogger logger) {
        String mode = System.getenv("FEEDBACK_QUEUE_MODE");
        if (mode == null || mode.isBlank()) {
            mode = "noop";
        }

        switch (mode.toLowerCase()) {
            case "azure":
                return new AzureQueueFeedbackSender(logger);
            case "noop":
            default:
                return new NoopQueueSender(logger);
        }
    }
}
