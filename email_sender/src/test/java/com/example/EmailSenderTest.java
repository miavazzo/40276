package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EmailSenderTest {
    private static final String TO = "massimiliano.iavazzo@capgemini.com";
    private static final String ATTACHMENT_PATH_1 = "C:\\Users\\miavazzo\\OneDrive - Capgemini\\Documents\\T. 40276 parametri email portale clienti - fatture Newatt\\Crypto101.pdf";
    private static final String ATTACHMENT_PATH_2 = "C:\\Users\\miavazzo\\OneDrive - Capgemini\\Documents\\T. 40276 parametri email portale clienti - fatture Newatt\\Form-Argon-Design-Form.pdf"; // Modifica questo percorso

    private static final String CLIENT_ID = System.getenv("CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("CLIENT_SECRET");
    private static final String TENANT_ID = System.getenv("TENANT_ID");
    private static final String USERNAME = System.getenv("USERNAME");

    public static void main(String[] args) {
        testTextEmailNoAttachment();
        testTextEmailWithAttachment();
        testTextEmailWithMultipleAttachments();
        testHtmlEmailNoAttachment();
        testHtmlEmailWithAttachment();
        testHtmlEmailWithMultipleAttachments();
    }

    private static void testTextEmailNoAttachment() {
        System.out.println("Test 1: Invio email in formato testuale senza allegato");
        String subject = "T.40240-Test Email Testuale Senza Allegato";
        String body = "Questa è un'email di test in formato testuale senza allegato.";
        sendEmail(CLIENT_ID, CLIENT_SECRET, TENANT_ID, USERNAME, TO, subject, body, new String[]{});
    }

    private static void testTextEmailWithAttachment() {
        System.out.println("Test 2: Invio email in formato testuale con allegato");
        String subject = "T.40240-Test Email Testuale Con Allegato";
        String body = "Questa è un'email di test in formato testuale con un allegato.";
        sendEmail(CLIENT_ID, CLIENT_SECRET, TENANT_ID, USERNAME, TO, subject, body, ATTACHMENT_PATH_1);
    }

    private static void testTextEmailWithMultipleAttachments() {
        System.out.println("Test 3: Invio email in formato testuale con più allegati");
        String subject = "T.40240-Test Email Testuale Con Più Allegati";
        String body = "Questa è un'email di test in formato testuale con più allegati.";
        sendEmail(CLIENT_ID, CLIENT_SECRET, TENANT_ID, USERNAME, TO, subject, body, ATTACHMENT_PATH_1, ATTACHMENT_PATH_2);
    }

    private static void testHtmlEmailNoAttachment() {
        System.out.println("Test 4: Invio email in formato HTML senza allegato");
        String subject = "T.40240-Test Email HTML Senza Allegato";
        String body = "<h1>Email HTML</h1><p>Questa è un'email di test in formato HTML senza allegato.</p>";
        sendEmail(CLIENT_ID, CLIENT_SECRET, TENANT_ID, USERNAME, TO, subject, body, (String[]) null);
    }

    private static void testHtmlEmailWithAttachment() {
        System.out.println("Test 5: Invio email in formato HTML con allegato");
        String subject = "T.40240-Test Email HTML Con Allegato";
        String body = "<h1>Email HTML con Allegato</h1><p>Questa è un'email di test in formato HTML con un allegato.</p>";
        sendEmail(CLIENT_ID, CLIENT_SECRET, TENANT_ID, USERNAME, TO, subject, body, ATTACHMENT_PATH_1);
    }

    private static void testHtmlEmailWithMultipleAttachments() {
        System.out.println("Test 6: Invio email in formato HTML con più allegati");
        String subject = "T.40240-Test Email HTML Con Più Allegati";
        String body = "<h1>Email HTML con Più Allegati</h1><p>Questa è un'email di test in formato HTML con più allegati.</p>";
        sendEmail(CLIENT_ID, CLIENT_SECRET, TENANT_ID, USERNAME, TO, subject, body, ATTACHMENT_PATH_1, ATTACHMENT_PATH_2);
    }

    private static void sendEmail(String clientId, String clientSecret, String tenantId, String username, String to, String subject, String body, String... attachmentPaths) {
        System.out.println("Iniziando il test di invio email...");
        System.out.println("Destinatario: " + to);
        System.out.println("Oggetto: " + subject);
        System.out.println("Corpo: " + body);

        List<String> validAttachments = new ArrayList<>();
        if (attachmentPaths != null && attachmentPaths.length > 0) {
            System.out.println("Allegati:");
            for (String path : attachmentPaths) {
                try {
                    long size = Files.size(Paths.get(path));
                    System.out.println("- " + path + " (Dimensione: " + size + " bytes)");
                    validAttachments.add(path);
                } catch (IOException e) {
                    System.err.println("Errore: Impossibile accedere all'allegato '" + path + "': " + e.getMessage());
                }
            }
        } else {
            System.out.println("Nessun allegato");
        }

        try {
            EmailSender.sendEmail(
                clientId, 
                clientSecret, 
                tenantId, 
                username, 
                to, 
                subject, 
                body, 
                validAttachments.toArray(new String[0])
            );
            System.out.println("Test completato con successo!");
        } catch (Exception e) {
            System.err.println("Errore durante l'invio dell'email:");
            e.printStackTrace();
        }
        System.out.println("--------------------");
    }
}
