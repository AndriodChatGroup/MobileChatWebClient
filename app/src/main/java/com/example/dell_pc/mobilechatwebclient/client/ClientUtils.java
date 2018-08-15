package com.example.dell_pc.mobilechatwebclient.client;

import com.codebutler.android_websockets.WebSocketClient;

public class ClientUtils
{
    private WebSocketClient client;

    public ClientUtils(WebSocketClient client)
    {
        this.client = client;
        client.connect();
    }

    public void sendMessageToServer(String message)
    {
        if (client != null && client.isConnected())
            client.send(message);
    }

    public void close()
    {
        if (client != null && client.isConnected())
            client.disconnect();
    }
}
