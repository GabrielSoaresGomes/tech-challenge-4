package br.com.tc4.notify_critical_feedback.logging;

import java.util.Map;

public interface AppLogger {
    void info(String message);

    void info(String message, Map<String, String> properties);

    void warn(String message);

    void warn(String message, Map<String, String> properties);

    void error(String message, Throwable t);

    void error(String message, Throwable t, Map<String, String> properties);
}
