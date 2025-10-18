package com.prithvianilk;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
        assertEquals(3, messages.size());

        // Verify first message
        WhatsAppMessage msg1 = messages.getFirst();
        assertEquals("Person 1", msg1.by());
        assertEquals("Hi", msg1.content());
        assertTimestamp(msg1.timestamp(), 2025, 10, 15, 22, 1, 32);

        // Verify second message
        WhatsAppMessage msg2 = messages.get(1);
        assertEquals("Person 1", msg2.by());
        assertEquals("Bye. But what's up?", msg2.content());
        assertTimestamp(msg2.timestamp(), 2025, 10, 15, 22, 1, 42);

        // Verify third message (multi-line)
        WhatsAppMessage msg3 = messages.get(2);
        assertEquals("Person 2", msg3.by());
        assertEquals("nothing\nsometimes it's something\nlol", msg3.content());
        assertTimestamp(msg3.timestamp(), 2025, 10, 15, 22, 2, 4);
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
        File emptyFile = new File(filePath);
        emptyFile.getParentFile().mkdirs();
        emptyFile.createNewFile();

        WhatsAppChatParser parser = new WhatsAppChatParser(filePath);

        // When
        List<WhatsAppMessage> messages = parser.getMessages().toList();

        // Then
        assertEquals(0, messages.size());

        // Cleanup
        emptyFile.delete();
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
                                LocalDateTime.of(2025, 10, 17, 16, 9, 21).toInstant(ZoneOffset.UTC),
                                "Prithvi Anil Kumar",
                                "Hi"),
                        new WhatsAppMessage(
                                LocalDateTime.of(2025, 10, 17, 16, 52, 36).toInstant(ZoneOffset.UTC),
                                "Prithvi Anil Kumar",
                                "Hey")),
                messages);
    }

    private void assertTimestamp(Instant actual, int year, int month, int day, int hour, int minute, int second) {
        LocalDateTime actualDateTime = LocalDateTime.ofInstant(actual, ZoneId.systemDefault());
        assertEquals(year, actualDateTime.getYear());
        assertEquals(month, actualDateTime.getMonthValue());
        assertEquals(day, actualDateTime.getDayOfMonth());
        assertEquals(hour, actualDateTime.getHour());
        assertEquals(minute, actualDateTime.getMinute());
        assertEquals(second, actualDateTime.getSecond());
    }
}
