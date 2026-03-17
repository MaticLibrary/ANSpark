package com.anspark.services;

import android.util.Log;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.internal.ws.WebSocketProtocol;

public class WebSocketService {
    private static final String TAG = "WebSocketService";
    
    public interface WebSocketListener {
        void onConnected();
        void onMessage(String message);
        void onClosed();
        void onError(String message);
    }

    private WebSocket webSocket;
    private WebSocketListener listener;
    private final OkHttpClient client;
    private final Gson gson;

    public WebSocketService() {
        this.client = new OkHttpClient.Builder()
                .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build();
        this.gson = new Gson();
    }

    public void connect(String url, WebSocketListener listener) {
        this.listener = listener;
        
        Request request = new Request.Builder()
                .url(url)
                .header("Sec-WebSocket-Key", generateWebSocketKey())
                .header("Sec-WebSocket-Version", "13")
                .header("Connection", "Upgrade")
                .header("Upgrade", "websocket")
                .build();

        webSocket = client.newWebSocket(request, new okhttp3.WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d(TAG, "WebSocket Connected");
                if (listener != null) {
                    listener.onConnected();
                }
                // Wysyłaj STOMP CONNECT ramkę
                sendStompFrame("CONNECT\naccept-version:1.0,1.1,1.2\nheart-beat:0,0\n\n\0");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Message received: " + text);
                if (listener != null) {
                    listener.onMessage(text);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, okhttp3.ByteString bytes) {
                Log.d(TAG, "Binary message received");
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket closing: " + reason);
                webSocket.close(1000, null);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket closed: " + reason);
                if (listener != null) {
                    listener.onClosed();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e(TAG, "WebSocket failure: ", t);
                if (listener != null) {
                    listener.onError(t.getMessage() != null ? t.getMessage() : "Unknown error");
                }
            }
        });
    }

    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    private void sendStompFrame(String frame) {
        if (webSocket != null) {
            webSocket.send(frame);
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Normal closure");
            webSocket = null;
        }
    }

    private String generateWebSocketKey() {
        // Generyj losowy klucz WebSocket
        byte[] key = new byte[16];
        for (int i = 0; i < 16; i++) {
            key[i] = (byte) (Math.random() * 256);
        }
        return android.util.Base64.encodeToString(key, android.util.Base64.NO_WRAP);
    }
}
