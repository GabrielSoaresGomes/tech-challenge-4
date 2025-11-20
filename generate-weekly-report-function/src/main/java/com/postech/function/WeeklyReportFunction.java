package com.postech.function;

import com.postech.report.PDFReportGenerator;
import com.postech.service.WeeklyReportService;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WeeklyReportFunction {

    @Inject
    WeeklyReportService reportService;

    @Inject
    PDFReportGenerator pdfReportGenerator;

    @FunctionName("weekly-report")
    public void run(
            @TimerTrigger(
                    name = "timerInfo",
                    schedule = "0/10 * * * * *"
            ) String timerInfo,
            final ExecutionContext context
    ) {
        context.getLogger().info("Gerando relatório semanal...");

        var report = reportService.generateWeeklyReport();
        byte[] pdfBytes = pdfReportGenerator.generateReport(report);

        Path outputPath = Paths.get("weekly-report.pdf");
        try {
            Files.write(outputPath, pdfBytes);
            context.getLogger().info("PDF gerado em: " + outputPath.toAbsolutePath());
        } catch (IOException e) {
            context.getLogger().severe("Erro ao salvar PDF: " + e.getMessage());
        }
    }
}
