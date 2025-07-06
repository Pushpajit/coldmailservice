package com.coldmailerservice.coldmailerservice.dto;

public class EmailDTO {
    private String sentDate;
    private String username;
    private String gmail;
    private String authStatus;
    private String company;
    private String companyGmail;
    private String role;
    private String template;
    private String status;
    private String trackingStatus;
    private String emailCount;

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getAuthStatus() {
        return authStatus;
    }

    public void setAuthStatus(String authStatus) {
        this.authStatus = authStatus;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCompanyGmail() {
        return companyGmail;
    }

    public void setCompanyGmail(String companyGmail) {
        this.companyGmail = companyGmail;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTrackingStatus() {
        return trackingStatus;
    }

    public void setTrackingStatus(String trackingStatus) {
        this.trackingStatus = trackingStatus;
    }

    public String getEmailCount() {
        return emailCount;
    }

    public void setEmailCount(String emailCount) {
        this.emailCount = emailCount;
    }
}
