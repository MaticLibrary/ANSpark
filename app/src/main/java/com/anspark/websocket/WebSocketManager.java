package com.anspark.websocket;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static WebSocketManager instance;
    private WebSocketClient webSocketClient;
    private String token;
    private MessageListener messageListener;

    public interface MessageListener {
        void onMessageReceived(String message);
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    private WebSocketManager() {}

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void connect(String token, MessageListener listener) {
        this.token = token;
        this.messageListener = listener;

        try {
            URI uri = new URI("ws://10.0.2.2:5566/ws");
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    Log.d(TAG, "WebSocket connected");
                    // Підписуємось на отримання повідомлень
                    subscribeToQueue();
                    if (messageListener != null) {
                        messageListener.onConnected();
                    }
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Message received: " + message);
                    if (messageListener != null) {
                        messageListener.onMessageReceived(message);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket closed: " + reason);
                    if (messageListener != null) {
                        messageListener.onDisconnected();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error: " + ex.getMessage());
                    if (messageListener != null) {
                        messageListener.onError(ex.getMessage());
                    }
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            Log.e(TAG, "URI error: " + e.getMessage());
        }
    }

    private void subscribeToQueue() {
        // Відправляємо STOMP subscribe frame
        if (webSocketClient != null && webSocketClient.isOpen()) {
            String subscribeFrame = "SUBSCRIBE\nid:sub-0\ndestination:/user/queue/messages\n\n\0";
            webSocketClient.send(subscribeFrame);
            Log.d(TAG, "Subscribed to /user/queue/messages");
        }
    }

    public void sendMessage(String matchId, String content) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            String sendFrame = "SEND\ndestination:/app/chat/" + matchId + "\n\n" + content + "\0";
            webSocketClient.send(sendFrame);
            Log.d(TAG, "Message sent to match: " + matchId);
        } else {
            Log.e(TAG, "WebSocket not connected");
        }
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }
}