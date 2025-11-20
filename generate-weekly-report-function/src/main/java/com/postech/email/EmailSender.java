package com.postech.email;

import java.util.List;

public interface EmailSender {
    void sendEmail(List<String> recipients, byte[] pdfBytes);
}
