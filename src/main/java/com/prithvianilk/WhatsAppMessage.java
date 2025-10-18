package com.prithvianilk;

import java.time.Instant;
import java.util.Objects;

public class WhatsAppMessage {
    private final Instant timestamp;
    private final String by;
    private final String content;

    public WhatsAppMessage(Instant timestamp, String by, String content) {
        this.timestamp = timestamp;
        this.by = by;
        this.content = content;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public String by() {
        return by;
    }

    public String content() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WhatsAppMessage that = (WhatsAppMessage) o;
        return Objects.equals(timestamp, that.timestamp) && Objects.equals(by, that.by) && Objects.equals(content, that.content);
    }


    @Override
    public int hashCode() {
        int result = timestamp.hashCode();
        result = 31 * result + by.hashCode();
        result = 31 * result + content.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "WhatsAppMessage{" +
                "timestamp=" + timestamp +
                ", by='" + by + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}

