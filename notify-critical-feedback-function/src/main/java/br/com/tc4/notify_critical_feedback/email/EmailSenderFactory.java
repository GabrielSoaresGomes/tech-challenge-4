package br.com.tc4.notify_critical_feedback.email;

public class EmailSenderFactory {
    private EmailSenderFactory() {
    }

    public static EmailSender createFromEnv() {
        String provider = System.getenv("EMAIL_PROVIDER");
        if (provider != null && provider.equalsIgnoreCase("azure")) {
            return new AzureEmailSender();
        }

        return new LocalEmailSender();
    }
}
