package com.webprobity.ms_spares_classified.adapters;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.modelsList.ChatMessage;
import com.webprobity.ms_spares_classified.utills.SettingsMain;
import com.squareup.picasso.Picasso;

import org.sufficientlysecure.htmltextview.HtmlTextView;

public class ChatAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private ArrayList<ChatMessage> chatMessageList;
    SettingsMain settingsMain;
    Context context;

    public ChatAdapter(Activity activity, ArrayList<ChatMessage> list) {
        chatMessageList = list;
        context = activity;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        settingsMain = new SettingsMain(context);

    }

    @Override
    public int getCount() {
        return chatMessageList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ChatMessage message = chatMessageList.get(position);
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.item_chat_layout, null);

        HtmlTextView msg = vi.findViewById(R.id.message_text);
        ImageView imageView = vi.findViewById(R.id.profile_image);
//        if (message.isMine()) {
            msg.setHtml("<font color=\"#000000\">" + message.getBody() + "<br><br>"
                    + "<small><font color=\"#949494\">" + message.getDate());
//        } else {
//            msg.setHtml("<font color=\"#FFFFFF\">" + message.getBody() + "<br><br>"
//                    + "<small><font color=\"#FFFFFF\">" + message.getDate());
//        }


        LinearLayout parent_layout = vi
                .findViewById(R.id.bubble_layout_parent);

        Picasso.with(context).load(message.getImage())
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(imageView);


        // if message is mine then align to right
        if (settingsMain.getRTL()) {
            if (message.isMine()) {
                msg.setBackgroundResource(R.drawable.ic_received_message);
                parent_layout.setGravity(Gravity.END);
                parent_layout.removeView(imageView);
                parent_layout.addView(imageView, 1);
            }
            // If not mine then align to left
            else {
                msg.setPaddingRelative(45, 5, 20, 5);
                msg.setBackgroundResource(R.drawable.ic_send_message);
                parent_layout.setGravity(Gravity.START);
                parent_layout.removeView(imageView);
                parent_layout.addView(imageView, 0);
            }
        } else {
            if (message.isMine()) {
                msg.setBackgroundResource(R.drawable.ic_send_message);
                parent_layout.setGravity(Gravity.END);
                parent_layout.removeView(imageView);
                parent_layout.addView(imageView, 1);
            }
            // If not mine then align to left
            else {
                msg.setPaddingRelative(45, 5, 20, 5);
                msg.setBackgroundResource(R.drawable.ic_received_message);
                parent_layout.setGravity(Gravity.START);
                parent_layout.removeView(imageView);
                parent_layout.addView(imageView, 0);
            }
        }
        return vi;
    }

    public void add(ChatMessage object) {
        chatMessageList.add(object);
    }
}