package com.coldmailerservice.coldmailerservice.controller;

import com.coldmailerservice.coldmailerservice.dto.EmailDTO;
import com.coldmailerservice.coldmailerservice.dto.PlatformPayload;
import com.coldmailerservice.coldmailerservice.service.GmailConfigService;
import com.coldmailerservice.coldmailerservice.service.GoogleSheetConfigService;
import com.coldmailerservice.coldmailerservice.utils.ExcelToDTOMapper;
import com.google.api.services.gmail.Gmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class EmailController {
    @Autowired
    private GoogleSheetConfigService googleSheetConfigService;

    @Autowired
    private GmailConfigService gmailConfigService;


    @GetMapping("/auth")
    public ResponseEntity<?> getAuth(@RequestParam("username") String username) throws Exception {
        String authorizationUrl = gmailConfigService.getAuthorizationUrl(username);

        System.out.println("Creating token for username: " + username);

        return ResponseEntity.ok(Map.of("auth_url", authorizationUrl));
    }

    // This is a helper route which will automatically redirected & parse the token and save into the local disk.
    @GetMapping("/Callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam("code") String code, @RequestParam("state") String userGmail) throws Exception {
        Gmail gmailService = gmailConfigService.exchangeCodeForTokens(code, userGmail);
        System.out.println(gmailService);
        return ResponseEntity.ok("Authorization successful!" + gmailConfigService.getUSERNAME());
    }

    @GetMapping("/login")
    public ResponseEntity<?> getLogin(@RequestParam("username") String username) {
        try {
            Gmail gmailService = gmailConfigService.getGmailService(username);
            System.out.println("Login Completed, Gmail service granted");
            return ResponseEntity.ok("Login successful!" + gmailService.users());
        }catch (Exception ex){
            return ResponseEntity.status(401).body("User not authorized. No credentials found for user " + username);
        }
    }

    //TODO: Make this post mapping
    @GetMapping("/parse-sheet")
    public ResponseEntity<?> getParseSheet(@RequestParam("username") String username){
        System.out.println("[INFO]: Inside /parse-sheet controller");
        try{
            List<List<Object>> responseValues = googleSheetConfigService.getResponseValues(username);
            List<EmailDTO> list =  ExcelToDTOMapper.DTOMapper(responseValues);
            System.out.println(list);
            return ResponseEntity.status(200).body(list);

        }catch (Exception ex){
            return ResponseEntity.status(500).body(ex.toString());
        }
    }

    //TODO: [DONE] Make validate-email controller which will (Add or Modify google sheet columns if necessary also DTO class)
    //   --> Parse excel sheet user's gmail
    //   --> Go through every gmail if verified
    //   --> If not then modify all unverified cell with a OAuth 0.2  registration link
    //   --> Store credentials for that user
    //   --> Mark all verified cells as AUTHORIZED
    @PostMapping("/validate-email")
    public ResponseEntity<?> getValidateEmail(@RequestBody PlatformPayload platformPayload){
        System.out.println("[INFO]: Inside /validate-email controller");
        String verifiedGmail = platformPayload.getGmail();

        if (verifiedGmail == null || verifiedGmail.isEmpty()){
            return ResponseEntity.status(400).body("Mandatory field missing in request body 'gmail'");
        }

        try {
            List<List<Object>> responseValues = googleSheetConfigService.getResponseValues(verifiedGmail);
            List<EmailDTO> list =  ExcelToDTOMapper.DTOMapper(responseValues);

            googleSheetConfigService.validateSheetGmail(list, platformPayload);

            return ResponseEntity.status(200).body("Validation Done! Check the sheet");
        }catch (Exception ex){
            return ResponseEntity.status(400).body(ex.toString());
        }
    }


    // TODO: make controller to generate gmail template and send (which row has the status == READY)
    @GetMapping("/send-email")
    public ResponseEntity<?> sendEmail(){
        System.out.println("[INFO]: Inside /send-email controller");
        String username = "pushpajitnexus007@gmail.com";

        try{
            List<List<Object>> responseValues = googleSheetConfigService.getResponseValues(username);
            List<EmailDTO> list =  ExcelToDTOMapper.DTOMapper(responseValues);
            // System.out.println(list);
            gmailConfigService.sendEmail(list);
            return ResponseEntity.status(200).body(list);

        }catch (Exception ex){
            return ResponseEntity.status(500).body(ex.toString());
        }

    }



    @GetMapping("/healthcheck")
    public ResponseEntity<?> getHealthCheck() {

        System.out.println("GOOD HEALTH");
        return new ResponseEntity<String>("GOOD HEALTH!", HttpStatus.OK);
    }
}
