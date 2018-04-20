package com.webprobity.ms_spares_classified.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.helper.ItemTouchHelperAdapter;
import com.webprobity.ms_spares_classified.helper.ItemTouchHelperViewHolder;
import com.webprobity.ms_spares_classified.helper.MyAdsOnclicklinstener;
import com.webprobity.ms_spares_classified.helper.OnStartDragListener;
import com.webprobity.ms_spares_classified.modelsList.myAdsModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

public class ItemEditImageAdapter extends RecyclerView.Adapter<ItemEditImageAdapter.MyViewHolder>
        implements ItemTouchHelperAdapter {
    private ArrayList<myAdsModel> list;
    private MyAdsOnclicklinstener onItemClickListener;
    private Context mContext;
    private final OnStartDragListener mDragStartListener;

    public ItemEditImageAdapter(Context context, ArrayList<myAdsModel> Data, OnStartDragListener dragStartListener) {
        this.list = Data;
        mDragStartListener = dragStartListener;
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemof_edit_image, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        final myAdsModel feedItem = list.get(position);

        if (!TextUtils.isEmpty(feedItem.getImage())) {
            Picasso.with(mContext).load(feedItem.getImage())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.mainImage);
        }
        holder.delAd.setTag(list.get(position).getAdId());

        View.OnClickListener listener2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.delViewOnClick(v, position);
            }
        };

        holder.relativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });

        holder.delAd.setOnClickListener(listener2);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public String getAllTags() {
        String s = "";
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                final myAdsModel feedItem = list.get(i);

                s = s.concat(feedItem.getAdId() + ",");
            }
        }
        return s;
    }

    public void setOnItemClickListener(MyAdsOnclicklinstener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(list, fromPosition, toPosition);
        this.notifyDataSetChanged();
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    class MyViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        ImageView mainImage, delAd;
        RelativeLayout relativeLayout;

        MyViewHolder(View v) {
            super(v);

            relativeLayout = v.findViewById(R.id.linear_layout_card_view);
            delAd = v.findViewById(R.id.delAdd);
            mainImage = v.findViewById(R.id.image_view);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}
