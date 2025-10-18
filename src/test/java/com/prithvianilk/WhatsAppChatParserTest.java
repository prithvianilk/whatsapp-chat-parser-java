package com.prithvianilk;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WhatsAppChatParserTest {
    @Test
    void testParseMessages() throws IOException {
        // Given
        String filePath = "src/test/resources/example.txt";
        WhatsAppChatParser parser = new WhatsAppChatParser(filePath);

        // When
        List<WhatsAppMessage> messages = parser.getMessages().toList();

        // Then
        System.out.println(Instant.now());
        List<WhatsAppMessage> expectedMessages = List.of(
                new WhatsAppMessage(
                        toInstantFromIstTimestamp(2025, 10, 15, 22, 1, 32),
                        "Person 1",
                        "Hi"),
                new WhatsAppMessage(
                        toInstantFromIstTimestamp(2025, 10, 15, 22, 1, 42),
                        "Person 1",
                        "Bye. But what's up?"),
                new WhatsAppMessage(
                        toInstantFromIstTimestamp(2025, 10, 15, 22, 2, 4),
                        "Person 2",
                        "nothing\nsometimes it's something\nlol"));

        for (int i = 0; i < 3; ++i) {
            assertEquals(expectedMessages.get(i), messages.get(i));
        }
    }

    private Instant toInstantFromIstTimestamp(int year, int month, int day, int hour, int minute, int second) {
        return LocalDateTime.of(year, month, day, hour, minute, second).atZone(ZoneId.of("Asia/Kolkata")).toInstant();
    }

    @Test
    void testStreamCanBeClosed() throws IOException {
        // Given
        String filePath = "src/test/resources/example.txt";
        WhatsAppChatParser parser = new WhatsAppChatParser(filePath);

        // When/Then - should not throw exception
        try (var stream = parser.getMessages()) {
            long count = stream.count();
            assertEquals(3, count);
        }
    }

    @Test
    void testEmptyFile() throws IOException {
        // Given
        String filePath = "src/test/resources/empty.txt";
        WhatsAppChatParser parser = new WhatsAppChatParser(filePath);

        // When
        List<WhatsAppMessage> messages = parser.getMessages().toList();

        // Then
        assertEquals(0, messages.size());
    }

    @Test
    void testOmittedDataFile() throws IOException {
        // Given
        String filePath = "src/test/resources/ommited_data.txt";
        WhatsAppChatParser parser = new WhatsAppChatParser(filePath, new ParserOptions(true));

        // When
        List<WhatsAppMessage> messages = parser.getMessages().toList();

        // Then
        assertEquals(
                List.of(new WhatsAppMessage(
                                toInstantFromIstTimestamp(2025, 10, 17, 21, 39, 21),
                                "Prithvi Anil Kumar",
                                "Hi"),
                        new WhatsAppMessage(
                                toInstantFromIstTimestamp(2025, 10, 17, 22, 22, 36),
                                "Prithvi Anil Kumar",
                                "Hey")),
                messages);
    }
}
