package com.coldmailerservice.coldmailerservice.utils;

import com.coldmailerservice.coldmailerservice.dto.EmailDTO;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailTemplateRenderer {



    public static String renderTemplate(String template, EmailDTO emailDTO) {
        if (template == null || emailDTO == null) return "";

        return template
                .replaceAll("(?i)\\{\\{sentdate}}", safe(emailDTO.getSentDate()))
                .replaceAll("(?i)\\{\\{username}}", safe(emailDTO.getUsername()))
                .replaceAll("(?i)\\{\\{gmail}}", safe(emailDTO.getGmail()))
                .replaceAll("(?i)\\{\\{authstatus}}", safe(emailDTO.getAuthStatus()))
                .replaceAll("(?i)\\{\\{company}}", safe(emailDTO.getCompany()))
                .replaceAll("(?i)\\{\\{companygmail}}", safe(emailDTO.getCompanyGmail()))
                .replaceAll("(?i)\\{\\{role}}", safe(emailDTO.getRole()))
                .replaceAll("(?i)\\{\\{template}}", safe(emailDTO.getTemplate()))
                .replaceAll("(?i)\\{\\{status}}", safe(emailDTO.getStatus()))
                .replaceAll("(?i)\\{\\{trackingstatus}}", safe(emailDTO.getTrackingStatus()))
                .replaceAll("(?i)\\{\\{emailcount}}", safe(emailDTO.getEmailCount()));
    }


    private static String safe(String value) {
        return value == null ? "" : Matcher.quoteReplacement(value);
    }


}
