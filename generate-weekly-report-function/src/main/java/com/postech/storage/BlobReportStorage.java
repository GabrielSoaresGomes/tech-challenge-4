package com.postech.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.postech.logging.AppLogger;

import java.io.ByteArrayInputStream;

public class BlobReportStorage implements ReportStorage {

    private final BlobContainerClient containerClient;
    private final AppLogger logger;

    public BlobReportStorage(AppLogger logger) {
        this.logger = logger;

        String connectionString = getRequiredEnv("STORAGE_CONNECTION_STRING");
        String containerName = getRequiredEnv("STORAGE_CONTAINER_REPORTS");

        this.containerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();

        if (!this.containerClient.exists()) {
            this.containerClient.create();
            logger.info("Container de storage criado: " + containerName);
        }
    }

    @Override
    public void store(byte[] pdfBytes, String fileName) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(fileName);

            logger.info("Enviando relatório para Blob Storage. Blob: " + fileName);

            try (ByteArrayInputStream dataStream = new ByteArrayInputStream(pdfBytes)) {
                blobClient.upload(dataStream, pdfBytes.length, true);
            }

            logger.info("Relatório armazenado com sucesso no Blob Storage. Blob URL: " + blobClient.getBlobUrl());
        } catch (Exception e) {
            logger.error("Falha ao armazenar relatório no Blob Storage: " + e.getMessage(), e);
        }
    }

    private String getRequiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Variável de ambiente obrigatória não definida: " + key);
        }
        return value;
    }
}
