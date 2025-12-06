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

    private TelemetryClient telemetryClient;
    private LogDestination destination;

    public AppInsightsLogger() {
        String mode = System.getenv("LOG_DESTINATION");
        if (mode == null) {
            mode = "cloud";
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

        boolean cloudOk = initCloudTelemetryClientSafe();

        if (!cloudOk && (this.destination == LogDestination.CLOUD || this.destination == LogDestination.ALL)) {
            System.out.println("[WARN] Cloud logging desabilitado: Application Insights não configurado corretamente. Fallback para LOCAL.");
            this.destination = LogDestination.LOCAL;
        }
    }

    private boolean initCloudTelemetryClientSafe() {
        try {
            String ikey = System.getenv("APPINSIGHTS_INSTRUMENTATIONKEY");
            if (ikey != null) {
                ikey = ikey.trim();
            }

            if (ikey == null || ikey.isBlank() || "null".equalsIgnoreCase(ikey)) {
                String conn = System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING");
                if (conn != null) {
                    conn = conn.trim();
                }
                if (conn == null || conn.isBlank() || "InstrumentationKey=null".equalsIgnoreCase(conn)) {
                    return false;
                }

                ikey = extractInstrumentationKey(conn);
                if (ikey == null || ikey.isBlank() || "null".equalsIgnoreCase(ikey)) {
                    return false;
                }
            }

            TelemetryConfiguration configuration = TelemetryConfiguration.createDefault();
            configuration.setInstrumentationKey(ikey);

            this.telemetryClient = new TelemetryClient(configuration);
            return true;
        } catch (Exception e) {
            System.out.println("[WARN] Falha ao inicializar TelemetryClient de Application Insights: " + e.getMessage());
            e.printStackTrace(System.out);
            this.telemetryClient = null;
            return false;
        }
    }

    private String extractInstrumentationKey(String connectionString) {
        String[] parts = connectionString.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("InstrumentationKey=")) {
                return trimmed.substring("InstrumentationKey=".length());
            }
        }
        return null;
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

        if ((destination == LogDestination.CLOUD || destination == LogDestination.ALL) && telemetryClient != null) {
            logCloudSafe(severity, message, properties, t);
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

    private void logCloudSafe(String severity,
                              String message,
                              Map<String, String> properties,
                              Throwable t) {

        try {
            Map<String, String> props = properties != null
                    ? new HashMap<>(properties)
                    : new HashMap<>();

            props.put("severity", severity);
            props.put("message", message);
            props.put("source", "weekly-report");

            EventTelemetry telemetry = new EventTelemetry("AppLog");
            telemetry.getProperties().putAll(props);
            telemetryClient.trackEvent(telemetry);

            if (t != null) {
                ExceptionTelemetry ex = new ExceptionTelemetry(t);
                ex.setSeverityLevel(SeverityLevel.Error);
                ex.getProperties().putAll(props);
                telemetryClient.trackException(ex);
            }
        } catch (Exception e) {
            System.out.println("[WARN] Falha ao enviar log para Application Insights: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}
