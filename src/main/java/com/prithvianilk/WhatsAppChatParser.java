package com.prithvianilk;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class WhatsAppChatParser {
    // Pattern to match WhatsApp messages in format: [DD/MM/YYYY, HH:MM:SS] Name: Message
    // or DD/MM/YY, HH:MM - Name: Message
    private static final Pattern MESSAGE_PATTERN = Pattern.compile(
            "^\\[?(\\d{1,2}/\\d{1,2}/\\d{2,4},\\s\\d{1,2}:\\d{2}(?::\\d{2})?)\\]?\\s*[-:]\\s*([^:]+):\\s*(.*)$"
    );
    
    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ofPattern("d/M/yyyy, HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm:ss"),
            DateTimeFormatter.ofPattern("d/M/yy, HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yy, HH:mm")
    };
    
    private final BufferedReader fileReader;
    
    public WhatsAppChatParser(String filename) throws IOException {
        this.fileReader = new BufferedReader(new FileReader(filename));
    }
    
    public Stream<WhatsAppMessage> getMessages() {
        return fileReader.lines()
                .map(this::parseMessage)
                .filter(message -> message != null);
    }
    
    private WhatsAppMessage parseMessage(String line) {
        Matcher matcher = MESSAGE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null;
        }
        
        String timestampStr = matcher.group(1);
        String by = matcher.group(2).trim();
        String content = matcher.group(3);
        
        Instant timestamp = parseTimestamp(timestampStr);
        if (timestamp == null) {
            return null;
        }
        
        return new WhatsAppMessage(timestamp, by, content);
    }
    
    private Instant parseTimestamp(String timestampStr) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(timestampStr, formatter);
                return dateTime.atZone(ZoneId.systemDefault()).toInstant();
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        return null;
    }
}
