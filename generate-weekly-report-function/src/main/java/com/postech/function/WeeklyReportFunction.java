package com.postech.function;

import com.postech.db.DatabaseConnector;
import com.postech.dto.WeeklyReportDTO;
import com.postech.email.EmailSender;
import com.postech.email.EmailSenderFactory;
import com.postech.logging.AppInsightsLogger;
import com.postech.logging.AppLogger;
import com.postech.report.PDFReportGenerator;
import com.postech.repository.FeedbackRepository;
import com.postech.service.WeeklyReportService;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WeeklyReportFunction {

    private static final AppLogger LOGGER = new AppInsightsLogger();
    private static final DatabaseConnector DB_CONNECTOR = new DatabaseConnector(LOGGER);
    private static final FeedbackRepository FEEDBACK_REPOSITORY = new FeedbackRepository(DB_CONNECTOR);
    private static final WeeklyReportService WEEKLY_REPORT_SERVICE =
            new WeeklyReportService(FEEDBACK_REPOSITORY);
    private static final PDFReportGenerator PDF_REPORT_GENERATOR = new PDFReportGenerator();
    private static final EmailSender EMAIL_SENDER = EmailSenderFactory.createFromEnv();

    static {
        DB_CONNECTOR.testConnection();
    }

    @FunctionName("weekly-report")
    public void run(
            @TimerTrigger(
                    name = "timerInfo",
                    schedule = "%WEEKLY_REPORT_CRON%"
            ) String timerInfo,
            final ExecutionContext context
    ) {
        LOGGER.info("Iniciando geração de relatório semanal...");

        try {
            LocalDateTime now = LocalDateTime.now();

            WeeklyReportDTO report = WEEKLY_REPORT_SERVICE.generateWeeklyReport(now);
            byte[] pdfBytes = PDF_REPORT_GENERATOR.generateReport(report);

            String fileName = "weekly-report-" +
                    LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".pdf";

            Path outputPath = Path.of(fileName);
            Files.write(outputPath, pdfBytes);

            LOGGER.info("Arquivo do relatório semanal gerado com sucesso!");

            List<String> admins = List.of(
                    "gabrielsoares221@gmail.com"
            );

            LOGGER.info("Enviando relatório semanal por e-mail para administradores: " + String.join(", ", admins));

            EMAIL_SENDER.sendEmail(admins, pdfBytes);

            LOGGER.info("Relatório semanal enviado por e-mail via provider: " +
                    (System.getenv("EMAIL_PROVIDER") == null ? "local" : System.getenv("EMAIL_PROVIDER")));

        } catch (Exception e) {
            LOGGER.error("Erro ao gerar/enviar relatório semanal: " + e.getMessage(), e);
        }
    }
}
