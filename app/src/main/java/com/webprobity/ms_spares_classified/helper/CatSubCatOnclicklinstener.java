package com.webprobity.ms_spares_classified.helper;

import android.view.View;

import com.webprobity.ms_spares_classified.modelsList.catSubCatlistModel;

public interface CatSubCatOnclicklinstener {
    void onItemClick(catSubCatlistModel item);
    void addToFavClick(View v, String position);

}
