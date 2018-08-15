package com.example.dell_pc.mobilechatwebclient.client;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageUtils
{
    private SessionUtils sessionUtils;

    public MessageUtils(SessionUtils sessionUtils)
    {
        this.sessionUtils = sessionUtils;
    }

    public String getSendMessageJSON(String message)
    {
        String json = null;

        try
        {
            JSONObject jObj = new JSONObject();
            jObj.put("flag", MessageType.FLAG_MESSAGE);
            jObj.put("sessionId", sessionUtils.getSessionId());
            jObj.put("message", message);

            json = jObj.toString();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return json;
    }
}
