package com.coldmailerservice.coldmailerservice.utils;

import com.coldmailerservice.coldmailerservice.dto.EmailDTO;

import java.util.ArrayList;
import java.util.List;

public class ExcelToDTOMapper {

    public static List<EmailDTO> DTOMapper(List<List<Object>> responseValues) {
        List<EmailDTO> emailList = new ArrayList<>();

        for (List<Object> row : responseValues) {
            // Skip empty rows
            if (row == null || row.isEmpty()) continue;

            EmailDTO dto = new EmailDTO();

            dto.setSentDate(getValue(row, 0));
            dto.setUsername(getValue(row, 1));
            dto.setGmail(getValue(row, 2));
            dto.setAuthStatus(getValue(row, 3));
            dto.setCompany(getValue(row, 4));
            dto.setCompanyGmail(getValue(row, 5));
            dto.setRole(getValue(row, 6));
            dto.setTemplate(getValue(row, 7));
            dto.setStatus(getValue(row, 8));
            dto.setTrackingStatus(getValue(row, 9));
            dto.setEmailCount(getValue(row, 10));

            emailList.add(dto);
        }

        return emailList;
    }

    // Helper to avoid index out of bounds
    private static String getValue(List<Object> row, int index) {
        if (index < row.size()) {
            Object value = row.get(index);
            return value == null ? "" : value.toString().trim();
        }
        return "";
    }
}
