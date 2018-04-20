package com.webprobity.ms_spares_classified.helper;

import android.view.View;

import com.webprobity.ms_spares_classified.modelsList.myAdsModel;

public interface MyAdsOnclicklinstener {

    void onItemClick(myAdsModel item);
    void delViewOnClick(View v, int position);
    void editViewOnClick(View v, int position);

}
