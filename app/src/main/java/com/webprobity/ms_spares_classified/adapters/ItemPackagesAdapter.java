package com.webprobity.ms_spares_classified.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.helper.OnItemClickListenerPackages;
import com.webprobity.ms_spares_classified.home.StripePayment;
import com.webprobity.ms_spares_classified.modelsList.PackagesModel;
import com.webprobity.ms_spares_classified.utills.SettingsMain;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class ItemPackagesAdapter extends RecyclerView.Adapter<ItemPackagesAdapter.CustomViewHolder> {

    private List<PackagesModel> feedItemList;
    private OnItemClickListenerPackages onItemClickListener;
    SettingsMain settingsMain;
    private Context mContext;

    public ItemPackagesAdapter(Context context1, List<PackagesModel> feedItemList) {
        this.feedItemList = feedItemList;
        settingsMain = new SettingsMain(context1);
        this.mContext=context1;
    }

    @Override
    public ItemPackagesAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_packages, null);
        return new ItemPackagesAdapter.CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemPackagesAdapter.CustomViewHolder customViewHolder, int i) {
        final PackagesModel feedItem = feedItemList.get(i);

        if (settingsMain.getAppOpen())
        {
            customViewHolder.spinner.setVisibility(View.GONE);
        }
        customViewHolder.name.setText(feedItem.getPlanType());
        customViewHolder.price.setText(feedItem.getPrice());
        customViewHolder.validaty.setText(feedItem.getValidaty());
        customViewHolder.ads.setText(feedItem.getFreeAds());
        customViewHolder.featureads.setText(feedItem.getFeatureAds());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, feedItem.getSpinnerData());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        customViewHolder.spinner.setAdapter(adapter);
        customViewHolder.spinner.setTag(feedItem.getBtnTag());

        customViewHolder.btn.setText(feedItem.getBtnText());
        customViewHolder.btn.setTag(feedItem.getBtnTag());

        customViewHolder.btn.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(feedItem);
            }
        };
        View.OnTouchListener listener1 = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onItemClickListener.onItemTouch();
                return false;
            }
        };
        customViewHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onItemClickListener.onItemSelected(feedItem,position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        customViewHolder.spinner.setOnTouchListener(listener1);
        customViewHolder.btn.setOnClickListener(listener);


    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView name, validaty, ads, price, featureads, btn;
        Spinner spinner;
        RelativeLayout selectPackageLayout;

        boolean spinnerTouched=false;
        CustomViewHolder(View view) {
            super(view);

            this.name = view.findViewById(R.id.textView22);
            this.price = view.findViewById(R.id.textView26);
            this.validaty = view.findViewById(R.id.textView23);
            this.ads = view.findViewById(R.id.textView24);
            this.featureads = view.findViewById(R.id.textView25);
            this.btn = view.findViewById(R.id.btnSelect);
            spinner=view.findViewById(R.id.selectPlan);
            selectPackageLayout=view.findViewById(R.id.selectPackageLayout);

            price.setTextColor(Color.parseColor(settingsMain.getMainColor()));
        }
    }

    public void setOnItemClickListener(OnItemClickListenerPackages onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

}
