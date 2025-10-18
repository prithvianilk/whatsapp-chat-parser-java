package com.prithvianilk;

import java.time.Instant;

public record WhatsAppMessage(Instant timestamp, String by, String content) {
}
