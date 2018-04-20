package com.webprobity.ms_spares_classified.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.webprobity.ms_spares_classified.helper.SendReciveONClickListner;

import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.modelsList.messageSentRecivModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ItemSendRecMesageAdapter extends RecyclerView.Adapter<ItemSendRecMesageAdapter.CustomViewHolder> {
    private List<messageSentRecivModel> feedItemList;
    private Context mContext;
    private SendReciveONClickListner oNItemClickListener;


    public ItemSendRecMesageAdapter(Context context, List<messageSentRecivModel> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;

    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sent_message, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final messageSentRecivModel feedItem = feedItemList.get(i);

        customViewHolder.name.setText(feedItemList.get(i).getName());
        customViewHolder.active.setText(feedItemList.get(i).getActive());
        customViewHolder.topic.setText(feedItemList.get(i).getTopic());

        if (!TextUtils.isEmpty(feedItem.getTumbnail())) {
            Picasso.with(mContext).load(feedItem.getTumbnail())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(customViewHolder.imageView);
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oNItemClickListener.onItemClick(feedItem);
            }
        };

        customViewHolder.linearLayout.setOnClickListener(listener);

    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name, active, topic;
        LinearLayout linearLayout;

        CustomViewHolder(View view) {
            super(view);

            this.linearLayout = view.findViewById(R.id.linear_layout_card_view);
            this.imageView = view.findViewById(R.id.image_view);
            this.name = view.findViewById(R.id.text_viewName);
            this.active = view.findViewById(R.id.verified);
            this.topic = view.findViewById(R.id.loginTime);
        }
    }

    public SendReciveONClickListner getOnItemClickListener() {
        return oNItemClickListener;
    }

    public void setOnItemClickListener(SendReciveONClickListner onItemClickListener) {
        this.oNItemClickListener = onItemClickListener;
    }
}