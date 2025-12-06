package com.postech.storage;

import com.postech.logging.AppLogger;

public class NoopReportStorage implements ReportStorage {

    private final AppLogger logger;

    public NoopReportStorage(AppLogger logger) {
        this.logger = logger;
    }

    @Override
    public void store(byte[] pdfBytes, String fileName) {
        logger.info("Storage mode = NONE. Relatório não será persistido em storage. Arquivo: " + fileName);
    }
}
