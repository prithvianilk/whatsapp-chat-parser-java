package com.prithvianilk;

public record ParserOptions(boolean omitMedia) {
    public ParserOptions() {
        this(false);
    }
}