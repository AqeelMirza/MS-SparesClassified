package com.webprobity.ms_spares_classified.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.ad_detail.Ad_detail_activity;
import com.webprobity.ms_spares_classified.adapters.ItemCatgorySubListAdapter;
import com.webprobity.ms_spares_classified.adapters.ItemSearchFeatureAdsAdapter;
import com.webprobity.ms_spares_classified.helper.CatSubCatOnclicklinstener;
import com.webprobity.ms_spares_classified.modelsList.catSubCatlistModel;
import com.webprobity.ms_spares_classified.utills.AnalyticsTrackers;
import com.webprobity.ms_spares_classified.utills.Network.RestService;
import com.webprobity.ms_spares_classified.utills.SettingsMain;
import com.webprobity.ms_spares_classified.utills.UrlController;

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

public class FragmentCatSubNSearch extends Fragment {

    ArrayList<catSubCatlistModel> searchedAdList = new ArrayList<>();
    ArrayList<catSubCatlistModel> featureAdsList = new ArrayList<>();
    RecyclerView MyRecyclerView, recyclerViewFeatured;

    //EditText editTextSearch;
    SettingsMain settingsMain;
    TextView textViewTitleFeature, textViewFilterText;

    ItemCatgorySubListAdapter itemCatgorySubListAdapter;
    ItemSearchFeatureAdsAdapter itemSearchFeatureAdsAdapter;
    LinearLayout viewProductLayout;
    JSONObject jsonObjectPagination;
    JsonObject lastSentParamas;
    LinearLayout linearLayoutCollapse;
    Spinner spinnerFilter;
    LinearLayout linearLayoutFilter;

    boolean isSort = false;
    RelativeLayout relativeLayoutSpiner;
    NestedScrollView scrollView;

    ProgressBar progressBar;

    int currentPage = 1, nextPage = 1, totalPage = 0;

    String myId, title, ad_country, nearby_latitude, nearby_longitude, nearby_distance;
    RestService restService;
    GridLayoutManager MyLayoutManager2;
    private JSONObject jsonObjectFilterSpinner;
    private boolean spinnerTouched2 = false;

    public FragmentCatSubNSearch() {
        // Required empty public constructor
    }

    public static void adforest_recylerview_autoScroll(final int duration, final int pixelsToMove, final int delayMillis,
                                                       final RecyclerView recyclerView, final GridLayoutManager gridLayoutManager,
                                                       final ItemSearchFeatureAdsAdapter itemFeatureAdsAdapter) {
        final Handler mHandler = new Handler(Looper.getMainLooper());
        final Runnable SCROLLING_RUNNABLE = new Runnable() {

            @Override
            public void run() {
                recyclerView.smoothScrollBy(pixelsToMove, 0);
                mHandler.postDelayed(this, duration);
            }
        };
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastItem = gridLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastItem == gridLayoutManager.getItemCount() - 1) {
                    mHandler.removeCallbacks(SCROLLING_RUNNABLE);
                    Handler postHandler = new Handler();
                    postHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.setAdapter(null);
                            recyclerView.setAdapter(itemFeatureAdsAdapter);
                            mHandler.postDelayed(SCROLLING_RUNNABLE, delayMillis);
                        }
                    }, delayMillis);
                }
            }
        });
        mHandler.postDelayed(SCROLLING_RUNNABLE, 2000);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cat_subcatlist, container, false);
        scrollView = view.findViewById(R.id.scrollView);
        progressBar = view.findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            myId = bundle.getString("id", "");
            title = bundle.getString("title", "");
            ad_country = bundle.getString("ad_country", "");
            nearby_latitude = bundle.getString("nearby_latitude", "");
            nearby_longitude = bundle.getString("nearby_longitude", "");
            nearby_distance = bundle.getString("nearby_distance", "");
        }
        settingsMain = new SettingsMain(getActivity());

        linearLayoutCollapse = view.findViewById(R.id.linearLayout);
        linearLayoutFilter = view.findViewById(R.id.filter_layout);
        textViewFilterText = view.findViewById(R.id.textViewFilter);
        spinnerFilter = view.findViewById(R.id.spinner);
        relativeLayoutSpiner = view.findViewById(R.id.rel1);

        viewProductLayout = view.findViewById(R.id.customOptionLL);

        textViewTitleFeature = view.findViewById(R.id.textView6);

        MyRecyclerView = view.findViewById(R.id.recycler_view);
        MyRecyclerView.setHasFixedSize(true);
        MyRecyclerView.setNestedScrollingEnabled(false);

        LinearLayoutManager MyLayoutManager = new LinearLayoutManager(getActivity());
        MyLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        MyRecyclerView.setLayoutManager(MyLayoutManager);

        recyclerViewFeatured = view.findViewById(R.id.recycler_view2);
        recyclerViewFeatured.setHasFixedSize(true);
        MyLayoutManager2 = new GridLayoutManager(getActivity(), 1);
        MyLayoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        if (settingsMain.getAppOpen()) {
            restService = UrlController.createService(RestService.class);
        } else
            restService = UrlController.createService(RestService.class, settingsMain.getUserEmail(), settingsMain.getUserPassword(), getActivity());

        recyclerViewFeatured.setLayoutManager(MyLayoutManager2);

        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                        scrollY > oldScrollY) {

                    if (nextPage <= totalPage) {
                        progressBar.setVisibility(View.VISIBLE);
                        adforest_loadmore();

                        Log.d("heeeeeeelo", nextPage + "==" + totalPage);

                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });

        adforest_submitQuery("");

        ((HomeActivity) getActivity()).updateApi(new HomeActivity.UpdateFragment() {
            @Override
            public void updatefrag(String s) {
                title = s;
                adforest_submitQuery("");
            }

            @Override
            public void updatefrag(String latitude, String longitude, String distance) {
                nearby_latitude=latitude;
                nearby_longitude=longitude;
                nearby_distance=distance;
                adforest_submitQuery("");
            }
        });
        return view;
    }

    private void adforest_submitQuery(String s) {

        JsonObject params = new JsonObject();
        if (!myId.equals("")) {
            params.addProperty(settingsMain.getAlertDialogMessage("catId"), myId);
        }
        if (!title.equals("")) {
            params.addProperty("ad_title", title);
        }
        if (isSort) {
            params.addProperty("sort", s);
        }
        if (!ad_country.equals("")) {
            params.addProperty("ad_country", ad_country);
        }
        if (!nearby_latitude.equals("") && !nearby_longitude.equals("")) {
            params.addProperty("nearby_latitude", nearby_latitude);
            params.addProperty("nearby_longitude", nearby_longitude);
            params.addProperty("nearby_distance", nearby_distance);
        }
        lastSentParamas = params;

        params.addProperty("page_number", 1);


        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());
            Log.d("info Send MenuSearch =", "" + params.toString());

            Call<ResponseBody> myCall = restService.postGetMenuSearchData(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info MenuSearch Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info MenuSearch object", "" + response.getJSONObject("data"));

                                getActivity().setTitle(response.getJSONObject("extra").getString("title"));

                                if (response.getJSONObject("extra").getBoolean("is_show_featured")) {
                                    textViewTitleFeature.setVisibility(View.VISIBLE);
                                } else {
                                    textViewTitleFeature.setVisibility(View.GONE);
                                }

                                try {
                                    jsonObjectFilterSpinner = response.getJSONObject("topbar");


                                    final JSONArray dropDownJSONOpt = jsonObjectFilterSpinner.getJSONArray("sort_arr");
                                    final ArrayList<String> SpinnerOptions;
                                    SpinnerOptions = new ArrayList<>();
                                    for (int j = 0; j < dropDownJSONOpt.length(); j++) {
                                        SpinnerOptions.add(dropDownJSONOpt.getJSONObject(j).getString("value"));
                                    }

                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item_medium, SpinnerOptions);
                                    spinnerFilter.setAdapter(adapter);

                                    spinnerFilter.setOnTouchListener(new View.OnTouchListener() {
                                        @Override
                                        public boolean onTouch(View v, MotionEvent event) {
                                            System.out.println("Real touch felt.");
                                            spinnerTouched2 = true;
                                            return false;
                                        }
                                    });

                                    spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                                            if (spinnerTouched2) {
                                                try {
                                                    //Toast.makeText(getActivity(), "" + dropDownJSONOpt.getJSONObject(i).getString("key"), Toast.LENGTH_SHORT).show();
                                                    isSort = true;
                                                    adforest_submitQuery(dropDownJSONOpt.getJSONObject(i).getString("key"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            spinnerTouched2 = false;
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> adapterView) {

                                        }
                                    });

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                                jsonObjectPagination = response.getJSONObject("pagination");

                                nextPage = jsonObjectPagination.getInt("next_page");
                                currentPage = jsonObjectPagination.getInt("current_page");
                                totalPage = jsonObjectPagination.getInt("max_num_pages");

                                adforest_loadList(response.getJSONObject("data").getJSONObject("featured_ads"),
                                        response.getJSONObject("data").getJSONArray("ads"),
                                        response.getJSONObject("topbar"));

                                itemCatgorySubListAdapter = new ItemCatgorySubListAdapter(getActivity(), searchedAdList);
                                itemSearchFeatureAdsAdapter = new ItemSearchFeatureAdsAdapter(getActivity(), featureAdsList);

                                recyclerViewFeatured.setAdapter(itemSearchFeatureAdsAdapter);

                                if (settingsMain.isFeaturedScrollEnable()) {
                                    adforest_recylerview_autoScroll(settingsMain.getFeaturedScroolDuration(),
                                            40, settingsMain.getFeaturedScroolLoop(),
                                            recyclerViewFeatured, MyLayoutManager2, itemSearchFeatureAdsAdapter);
                                }
                                MyRecyclerView.setAdapter(itemCatgorySubListAdapter);

                                itemSearchFeatureAdsAdapter.setOnItemClickListener(new CatSubCatOnclicklinstener() {
                                    @Override
                                    public void onItemClick(catSubCatlistModel item) {
                                        Intent intent = new Intent(getActivity(), Ad_detail_activity.class);
                                        intent.putExtra("adId", item.getId());
                                        getActivity().startActivity(intent);
                                        getActivity().overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                                    }

                                    @Override
                                    public void addToFavClick(View v, String position) {
                                    }
                                });

                                itemCatgorySubListAdapter.setOnItemClickListener(new CatSubCatOnclicklinstener() {
                                    @Override
                                    public void onItemClick(catSubCatlistModel item) {

                                        Intent intent = new Intent(getActivity(), Ad_detail_activity.class);
                                        intent.putExtra("adId", item.getId());
                                        getActivity().startActivity(intent);
                                        getActivity().overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                                    }

                                    @Override
                                    public void addToFavClick(View v, String position) {

                                    }
                                });
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
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info MenuSearch ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info MenuSearch err", String.valueOf(t));
                        Log.d("info MenuSearch err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
        isSort = false;
    }

    private void adforest_loadmore() {

        lastSentParamas.addProperty("page_number", nextPage);
        Log.d("info loadMore MenuSrch=", "" + lastSentParamas.toString());

        if (SettingsMain.isConnectingToInternet(getActivity())) {
            Call<ResponseBody> myCall = restService.postGetMenuSearchData(lastSentParamas, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info MenuSearch Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info MenuSearch object", "" + response.getJSONObject("data"));

                                Log.d("info extra_obj", "" + response.getJSONObject("extra"));

                                jsonObjectPagination = response.getJSONObject("pagination");

                                nextPage = jsonObjectPagination.getInt("next_page");
                                currentPage = jsonObjectPagination.getInt("current_page");
                                totalPage = jsonObjectPagination.getInt("max_num_pages");

                                JSONArray searchAds = response.getJSONObject("data").getJSONArray("ads");

                                try {
                                    Log.d("info MenuSearch is = ", searchAds.toString());
                                    if (searchAds.length() > 0) {
                                        for (int i = 0; i < searchAds.length(); i++) {

                                            catSubCatlistModel item = new catSubCatlistModel();

                                            JSONObject object = searchAds.getJSONObject(i);

                                            item.setId(object.getString("ad_id"));
                                            item.setCardName(object.getString("ad_title"));
                                            item.setDate(object.getString("ad_date"));
                                            item.setAdViews(object.getString("ad_views"));
                                            item.setPath(object.getString("ad_cats_name"));
                                            item.setPrice(object.getJSONObject("ad_price").getString("price"));
                                            item.setImageResourceId((object.getJSONArray("images").getJSONObject(0).getString("thumb")));
                                            item.setLocation(object.getJSONObject("location").getString("address"));
                                            item.setIsturned(0);

                                            searchedAdList.add(item);
                                        }
                                        MyRecyclerView.setAdapter(itemCatgorySubListAdapter);
                                        itemCatgorySubListAdapter.notifyDataSetChanged();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
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
                    SettingsMain.hideDilog();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info MenuSearch ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info MenuSearch err", String.valueOf(t));
                        Log.d("info MenuSearch err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    void adforest_loadList(JSONObject featureObject, JSONArray searchAds, JSONObject filtertext) {
        searchedAdList.clear();
        featureAdsList.clear();
        try {
            Log.d("search jsonaarry is = ", searchAds.toString());
            if (searchAds.length() > 0)
                for (int i = 0; i < searchAds.length(); i++) {

                    catSubCatlistModel item = new catSubCatlistModel();

                    JSONObject object = searchAds.getJSONObject(i);

                    item.setId(object.getString("ad_id"));
                    item.setCardName(object.getString("ad_title"));
                    item.setDate(object.getString("ad_date"));
                    item.setAdViews(object.getString("ad_views"));
                    item.setPath(object.getString("ad_cats_name"));
                    item.setPrice(object.getJSONObject("ad_price").getString("price"));
                    item.setImageResourceId((object.getJSONArray("images").getJSONObject(0).getString("thumb")));
                    item.setLocation(object.getJSONObject("location").getString("address"));
                    item.setIsturned(0);

                    searchedAdList.add(item);
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Log.d("feature Object is = ", featureObject.getJSONArray("ads").toString());
            textViewTitleFeature.setText(featureObject.getString("text"));
            if (featureObject.getJSONArray("ads").length() > 0)
                for (int i = 0; i < featureObject.getJSONArray("ads").length(); i++) {

                    catSubCatlistModel item = new catSubCatlistModel();
                    JSONObject object = featureObject.getJSONArray("ads").getJSONObject(i);

                    item.setAddTypeFeature(object.getJSONObject("ad_status").getString("featured_type_text"));
                    item.setId(object.getString("ad_id"));
                    item.setCardName(object.getString("ad_title"));
                    item.setDate(object.getString("ad_date"));
                    item.setAdViews(object.getString("ad_views"));
                    item.setPath(object.getString("ad_cats_name"));
                    item.setPrice(object.getJSONObject("ad_price").getString("price"));
                    item.setImageResourceId((object.getJSONArray("ad_images").getJSONObject(0).getString("thumb")));
                    item.setLocation(object.getJSONObject("ad_location").getString("address"));
                    item.setIsfav(object.getJSONObject("ad_saved").getInt("is_saved"));
                    item.setFavBtnText(object.getJSONObject("ad_saved").getString("text"));
                    item.setIsturned(1);
                    featureAdsList.add(item);
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            textViewFilterText.setText(filtertext.getString("count_ads"));
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

        featureAdsList.clear();
        searchedAdList.clear();

        nextPage = 1;
        currentPage = 1;
        totalPage = 0;
    }

    @Override
    public void onResume() {
        if (settingsMain.getAnalyticsShow() && !settingsMain.getAnalyticsId().equals(""))
            AnalyticsTrackers.getInstance().trackScreenView("Simple Search");
        super.onResume();
    }
}