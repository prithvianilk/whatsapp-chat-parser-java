package com.prithvianilk;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        WhatsAppChatParser parser = new WhatsAppChatParser("/Users/prithvianilkumar/Downloads/_chat.txt");
        parser.getMessages().forEach(System.out::println);
    }
}