package com.postech.logging;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.telemetry.EventTelemetry;
import com.microsoft.applicationinsights.telemetry.ExceptionTelemetry;
import com.microsoft.applicationinsights.telemetry.SeverityLevel;

import java.util.HashMap;
import java.util.Map;

public class AppInsightsLogger implements AppLogger {

    private enum LogDestination {
        LOCAL,
        CLOUD,
        ALL
    }

    private final TelemetryClient telemetryClient;
    private final LogDestination destination;

    public AppInsightsLogger() {
        TelemetryConfiguration configuration = TelemetryConfiguration.getActive();
        String connectionString = System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING");
        if (connectionString != null && !connectionString.isBlank()) {
            configuration.setConnectionString(connectionString);
        }

        this.telemetryClient = new TelemetryClient(configuration);

        String mode = System.getenv("LOG_DESTINATION");
        if (mode == null) {
            mode = "cloud"; // default
        }

        switch (mode.toLowerCase()) {
            case "local":
                this.destination = LogDestination.LOCAL;
                break;
            case "all":
                this.destination = LogDestination.ALL;
                break;
            case "cloud":
            default:
                this.destination = LogDestination.CLOUD;
                break;
        }
    }

    @Override
    public void info(String message) {
        info(message, null);
    }

    @Override
    public void info(String message, Map<String, String> properties) {
        log("INFO", message, properties, null);
    }

    @Override
    public void warn(String message) {
        warn(message, null);
    }

    @Override
    public void warn(String message, Map<String, String> properties) {
        log("WARN", message, properties, null);
    }

    @Override
    public void error(String message, Throwable t) {
        error(message, t, null);
    }

    @Override
    public void error(String message, Throwable t, Map<String, String> properties) {
        log("ERROR", message, properties, t);
    }

    private void log(String severity,
                     String message,
                     Map<String, String> properties,
                     Throwable t) {

        if (destination == LogDestination.LOCAL || destination == LogDestination.ALL) {
            logLocal(severity, message, properties, t);
        }

        if (destination == LogDestination.CLOUD || destination == LogDestination.ALL) {
            logCloud(severity, message, properties, t);
        }
    }

    private void logLocal(String severity,
                          String message,
                          Map<String, String> properties,
                          Throwable t) {

        StringBuilder sb = new StringBuilder();
        sb.append("[").append(severity).append("] ").append(message);

        if (properties != null && !properties.isEmpty()) {
            sb.append(" | props=").append(properties);
        }

        System.out.println(sb);

        if (t != null) {
            t.printStackTrace(System.out);
        }
    }

    private void logCloud(String severity,
                          String message,
                          Map<String, String> properties,
                          Throwable t) {

        Map<String, String> props = properties != null
                ? new HashMap<>(properties)
                : new HashMap<>();

        props.put("severity", severity);
        props.put("message", message);

        EventTelemetry telemetry = new EventTelemetry("AppLog");
        telemetry.getProperties().putAll(props);
        telemetryClient.trackEvent(telemetry);

        if (t != null) {
            ExceptionTelemetry ex = new ExceptionTelemetry(t);
            ex.setSeverityLevel(SeverityLevel.Error);
            ex.getProperties().putAll(props);
            telemetryClient.trackException(ex);
        }
    }
}
