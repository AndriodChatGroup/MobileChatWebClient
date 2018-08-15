package com.example.dell_pc.mobilechatwebclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.dell_pc.mobilechatwebclient.client.Message;

import java.util.List;

/**
 * ListView 的适配器，主要就是判断是自己的消息还是别人的消息，在 getView 中分别填充
 * 绑定 view 和数据
 */
public class MessagesListAdapter extends BaseAdapter
{
    private Context context;
    private List<Message> messagesItems;

    public MessagesListAdapter(Context context, List<Message> messagesItems)
    {
        this.context = context;
        this.messagesItems = messagesItems;
    }

    /**
     * 要绑定条目的数目
     *
     * @return
     */
    @Override
    public int getCount()
    {
        return messagesItems.size();
    }

    /**
     * 根据索引获取该数据的对象
     *
     * @param i
     * @return
     */
    @Override
    public Object getItem(int i)
    {
        return messagesItems.get(i);
    }

    /**
     * 获取条目的id
     *
     * @param i
     * @return
     */
    @Override
    public long getItemId(int i)
    {
        return i;
    }

    /**
     * 获取该条目要显示的界面
     *
     * @param i
     * @param view
     * @param viewGroup
     * @return
     */
    @SuppressLint("InflateParams")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        /*
         * The following list not implemented reusable list items as list items are showing incorrect data
         * Add the solution if you have one
         */

        Message m = messagesItems.get(i);

//        LayoutInflater mInflater1 = LayoutInflater.from(context);
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
