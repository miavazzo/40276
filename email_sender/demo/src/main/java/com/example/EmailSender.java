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
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Collections;
import java.util.LinkedList;

import java.util.concurrent.CompletableFuture;

public class EmailSender {

    public static void sendEmail(String clientId, String clientSecret, String tenantId, String username, String to, String subject, String body, String[] attachments) throws Exception {
        String accessToken = getAccessToken(clientId, clientSecret, tenantId);
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

        if (attachments != null && attachments.length != 0) {
            FileAttachment attachment = new FileAttachment();
            attachment.name = Paths.get(attachments[0]).getFileName().toString();
            attachment.contentType = "application/pdf"; // Assumendo che sia un PDF, altrimenti modifica appropriatamente
            attachment.oDataType = "#microsoft.graph.fileAttachment";

            byte[] fileContent = Files.readAllBytes(Paths.get(attachments[0]));
            String base64Content = java.util.Base64.getEncoder().encodeToString(fileContent);
            attachment.contentBytes = base64Content.getBytes();

            LinkedList<Attachment> attachmentsList = new LinkedList<>();
            attachmentsList.add(attachment);
            message.attachments = new AttachmentCollectionPage(attachmentsList, null);
        }

        graphClient.users(username)
                .sendMail(UserSendMailParameterSet
                        .newBuilder()
                        .withMessage(message)
                        .withSaveToSentItems(true)
                        .build())
                .buildRequest()
                .post();

        System.out.println("Email inviata con successo!");
    }

    private static String getAccessToken(String clientId, String clientSecret, String tenantId) throws Exception {
        IClientCredential credential = ClientCredentialFactory.createFromSecret(clientSecret);
        ConfidentialClientApplication app = ConfidentialClientApplication
                .builder(clientId, credential)
                .authority("https://login.microsoftonline.com/" + tenantId)
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
        String clientId, String clientSecret, String tenantId, String username, String to, String subject, String body, String attachmentPath) {
        try {
            sendEmail(clientId, clientSecret, tenantId, username, to, subject, body, new String[] { attachmentPath });
            System.out.println("Email inviata con successo da Oracle!");
        } catch (Exception e) {
            System.err.println("Errore nell'invio dell'email da Oracle: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
