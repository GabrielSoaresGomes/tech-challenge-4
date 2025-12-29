package com.postech.storage;

import com.postech.logging.AppLogger;

public class ReportStorageFactory {

    public static ReportStorage create(AppLogger logger) {
        String mode = System.getenv("REPORT_STORAGE_MODE");
        if (mode == null || mode.isBlank()) {
            mode = "none";
        }

        switch (mode.toLowerCase()) {
            case "blob":
                return new BlobReportStorage(logger);
            case "none":
            default:
                return new NoopReportStorage(logger);
        }
    }
}
