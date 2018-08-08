package com.example.dell_pc.mobilechatwebclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.dell_pc.mobilechatwebclient.other.Message;

import java.util.List;

public class MessagesListAdapter extends BaseAdapter
{
    private Context context;
    private List<Message> messagesItems;

    public MessagesListAdapter(Context context, List<Message> messagesItems)
    {
        this.context = context;
        this.messagesItems = messagesItems;
    }

    @Override
    public int getCount()
    {
        return messagesItems.size();
    }

    @Override
    public Object getItem(int i)
    {
        return messagesItems.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        /**
         * The flowing list not implemented reusable list items as list items
         * are showing incorrect data Add the solution if you have one
         */

        Message m = messagesItems.get(i);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        // Identifying the message owner
        if (messagesItems.get(i).isSelf())
        {
            // message belongs to you, so load the right aligned layout
            view = mInflater.inflate(R.layout.list_item_message_right, null);
        }
        else
        {
            // message belongs to other person, load the left aligned layout
            view = mInflater.inflate(R.layout.list_item_message_left, null);
        }

        TextView lblFrom = view.findViewById(R.id.lblMsgFrom);
        TextView txtMsg = view.findViewById(R.id.txtMsg);

        txtMsg.setText(m.getMessage());
        lblFrom.setText(m.getFromName());

        return view;
    }
}
