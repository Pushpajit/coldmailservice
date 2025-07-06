package com.coldmailerservice.coldmailerservice.service;

import com.coldmailerservice.coldmailerservice.dto.EmailDTO;
import com.coldmailerservice.coldmailerservice.dto.PlatformPayload;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
public class GoogleSheetConfigService {
    @Value("${ApplicationName}")
    private String APPLICATION_NAME;
    @Value("${SpreadSheetID}")
    private String spreadsheetId;
    @Value("${SpreadSheetDefaultReadingRange}")
    private String range;

    @Autowired
    private GmailConfigService gmailConfigService;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();



    private Sheets getGoogleSheetService(String userName) throws Exception {
        Credential credential = gmailConfigService.getCredential(userName);

        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

    }
    
    public  List<List<Object>> getResponseValues(String verifiedGmail) throws Exception {
        Sheets googleSheetService = getGoogleSheetService(verifiedGmail);

        ValueRange response = googleSheetService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        return response.getValues();
    }

    // Iterating over all the EmailDTO to validated if it's authorized or not
    public void validateSheetGmail(List<EmailDTO> emailDTOList, PlatformPayload platformPayload) throws Exception {
        for(int i = 0; i < emailDTOList.size(); i++){
            if (gmailConfigService.isGmailValid(emailDTOList.get(i).getGmail())) {
                emailDTOList.get(i).setAuthStatus("Not Authorized");
                // If the gmail is not authorized, then update the status with the auth_url
                String authorizationUrl = gmailConfigService.getAuthorizationUrl(emailDTOList.get(i).getGmail());
                emailDTOList.get(i).setStatus(authorizationUrl);
            }else{
                emailDTOList.get(i).setAuthStatus("Authorized");
                emailDTOList.get(i).setStatus("READY");
            }

            // Provide the range which columns you want to update
            String range = "Sheet1!A" + (i + 2) + ":I" + (i + 2);

            //List of row data you want to update
            List<Object> rowData = List.of(
                    emailDTOList.get(i).getSentDate(),
                    emailDTOList.get(i).getUsername(),
                    emailDTOList.get(i).getGmail(),
                    emailDTOList.get(i).getAuthStatus(),
                    emailDTOList.get(i).getCompany(),
                    emailDTOList.get(i).getCompanyGmail(),
                    emailDTOList.get(i).getRole(),
                    emailDTOList.get(i).getTemplate(),
                    emailDTOList.get(i).getStatus()
            );

            updateSheet(rowData, platformPayload.getGmail(), range);
        }



    }

    // This function update the sheet row
    public void updateSheet(List<Object> rowData, String verifiedGmail, String range) throws Exception {

        // Get the google-sheet service (provide the gmail verified with google cloud API)
        Sheets sheetService = getGoogleSheetService(verifiedGmail);

        //It will the value for those columns which will get updated
        ValueRange valueRange = new ValueRange().setValues(List.of(rowData));

        sheetService.spreadsheets().values()
                .update(spreadsheetId, range, valueRange)
                .setValueInputOption("RAW")
                .execute();

    }

    
}
