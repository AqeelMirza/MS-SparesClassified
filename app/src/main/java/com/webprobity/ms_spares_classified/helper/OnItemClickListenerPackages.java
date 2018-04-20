package com.webprobity.ms_spares_classified.helper;

import com.webprobity.ms_spares_classified.modelsList.PackagesModel;

public interface OnItemClickListenerPackages {
    void onItemClick(PackagesModel item);
    void onItemTouch();
    void onItemSelected(PackagesModel packagesModel,int spinnerPosition);
}
