package com.webprobity.ms_spares_classified.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.helper.CatSubCatOnclicklinstener;
import com.webprobity.ms_spares_classified.modelsList.catSubCatlistModel;
import com.webprobity.ms_spares_classified.utills.SettingsMain;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ItemCatgorySubListAdapter extends RecyclerView.Adapter<ItemCatgorySubListAdapter.CustomViewHolder> {

    private ArrayList<catSubCatlistModel> list;
    private CatSubCatOnclicklinstener oNItemClickListener;
    private Context mContext;
    SettingsMain settingsMain;
    public ItemCatgorySubListAdapter(Context context, ArrayList<catSubCatlistModel> feedItemList) {
        this.list = feedItemList;
        this.mContext = context;
        settingsMain = new SettingsMain(context);
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_cat_sub_cat, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, final int position) {

        final catSubCatlistModel feedItem = list.get(position);

        holder.titleTextView.setText(list.get(position).getCardName());
        holder.pathTV.setText(list.get(position).getPath());
        holder.priceTV.setText(list.get(position).getPrice());
        holder.locationTV.setText(list.get(position).getLocation());

        if (!TextUtils.isEmpty(feedItem.getImageResourceId())) {
            Picasso.with(mContext).load(feedItem.getImageResourceId())
                    .resize(270,270)
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.mainImage);
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oNItemClickListener.onItemClick(feedItem);
            }
        };
        holder.linearLayoutMain.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return (null != list ? list.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView, pathTV, priceTV, locationTV;
        private ImageView mainImage;
        private LinearLayout linearLayoutMain;

        CustomViewHolder(View v) {
            super(v);

            titleTextView = v.findViewById(R.id.text_view_name);
            pathTV = v.findViewById(R.id.flow);
            priceTV = v.findViewById(R.id.prices);
            locationTV = v.findViewById(R.id.location);
            mainImage = v.findViewById(R.id.image_view);

            priceTV.setTextColor(Color.parseColor(settingsMain.getMainColor()));

            linearLayoutMain = v.findViewById(R.id.linear_layout_card_view);
        }
    }

    public CatSubCatOnclicklinstener getOnItemClickListener() {
        return oNItemClickListener;
    }

    public void setOnItemClickListener(CatSubCatOnclicklinstener onItemClickListener) {
        this.oNItemClickListener = onItemClickListener;
    }
}