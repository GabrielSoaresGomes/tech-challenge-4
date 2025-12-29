package com.postech.storage;

public interface ReportStorage {
    void store(byte[] pdfBytes, String fileName);
}
