package com.example.dell_pc.mobilechatwebclient.client;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionUtils
{
    // 数据持久化
    private SharedPreferences sharedPreferences;

    private static final String KEY_SHARED_PREF = "ANDROID_WEB_CHAT";
    private static final int KEY_MODE_PRIVATE = 0;
    private static final String KEY_SESSION_ID = "sessionId";

    public SessionUtils(Context context)
    {
        sharedPreferences = context.getSharedPreferences(KEY_SHARED_PREF, KEY_MODE_PRIVATE);
    }

    public void storeSessionId(String sessionId)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SESSION_ID, sessionId);
        editor.commit();
    }

    public String getSessionId()
    {
        return sharedPreferences.getString(KEY_SESSION_ID, null);
    }
}
