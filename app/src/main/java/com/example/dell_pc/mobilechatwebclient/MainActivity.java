package com.example.dell_pc.mobilechatwebclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.codebutler.android_websockets.WebSocketClient;
import com.example.dell_pc.mobilechatwebclient.other.Message;
import com.example.dell_pc.mobilechatwebclient.other.Utils;
import com.example.dell_pc.mobilechatwebclient.other.WsConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity
{
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private Button btnSend;
    private EditText inputMsg;

    private WebSocketClient client;

    // Chat messages list adapter
    private MessagesListAdapter adapter;
    private List<Message> listMessages;
    private ListView listViewMessages;

    private Utils utils;

    // Client name
    private String name = null;

    // location service
    private LocationManager locationManager;
    private EditText locationEditText;

    // JSON flags to identify the kind of JSON response
    private static final String
            TAG_SELF = "self",
            TAG_NEW = "new",
            TAG_MESSAGE = "message",
            TAG_EXIT = "exit";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSend = findViewById(R.id.btnSend);
        inputMsg = findViewById(R.id.inputMsg);
        listViewMessages = findViewById(R.id.list_view_messages);

        utils = new Utils(getApplicationContext());

        // 从上一个屏幕获取姓名
        Intent i = getIntent();
        name = i.getStringExtra("name");

        btnSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Sending message to web socket server
                sendMessageToServer(utils.getSendMessageJSON(inputMsg.getText().toString()));

                // Clearing the input filed once message was sent
                inputMsg.setText("");
            }
        });

        listMessages = new ArrayList<>();

        adapter = new MessagesListAdapter(this, listMessages);
        listViewMessages.setAdapter(adapter);

        // location service
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationEditText = findViewById(R.id.location);

        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)))
        {
            Toast.makeText(this, "请打开网络或GPS定位功能！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }

        try
        {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null)
            {
                Log.d(TAG, "onCreate.location = null");
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            Log.d(TAG, "onCreate.location = " + location);
            updateView(location);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 5, locationListener);
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }

        /*
         * 创建web socket客户端，有如下的回调函数
         */
        client = new WebSocketClient(URI.create(WsConfig.URL_WEBSOCKET + URLEncoder.encode(name)), new WebSocketClient.Listener()
        {
            @Override
            public void onConnect()
            {

            }

            @Override
            public void onMessage(String message)
            {
                Log.d(TAG, String.format("Got string message! %s", message));

                parseMessage(message);
            }

            @Override
            public void onMessage(byte[] data)
            {
                Log.d(TAG, String.format("Got binary message! %s", bytesToHex(data)));

                // Message will be in JSON format
                parseMessage(bytesToHex(data));
            }

            @Override
            public void onDisconnect(int code, String reason)
            {
                String message = String.format(Locale.US, "Disconnected! Code: %d Reason: %s", code, reason);

                showToast(message);

                // clear the session id from shared preferences
                utils.storeSessionId(null);
            }

            @Override
            public void onError(Exception error)
            {
                Log.e(TAG, "Error! : " + error);

                showToast("Error! : " + error);
            }
        }, null);

        client.connect();
    }

    /**
     * Send messages
     *
     * @param message
     */
    private void sendMessageToServer(String message)
    {
        if (client != null && client.isConnected())
            client.send(message);
    }

    /**
     * 解析从服务端收到的json消息的目的由flag字段所指定，flag：
     * self：消息属于指定的人，
     * new：新人加入到对话中，
     * message：新的消息，
     * exit退出
     *
     * @param msg
     */
    private void parseMessage(final String msg)
    {
        try
        {
            JSONObject jObj = new JSONObject(msg);

            // JSON node 'flag'
            String flag = jObj.getString("flag");

            if (flag.equalsIgnoreCase(TAG_SELF))
            {
                // 如果是self，json中包含sessionId信息
                String sessionId = jObj.getString("sessionId");

                // Save the session id in shared preferences
                utils.storeSessionId(sessionId);

                Log.e(TAG, "Your session id: " + utils.getSessionId());
            }
            else if (flag.equalsIgnoreCase(TAG_NEW))
            {
                // If the flag is 'new', new person joined the room
                String name = jObj.getString("name");
                String message = jObj.getString("message");

                // number of people online
                String onlineCount = jObj.getString("onlineCount");

                showToast(name + message + ". Currently " + onlineCount + " people online!");
            }
            else if (flag.equalsIgnoreCase(TAG_MESSAGE))
            {
                // if the flag is 'message', new message received
                String fromName = name;
                String message = jObj.getString("message");
                String sessionId = jObj.getString("sessionId");
                boolean isSelf = true;

                // Checking if the message was sent by you
                if (!sessionId.equals(utils.getSessionId()))
                {
                    fromName = jObj.getString("name");
                    isSelf = false;
                }

                Message m = new Message(fromName, message, isSelf);

                // 把消息加入到arraylist中
                appendMessage(m);
            }
            else if (flag.equalsIgnoreCase(TAG_EXIT))
            {
                // If the flag is 'exit', somebody left the conversation
                String name = jObj.getString("name");
                String message = jObj.getString("message");

                showToast(name + message);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy()
    {
        try
        {
            locationManager.removeUpdates(locationListener);
        }
        catch (SecurityException e)
        {

        }

        super.onDestroy();

        if (client != null && client.isConnected())
        {
            showToast("disconnect");
            client.disconnect();
        }
    }

    private LocationListener locationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location)
        {
            Log.d(TAG, "onProviderDisabled.location = " + location);
            updateView(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle)
        {
            Log.d(TAG, "onStatusChanged() called with " + "provider = [" + s + "], status = [" + i + "], extras = [" + bundle + "]");
            switch (i)
            {
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG, "TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String s)
        {
            Log.d(TAG, "onProviderEnable() called with " + "provider = [" + s + "]");
            try
            {
                Location location = locationManager.getLastKnownLocation(s);
                Log.d(TAG, "onProviderDisable.location = " + location);
                updateView(location);
            }
            catch (SecurityException e)
            {

            }
        }

        @Override
        public void onProviderDisabled(String s)
        {
            Log.d(TAG, "onProviderDisabled() called with " + "provider = [" + s + "]");
        }
    };

    /**
     * 把消息放到listView里
     * @param m
     */
    private void appendMessage(final Message m)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                listMessages.add(m);

                adapter.notifyDataSetChanged();

                // Playing device's notification
                playBeep();
            }
        });
    }

    /**
     * 播放默认的通知声音
     */
    public void playBeep()
    {
        try
        {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void showToast(final String message)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++)
        {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private void updateView(Location location)
    {
        Geocoder gc = new Geocoder(this);
        List<Address> addresses = null;
        String msg = "";
        Log.d(TAG, "updateView.location = " + location);
        if (location != null)
        {
            try
            {
                addresses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                Log.d(TAG, "updateView.address = " + addresses);
                if (addresses.size() > 0)
                {
                    msg += addresses.get(0).getAdminArea().substring(0, 2);
                    msg += " " + addresses.get(0).getLocality().substring(0, 2);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            locationEditText.setText("定位到位置：\n");
            locationEditText.append(msg);
            locationEditText.append("\n经度：");
            locationEditText.append(String.valueOf(location.getLongitude()));
            locationEditText.append("\n纬度：");
            locationEditText.append(String.valueOf(location.getLatitude()));
        }
        else
        {
            locationEditText.getEditableText().clear();
            locationEditText.setText("定位中");
        }
    }
}
