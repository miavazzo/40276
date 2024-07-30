package com.example;

import com.microsoft.aad.msal4j.*;
import com.microsoft.graph.requests.*;
import java.util.Properties;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.UserSendMailParameterSet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Collections;
import java.util.LinkedList;

import java.util.concurrent.CompletableFuture;

public class EmailSender {
    private static Properties config;

    static {
        try (InputStream input = EmailSender.class.getClassLoader().getResourceAsStream("config.properties")) {
            config = new Properties();
            if (input == null) {
                throw new IOException("Sorry, unable to find config.properties");
            }
            config.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static final String CLIENT_ID = config.getProperty("CLIENT_ID");
    private static final String CLIENT_SECRET = config.getProperty("CLIENT_SECRET");
    private static final String TENANT_ID = config.getProperty("TENANT_ID");
    private static final String USERNAME = config.getProperty("USERNAME");

    public static void sendEmail(String to, String subject, String body, String[] strings) throws Exception {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            throw new Exception("Impossibile ottenere il token di accesso");
        }

        GraphServiceClient<?> graphClient = getGraphClient(accessToken);

        Message message = new Message();
        message.subject = subject;
        ItemBody itemBody = new ItemBody();
        itemBody.contentType = BodyType.HTML;
        itemBody.content = body;
        message.body = itemBody;

        LinkedList<Recipient> toRecipientsList = new LinkedList<>();
        String[] recipients = to.split(",");
        for (String recipient : recipients) {
            Recipient toRecipient = new Recipient();
            EmailAddress emailAddress = new EmailAddress();
            emailAddress.address = recipient.trim();
            toRecipient.emailAddress = emailAddress;
            toRecipientsList.add(toRecipient);
}
message.toRecipients = toRecipientsList;

        if (strings != null && strings.length != 0) {
            FileAttachment attachment = new FileAttachment();
            attachment.name = Paths.get(strings[0]).getFileName().toString();
            attachment.contentType = "application/pdf"; // Assumendo che sia un PDF, altrimenti modifica appropriatamente
            attachment.oDataType = "#microsoft.graph.fileAttachment";
        
            byte[] fileContent = Files.readAllBytes(Paths.get(strings[0]));
            String base64Content = java.util.Base64.getEncoder().encodeToString(fileContent);
            attachment.contentBytes = base64Content.getBytes();
        
            LinkedList<Attachment> attachments = new LinkedList<>();
            attachments.add(attachment);
            message.attachments = new AttachmentCollectionPage(attachments, null);
        }

        graphClient.users(USERNAME)
                .sendMail(UserSendMailParameterSet
                        .newBuilder()
                        .withMessage(message)
                        .withSaveToSentItems(true)
                        .build())
                .buildRequest()
                .post();

        System.out.println("Email inviata con successo!");
        String base64Content = ""; // Initialize the base64Content variable
        System.out.println("Base64 content (primi 100 caratteri): " + base64Content.substring(0, Math.min(base64Content.length(), 100)));
    }

    private static String getAccessToken() throws Exception {
        IClientCredential credential = ClientCredentialFactory.createFromSecret(CLIENT_SECRET);
        ConfidentialClientApplication app = ConfidentialClientApplication
                .builder(CLIENT_ID, credential)
                .authority("https://login.microsoftonline.com/" + TENANT_ID)
                .build();

        ClientCredentialParameters parameters = ClientCredentialParameters
                .builder(Collections.singleton("https://graph.microsoft.com/.default"))
                .build();

        IAuthenticationResult result = app.acquireToken(parameters).join();
        return result.accessToken();
    }

    private static GraphServiceClient<?> getGraphClient(String accessToken) {
        return GraphServiceClient
                .builder()
                .authenticationProvider(request -> CompletableFuture.completedFuture(accessToken))
                .buildClient();
    }

    public static void sendEmailFromOracle(
        String to, String subject, String body, String attachmentPath) {
        try {
            sendEmail(to, subject, body, new String[] { attachmentPath });
            System.out.println("Email inviata con successo da Oracle!");
        } catch (Exception e) {
            System.err.println("Errore nell'invio dell'email da Oracle: " + e.getMessage());
            e.printStackTrace();
        }
    }
}