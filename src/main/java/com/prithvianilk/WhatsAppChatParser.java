package com.prithvianilk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WhatsAppChatParser {
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("^\\[(\\d{2}/\\d{2}/\\d{2}), (\\d{1,2}:\\d{2}:\\d{2} [AP]M)\\] ([^:]+):(.*)$");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy, h:mm:ss a", Locale.ENGLISH);

    private final File file;
    private final ParserOptions options;

    public WhatsAppChatParser(String fileName) {
        this(fileName, new ParserOptions());
    }

    public WhatsAppChatParser(String fileName, ParserOptions parserOptions) {
        this.file = new File(fileName);
        this.options = parserOptions;
    }

    public Stream<WhatsAppMessage> getMessages() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        Iterator<WhatsAppMessage> iterator = new MessageIterator(reader, options);

        return StreamSupport
                .stream(((Iterable<WhatsAppMessage>) () -> iterator).spliterator(), false)
                .onClose(() -> {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static class MessageIterator implements Iterator<WhatsAppMessage> {
        private final BufferedReader reader;
        private final ParserOptions parserOptions;
        private String currentLine;
        private WhatsAppMessage nextMessage;

        public MessageIterator(BufferedReader reader, ParserOptions parserOptions) {
            this.reader = reader;
            this.parserOptions = parserOptions;
            try {
                currentLine = reader.readLine();
                advance();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean hasNext() {
            return nextMessage != null;
        }

        @Override
        public WhatsAppMessage next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            WhatsAppMessage result = nextMessage;
            try {
                advance();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        private void advance() throws IOException {
            if (currentLine == null) {
                nextMessage = null;
                return;
            }

            findFirstMatchingLine().ifPresent(matcher -> {
                try {
                    parseMatchedLine(matcher);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        private Optional<Matcher> findFirstMatchingLine() throws IOException {
            Matcher matcher = MESSAGE_PATTERN.matcher(currentLine);

            // Skip lines until we find a message header
            while (currentLine != null && !matcher.matches()) {
                currentLine = reader.readLine();
                if (currentLine != null) {
                    matcher = MESSAGE_PATTERN.matcher(currentLine);
                }
            }

            if (currentLine == null) {
                nextMessage = null;
                return Optional.empty();
            }
            return Optional.of(matcher);
        }

        private void parseMatchedLine(Matcher matcher) throws IOException {
            Instant timestamp = getTimestamp(matcher);
            String by = matcher.group(3);
            String content = getContent(matcher);

            if (parserOptions.omitMedia() && contentContainsMedia(content)) {
                advance();
                return;
            }

            nextMessage = new WhatsAppMessage(timestamp, by, content);
        }

        private boolean contentContainsMedia(String content) {
            // TODO: Handle actual media. For now, I'm handling pre-omitted media lines.
            return content.contains("image omitted") || content.contains("video omitted");
        }

        private String getContent(Matcher matcher) throws IOException {
            StringBuilder contentBuilder = new StringBuilder(matcher.group(4));
            // Read continuation lines
            currentLine = reader.readLine();
            while (currentLine != null) {
                matcher = MESSAGE_PATTERN.matcher(currentLine);
                if (matcher.matches()) {
                    // Found next message, stop here
                    break;
                }
                // Continuation line
                contentBuilder.append("\n").append(currentLine);
                currentLine = reader.readLine();
            }

            String content = contentBuilder.toString();
            if (content.charAt(0) == ' ') {
                return content.substring(1);
            }

            return content;
        }

        private Instant getTimestamp(Matcher matcher) {
            String dateTimeStr = matcher.group(1) + ", " + matcher.group(2);
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        }
    }
}
