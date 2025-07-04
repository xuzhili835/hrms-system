package com.a0615.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CustomLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"),
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ISO_OFFSET_DATE_TIME,
        DateTimeFormatter.ISO_INSTANT
    };

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getText();
        
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        // 尝试不同的格式
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                // 对于包含时间信息的格式，只取日期部分
                if (dateString.contains("T")) {
                    return LocalDate.parse(dateString, formatter);
                } else {
                    return LocalDate.parse(dateString, formatter);
                }
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }
        
        // 如果所有格式都失败，尝试从ISO格式中提取日期部分
        try {
            if (dateString.contains("T")) {
                String datePart = dateString.substring(0, dateString.indexOf('T'));
                return LocalDate.parse(datePart, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        } catch (Exception e) {
            // 最后的尝试也失败了
        }
        
        throw new RuntimeException("无法解析日期格式: " + dateString + 
            "。支持的格式包括: yyyy-MM-dd, yyyy/MM/dd, ISO 8601等");
    }
} 