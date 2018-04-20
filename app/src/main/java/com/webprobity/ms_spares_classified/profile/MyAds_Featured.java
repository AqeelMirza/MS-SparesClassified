package com.webprobity.ms_spares_classified.profile;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.ad_detail.Ad_detail_activity;
import com.webprobity.ms_spares_classified.adapters.ItemMyAdsAdapter;
import com.webprobity.ms_spares_classified.helper.MyAdsOnclicklinstener;
import com.webprobity.ms_spares_classified.modelsList.myAdsModel;
import com.webprobity.ms_spares_classified.utills.AnalyticsTrackers;
import com.webprobity.ms_spares_classified.utills.CustomBorderDrawable;
import com.webprobity.ms_spares_classified.utills.NestedScroll;
import com.webprobity.ms_spares_classified.utills.Network.RestService;
import com.webprobity.ms_spares_classified.utills.SettingsMain;
import com.webprobity.ms_spares_classified.utills.UrlController;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAds_Featured extends Fragment {

    private ArrayList<myAdsModel> list = new ArrayList<>();

    SettingsMain settingsMain;
    TextView verifyBtn, textViewRateNo, textViewUserName, textViewLastLogin;
    TextView editProfBtn, textViewAdsSold, textViewTotalList, textViewInactiveAds, textViewEmptyData;

    RatingBar ratingBar;
    ImageView imageViewProfile;
    RecyclerView recyclerView;

    ItemMyAdsAdapter adapter;
    int nextPage = 1;
    boolean loading = true, hasNextPage = false;
    ProgressBar progressBar;
    NestedScrollView nestedScrollView;
    RestService restService;

    public MyAds_Featured() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_myadd, container, false);

        settingsMain = new SettingsMain(getActivity());


        progressBar = view.findViewById(R.id.progressBar4);
        nestedScrollView = view.findViewById(R.id.mainScrollView);
        progressBar.setVisibility(View.GONE);

        textViewLastLogin = view.findViewById(R.id.loginTime);
        verifyBtn = view.findViewById(R.id.verified);
        textViewRateNo = view.findViewById(R.id.numberOfRate);
        textViewUserName = view.findViewById(R.id.text_viewName);

        imageViewProfile = view.findViewById(R.id.image_view);
        ratingBar = view.findViewById(R.id.ratingBar);

        LayerDrawable stars = (LayerDrawable) this.ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#ffcc00"), PorterDuff.Mode.SRC_ATOP);

        editProfBtn = view.findViewById(R.id.editProfile);
        textViewEmptyData = view.findViewById(R.id.textView5);
        textViewEmptyData.setVisibility(View.GONE);
        textViewAdsSold = view.findViewById(R.id.share);
        textViewTotalList = view.findViewById(R.id.addfav);
        textViewInactiveAds = view.findViewById(R.id.report);

        editProfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new EditProfile(), "EditProfile");
            }
        });

        recyclerView = view.findViewById(R.id.cardView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        GridLayoutManager MyLayoutManager = new GridLayoutManager(getActivity(), 2);
        MyLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(MyLayoutManager);
        restService = UrlController.createService(RestService.class,settingsMain.getUserEmail(),settingsMain.getUserPassword(),getActivity());

        nestedScrollView.setOnScrollChangeListener(new NestedScroll() {
            @Override
            public void onScroll() {

                if (loading) {
                    loading = false;
                    Log.d("info data object", "sdfasdfadsasdfasdfasdf");

                    if (hasNextPage) {
                        progressBar.setVisibility(View.VISIBLE);
                        adforest_loadMore(nextPage);
                    }
                }
            }
        });

        ratingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    RatingFragment fragment = new RatingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("id", settingsMain.getUserLogin());
                    bundle.putBoolean("isprofile", true);
                    fragment.setArguments(bundle);

                    replaceFragment(fragment, "RatingFragment");
                }
                return true;
            }
        });
        loadData();

        return view;
    }

    private void setAllViewsText(JSONObject jsonObject) {
        try {
            textViewLastLogin.setText(jsonObject.getString("last_login"));
            textViewUserName.setText(jsonObject.getString("display_name"));

            Picasso.with(getActivity()).load(jsonObject.getString("profile_img"))
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(imageViewProfile);

            verifyBtn.setText(jsonObject.getJSONObject("verify_buton").getString("text"));
            verifyBtn.setBackground(CustomBorderDrawable.customButton(0, 0, 0, 0,
                    jsonObject.getJSONObject("verify_buton").getString("color"),
                    jsonObject.getJSONObject("verify_buton").getString("color"),
                    jsonObject.getJSONObject("verify_buton").getString("color"), 3));

            textViewAdsSold.setText(jsonObject.getString("ads_sold"));
            textViewTotalList.setText(jsonObject.getString("ads_total"));
            textViewInactiveAds.setText(jsonObject.getString("ads_inactive"));

            ratingBar.setNumStars(5);
            ratingBar.setRating(Float.parseFloat(jsonObject.getJSONObject("rate_bar").getString("number")));
            textViewRateNo.setText(jsonObject.getJSONObject("rate_bar").getString("text"));

            editProfBtn.setText(jsonObject.getString("edit_text"));
            SettingsMain.hideDilog();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void replaceFragment(Fragment someFragment, String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
        transaction.replace(R.id.frameContainer, someFragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    private void adforest_loadMore(int nextPag) {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            JsonObject params = new JsonObject();

            params.addProperty("page_number", nextPag);

            Log.d("info sendFeatured Load", params.toString());

            Call<ResponseBody> myCall = restService.postGetLoadMoreFeaturedAds(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info FeaturedMore Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info FeaturedMore obj", "" + response.getJSONObject("data"));

                                JSONObject jsonObjectPagination = response.getJSONObject("data").getJSONObject("pagination");

                                nextPage = jsonObjectPagination.getInt("next_page");
                                hasNextPage = jsonObjectPagination.getBoolean("has_next_page");

                                loadMoreList(response.getJSONObject("data"), response.getJSONObject("data").getJSONObject("text"));

                                loading = true;
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        SettingsMain.hideDilog();
                    } catch (JSONException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    } catch (IOException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    }
                    SettingsMain.hideDilog();
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info FeaturedMore ", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info FeaturedMore err", String.valueOf(t));
                        Log.d("info FeaturedMore err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadData() {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());
            Call<ResponseBody> myCall = restService.getFeaturedAdsDetails(UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info FeaturedAds Respon", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info FeaturedAds object", "" + response.getJSONObject("data"));
                                Log.d("info FeaturedAd Profile", "" + response.getJSONObject("data").getJSONObject("profile"));
                                getActivity().setTitle(response.getJSONObject("data").getString("page_title"));

                                JSONObject jsonObjectPagination = response.getJSONObject("data").getJSONObject("pagination");

                                nextPage = jsonObjectPagination.getInt("next_page");
                                hasNextPage = jsonObjectPagination.getBoolean("has_next_page");

                                makeList(response.getJSONObject("data"), response.getJSONObject("data").getJSONObject("text"));
                                setAllViewsText(response.getJSONObject("data").getJSONObject("profile"));

                                if (list.size() > 0) {
                                    adapter = new ItemMyAdsAdapter(getActivity(), list);
                                    recyclerView.setAdapter(adapter);
                                    adapter.setOnItemClickListener(new MyAdsOnclicklinstener() {
                                        @Override
                                        public void onItemClick(myAdsModel item) {


                                            Intent intent = new Intent(getActivity(), Ad_detail_activity.class);
                                            intent.putExtra("adId", item.getAdId());
                                            getActivity().startActivity(intent);
                                            getActivity().overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                                        }

                                        @Override
                                        public void delViewOnClick(View v, int position) {
                                            Toast.makeText(getContext(), "ss" + "" + position, Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void editViewOnClick(View v, int position) {
                                            Toast.makeText(getContext(), "s" + "" + position, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    textViewEmptyData.setVisibility(View.VISIBLE);
                                    textViewEmptyData.setText(response.get("message").toString());
                                }
                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        SettingsMain.hideDilog();

                    } catch (JSONException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    } catch (IOException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    SettingsMain.hideDilog();
                    Log.d("info FeaturedAds error", String.valueOf(t));
                    Log.d("info FeaturedAds error", String.valueOf(t.getMessage()+t.getCause()+t.fillInStackTrace()));
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }

    }

    void loadMoreList(JSONObject data, JSONObject texts) {
        try {
            JSONArray jsonArray = data.getJSONArray("ads");

            Log.d("jsonaarry is = ", jsonArray.toString());
            if (jsonArray.length() > 0)
                for (int i = 0; i < jsonArray.length(); i++) {

                    myAdsModel item = new myAdsModel();
                    JSONObject object = jsonArray.getJSONObject(i);

                    item.setAdId(object.getString("ad_id"));
                    item.setName(object.getString("ad_title"));
                    item.setAdStatus(object.getJSONObject("ad_status").getString("status"));
                    item.setAdStatusValue(object.getJSONObject("ad_status").getString("status_text"));
                    item.setAdTypeText(object.getJSONObject("ad_status").getString("featured_type_text"));
                    item.setPrice(object.getJSONObject("ad_price").getString("price"));
                    item.setImage((object.getJSONArray("ad_images").getJSONObject(0).getString("thumb")));

                    item.setDelAd(texts.getString("delete_text"));
                    item.setEditAd(texts.getString("edit_text"));
                    item.setAdType(texts.getString("ad_type"));

                    item.setSpinerData(texts.getJSONArray("status_dropdown_name"));
                    item.setSpinerValue(texts.getJSONArray("status_dropdown_value"));

                    list.add(item);
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void makeList(JSONObject data, JSONObject texts) {
        list.clear();

        try {
            JSONArray jsonArray = data.getJSONArray("ads");

//            Log.d("jsonaarry is = ", jsonArray.toString());
            if (jsonArray.length() > 0)
                for (int i = 0; i < jsonArray.length(); i++) {

                    myAdsModel item = new myAdsModel();
                    JSONObject object = jsonArray.getJSONObject(i);

                    item.setAdId(object.getString("ad_id"));
                    item.setName(object.getString("ad_title"));
                    item.setAdStatus(object.getJSONObject("ad_status").getString("status"));
                    item.setAdStatusValue(object.getJSONObject("ad_status").getString("status_text"));
                    item.setAdTypeText(object.getJSONObject("ad_status").getString("featured_type_text"));
                    item.setPrice(object.getJSONObject("ad_price").getString("price"));
                    item.setImage((object.getJSONArray("ad_images").getJSONObject(0).getString("thumb")));

                    item.setDelAd(texts.getString("delete_text"));
                    item.setEditAd(texts.getString("edit_text"));
                    item.setAdType(texts.getString("ad_type"));

                    item.setSpinerData(texts.getJSONArray("status_dropdown_name"));
                    item.setSpinerValue(texts.getJSONArray("status_dropdown_value"));

                    list.add(item);
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onResume() {
        if (settingsMain.getAnalyticsShow() && !settingsMain.getAnalyticsId().equals(""))
            AnalyticsTrackers.getInstance().trackScreenView("Featured Ads");
        super.onResume();
    }
}
