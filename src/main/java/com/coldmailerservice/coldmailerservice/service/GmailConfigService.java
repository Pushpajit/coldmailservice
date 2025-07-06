package com.coldmailerservice.coldmailerservice.service;

import com.coldmailerservice.coldmailerservice.dto.EmailDTO;
import com.coldmailerservice.coldmailerservice.utils.EmailTemplateRenderer;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.sheets.v4.SheetsScopes;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

@Component
public class GmailConfigService {
    @Value("${ApplicationName}")
    private String APPLICATION_NAME;
    @Value("${TokenDirectoryPath}")
    private String TOKENS_DIRECTORY_PATH;
    @Value("${CredentialDirectoryPath}")
    private String CREDENTIALS_FILE_PATH;
    @Value("${RedirectURL}")
    private String REDIRECT_URL;
    @Value("${RegisteredGmail}")
    private String API_REGISTERED_GMAIL;

    @Autowired
    private GoogleSheetConfigService googleSheetConfigService;

    private static final List<String> SCOPES = List.of(GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_SEND, GmailScopes.GMAIL_READONLY, SheetsScopes.SPREADSHEETS);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static String USERNAME;


    @PostConstruct
    public void init() {
        System.out.println("APP NAME: " + APPLICATION_NAME);
        System.out.println("TOKENS_DIRECTORY_PATH: " + TOKENS_DIRECTORY_PATH);
    }

    private GoogleAuthorizationCodeFlow getGoogleAuthorizationCodeFlow() throws Exception{
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets googleClientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, googleClientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        return flow;
    }

    public String getAuthorizationUrl(String userGmail) throws Exception {
        resetUSERNAME();
        USERNAME = userGmail;

        GoogleAuthorizationCodeFlow flow = getGoogleAuthorizationCodeFlow();

            // Build the manual OAuth URL
            GoogleAuthorizationCodeRequestUrl authorizationUrl =
                    flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URL).setState(userGmail);


            return authorizationUrl.build();
    }



    // Get the gmail service
    public Gmail exchangeCodeForTokens(String code, String userGmail) throws Exception {
        GoogleAuthorizationCodeFlow flow = getGoogleAuthorizationCodeFlow();

        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(REDIRECT_URL)
                .execute();

        //TODO: problem:- getUSERNAME() this method will cause in excel sheet inconsistency row value update in STATUS column.
        //      [FIXED - using flow.setState(userGmail) to make auth_url]

        Credential credential = flow.createAndStoreCredential(tokenResponse, userGmail);

        return new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        }

    // Get the gmail service
    public Gmail getGmailService(String userName) throws Exception {
        GoogleAuthorizationCodeFlow flow = getGoogleAuthorizationCodeFlow();

        // Try to load existing credentials
        Credential credential = flow.loadCredential(userName);

        if (credential == null || credential.getAccessToken() == null){
            System.out.println("User not authorized. No credentials found for: " + userName);
            throw new IllegalStateException("User not authorized");
        }

        return new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public Credential getCredential(String gmail) throws Exception {
        GoogleAuthorizationCodeFlow flow = getGoogleAuthorizationCodeFlow();

        // Try to load existing credentials
        Credential credential = flow.loadCredential(gmail);

        if (credential == null || credential.getAccessToken() == null){
            System.out.println("User not authorized. No credentials found for: " + gmail);
            throw new IllegalStateException("User not authorized");
        }

        return credential;
    }

    public boolean isGmailValid(String gmail) throws Exception {
        GoogleAuthorizationCodeFlow flow = getGoogleAuthorizationCodeFlow();

        // Try to load existing credentials
        Credential credential = flow.loadCredential(gmail);

        return (credential == null || credential.getAccessToken() == null);
    }


    public void sendEmail(List<EmailDTO> emailDTOList) throws Exception {

        for (int i = 0; i < emailDTOList.size(); i++){
            if (emailDTOList.get(i).getStatus().equals("READY")){
                String body = EmailTemplateRenderer.renderTemplate(emailDTOList.get(i).getTemplate(), emailDTOList.get(i));
                Gmail gmailService = getGmailService(emailDTOList.get(i).getGmail());

                MimeMessage message = createEmail(
                        emailDTOList.get(i).getCompanyGmail(),             // To
                        emailDTOList.get(i).getGmail(),                   // From
                        "Job Application - " + emailDTOList.get(i).getRole(),  // Subject
                        body                                   // Body
                );


                // Sending the actual e-mail
                sendMessage(gmailService, "me", message);
                System.out.println("From " + emailDTOList.get(i).getGmail() + " email sent to " + emailDTOList.get(i).getCompanyGmail());

                // Setting the status & tracking-status to 'SENT'
                emailDTOList.get(i).setTrackingStatus("SENT");
                emailDTOList.get(i).setStatus("SENT");

                // Setting the range which columns should be edited
                String range = "Sheet1!I" + (i + 2) + ":K" + (i + 2);

                System.out.println("TRACKING_STATUS: "+ emailDTOList.get(i).getTrackingStatus().equals("SENT"));

                //List of row data you want to update in google sheet.
                List<Object> rowData = List.of(
                        emailDTOList.get(i).getStatus(),
                        emailDTOList.get(i).getTrackingStatus(),
                        emailDTOList.get(i).getTrackingStatus().equals("SENT")
                                ? String.valueOf(Integer.parseInt(emailDTOList.get(i).getEmailCount()) + 1)
                                : emailDTOList.get(i).getEmailCount()

                );

                // Updating the sheet (ALSO change the verifiedGmail whenever changes)
                googleSheetConfigService.updateSheet(rowData, API_REGISTERED_GMAIL, range);
                System.out.println("[INFO]: Sheet updated STATUS, TRACKING STATUS = SENT");

            }
        }
    }

    private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);

        //TODO:
        // 1. Use Google Drive API to download resume file and send as attachment with email.
        // 2. Add one extra column for putting the google drive link for your resume.

        return email;
    }

    public void sendMessage(Gmail service, String userId, MimeMessage email) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(rawMessageBytes);

        Message message = new Message();
        message.setRaw(encodedEmail);

        service.users().messages().send(userId, message).execute();
    }



    public String getUSERNAME(){
        return USERNAME;
    }

    private void resetUSERNAME(){
        USERNAME = "";
    }
}
