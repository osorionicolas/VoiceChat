package com.nosorio.voiceChat;

import com.nosorio.voiceChat.client.Client;
import com.nosorio.voiceChat.server.Server;

public class Application {

    public static void main(String args[]) {
        new Server().runVOIP();
        new Client();
    }
}
