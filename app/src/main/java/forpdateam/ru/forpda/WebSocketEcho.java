package forpdateam.ru.forpda;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import forpdateam.ru.forpda.client.Client;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by radiationx on 12.06.17.
 */

public final class WebSocketEcho extends WebSocketListener {
    private void run() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url("ws://app.4pda.ru/ws/")
                .build();
        client.newWebSocket(request, this);

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();
    }


    public static void main(String... args) {
        //new WebSocketEcho().run();
        Client.getInstance().createWebSocketConnection(new WebSocketListener() {
            int i = 0;
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("WS_SUKA", "ON OPEN: " + response.toString());
                Log.d("WS_SUKA", "ON OPEN HEADERS: " + response.headers().toString());
                webSocket.send("[0,\"sv\"]");
                webSocket.send("[0, \"ea\", \"u2556269\"]");
                /*webSocket.send("Hello...");
                webSocket.send("...World!");
                webSocket.send(ByteString.decodeHex("deadbeef"));
                webSocket.close(1000, "Goodbye, World!");*/
            }


            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WS_SUKA", "ON T MESSAGE: " + text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.d("WS_SUKA", "ON B MESSAGE: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d("WS_SUKA", "ON CLOSING: " + code + " " + reason);
                webSocket.close(1000, null);

            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("WS_SUKA", "ON CLOSED: " + code + " " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.d("WS_SUKA", "ON FAILURE: " + t.getMessage() + " " + response.toString());
                t.printStackTrace();
            }
        });
    }
}
