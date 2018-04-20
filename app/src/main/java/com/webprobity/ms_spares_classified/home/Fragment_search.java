package com.webprobity.ms_spares_classified.home;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.ad_detail.Ad_detail_activity;
import com.webprobity.ms_spares_classified.adapters.ItemCatgorySubListAdapter;
import com.webprobity.ms_spares_classified.adapters.ItemSearchFeatureAdsAdapter;
import com.webprobity.ms_spares_classified.adapters.PlaceArrayAdapter;
import com.webprobity.ms_spares_classified.adapters.SpinnerAndListAdapter;
import com.webprobity.ms_spares_classified.helper.CatSubCatOnclicklinstener;
import com.webprobity.ms_spares_classified.modelsList.catSubCatlistModel;
import com.webprobity.ms_spares_classified.modelsList.subcatDiloglist;
import com.webprobity.ms_spares_classified.utills.AnimationUtils;
import com.webprobity.ms_spares_classified.utills.Network.RestService;
import com.webprobity.ms_spares_classified.utills.SettingsMain;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.webprobity.ms_spares_classified.utills.UrlController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.google.android.gms.location.places.AutocompleteFilter.TYPE_FILTER_ADDRESS;
import static com.google.android.gms.location.places.AutocompleteFilter.TYPE_FILTER_REGIONS;

public class Fragment_search extends Fragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    ArrayList<catSubCatlistModel> searchedAdList = new ArrayList<>();
    ArrayList<catSubCatlistModel> featureAdsList = new ArrayList<>();
    RecyclerView MyRecyclerView, recyclerViewFeatured;
    Button searchBtn;
    //EditText editTextSearch;
    SettingsMain settingsMain;
    TextView textViewTitleFeature, textViewFilterText;

    ItemCatgorySubListAdapter itemCatgorySubListAdapter;
    ItemSearchFeatureAdsAdapter itemSearchFeatureAdsAdapter;
    LinearLayout viewProductLayout, linearhide;
    List<View> allViewInstance = new ArrayList<>();
    List<View> allViewInstanceforCustom = new ArrayList<>();
    JSONObject jsonObject, jsonObjectFilterSpinner, jsonObjectforCustom, jsonObjectPagination;
    JsonObject lastSentParamas;
    ImageView imageViewCollapse;
    LinearLayout linearLayoutCollapse, linearLayoutCustom;
    private Boolean spinnerTouched = false;
    private Boolean spinnerTouched2 = false;
    RestService restService;

    Spinner spinnerFilter;
    LinearLayout linearLayoutFilter;

    String catID;
    boolean isSort = false, ison = false;
    RelativeLayout relativeLayoutSpiner;
    NestedScrollView scrollView;

    ProgressBar progressBar;

    boolean isLoading = false, hasNextPage = false;
    int currentPage = 1, nextPage = 1, totalPage = 0;
    GridLayoutManager MyLayoutManager2;

    String myId, stringCAT_keyName;

    public Fragment_search() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);
        scrollView = getActivity().findViewById(R.id.scrollView);
        progressBar = view.findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            myId = bundle.getString("id", "");
        }
        settingsMain = new SettingsMain(getActivity());
        searchBtn = view.findViewById(R.id.send_button);
        searchBtn.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));

        imageViewCollapse = getActivity().findViewById(R.id.collapse);

        linearhide = view.findViewById(R.id.linearhide);

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

        recyclerViewFeatured.setLayoutManager(MyLayoutManager2);

        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                        scrollY > oldScrollY) {
                    if (hasNextPage) {
                        if (!isLoading) {
                            isLoading = true;
                            progressBar.setVisibility(View.VISIBLE);
                            adforest_loadmore();
                            Log.d("heeeeeeelo", nextPage + "==" + totalPage);
                        }

                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });

        jsonObject = new JSONObject();
        if (settingsMain.getAppOpen()) {
            restService = UrlController.createService(RestService.class);
        } else
            restService = UrlController.createService(RestService.class, settingsMain.getUserEmail(), settingsMain.getUserPassword(), getActivity());

        adforest_getViews();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myId = "";
                adforest_submitQuery(adforest_getDataFromDynamicViews(), "");
            }
        });
        imageViewCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollView.scrollTo(0, 0);
                imageViewCollapse.setVisibility(View.VISIBLE);
                if (linearLayoutCollapse.getVisibility() == View.GONE) {
                    AnimationUtils.slideUp(linearLayoutCollapse);
                    imageViewCollapse.setImageResource(R.drawable.ic_remove_circle_outline);

                } else {
                    AnimationUtils.slideDown(linearLayoutCollapse);
                    imageViewCollapse.setImageResource(R.drawable.ic_search);
                }
            }
        });
        return view;
    }

    private void adforest_submitQuery(JsonObject params, String s) {

        if (!myId.equals("")) {
            params.addProperty(stringCAT_keyName, myId);
        }
        if (isSort) {
            params.addProperty("sort", s);
        }

        if (adforest_getDataFromDynamicViewsForCustom() != null) {
            params.add("custom_fields", adforest_getDataFromDynamicViewsForCustom());
        }

        lastSentParamas = params;

        params.addProperty("page_number", 1);

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            Log.d("info Send SearchData =", "" + params.toString());

            SettingsMain.showDilog(getActivity());
            Call<ResponseBody> myCall = restService.postGetSearchNdLoadMore(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info SearchData Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info SearchData object", "" + response.getJSONObject("data"));

                                Log.d("info SearchData extra", "" + response.getJSONObject("extra"));

                                if (response.getJSONObject("extra").getBoolean("is_show_featured")) {
                                    textViewTitleFeature.setVisibility(View.VISIBLE);
                                } else {
                                    textViewTitleFeature.setVisibility(View.GONE);
                                }
                                jsonObjectPagination = response.getJSONObject("pagination");

                                nextPage = jsonObjectPagination.getInt("next_page");
                                currentPage = jsonObjectPagination.getInt("current_page");
                                totalPage = jsonObjectPagination.getInt("max_num_pages");

                                hasNextPage = jsonObjectPagination.getBoolean("has_next_page");

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
                                        adforest_addToFavourite(position);
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

                                adforest_showFiler();

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
                        Log.d("info SearchData ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info SearchData err", String.valueOf(t));
                        Log.d("info SearchData err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
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

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            Log.d("info data object", "" + lastSentParamas.toString());
            Call<ResponseBody> myCall = restService.postGetSearchNdLoadMore(lastSentParamas, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info searchLoad Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info searchLoadMore obj", "" + response.getJSONObject("data"));

                                isLoading = false;
                                jsonObjectPagination = response.getJSONObject("pagination");

                                nextPage = jsonObjectPagination.getInt("next_page");
                                currentPage = jsonObjectPagination.getInt("current_page");
                                totalPage = jsonObjectPagination.getInt("max_num_pages");
                                hasNextPage = jsonObjectPagination.getBoolean("has_next_page");

                                JSONArray searchAds = response.getJSONObject("data").getJSONArray("ads");

                                try {
                                    Log.d("info search jsonaarry =", searchAds.toString());
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
                        Log.d("info searchLoadMore ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info searchLoadMore err", String.valueOf(t));
                        Log.d("info searchLoadMore err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void adforest_getViews() {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            Call<ResponseBody> myCall = restService.getSearchDetails(UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info Search Details", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                adforest_setViews(response);


                                if (!myId.equals("")) {
                                    adforest_submitQuery(adforest_getDataFromDynamicViews(), "");
                                }
                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        SettingsMain.hideDilog();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        SettingsMain.hideDilog();
                    } catch (IOException e) {
                        e.printStackTrace();
                        SettingsMain.hideDilog();
                    }
                    SettingsMain.hideDilog();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    SettingsMain.hideDilog();
                    Log.d("info ProfileGet error", String.valueOf(t));
                    Log.d("info ProfileGet error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });
        } else {
            SettingsMain.hideDilog();
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
                    item.setIsfav(1);
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

        imageViewCollapse.performClick();
    }


    void adforest_setViews(JSONObject jsonObjec) {

        try {
            jsonObject = jsonObjec;
            Log.d("info Search Data ===== ", jsonObject.toString());
            JSONArray customOptnList = jsonObject.getJSONArray("data");

            getActivity().setTitle(jsonObject.getJSONObject("extra").getString("title"));
            searchBtn.setText(jsonObject.getJSONObject("extra").getString("search_btn"));
            stringCAT_keyName = jsonObject.getJSONObject("extra").getString("field_type_name");


            for (int noOfCustomOpt = 0; noOfCustomOpt < customOptnList.length(); noOfCustomOpt++) {

                CardView cardView = new CardView(getActivity());
                cardView.setCardElevation(2);
                cardView.setUseCompatPadding(true);
                cardView.setRadius(0);
                cardView.setContentPadding(10, 10, 10, 10);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.topMargin = 10;
                params.bottomMargin = 10;
                cardView.setLayoutParams(params);

                final JSONObject eachData = customOptnList.getJSONObject(noOfCustomOpt);
                TextView customOptionsName = new TextView(getActivity());
                customOptionsName.setAllCaps(true);
                customOptionsName.setTextColor(Color.BLACK);
                customOptionsName.setTextSize(12);
                customOptionsName.setPadding(10, 15, 10, 15);

                customOptionsName.setText(eachData.getString("title"));

                LinearLayout linearLayout = new LinearLayout(getActivity());
                linearLayout.setPadding(5, 5, 5, 5);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                linearLayout.addView(customOptionsName);
                if (eachData.getString("field_type").equals("select")) {

                    final JSONArray dropDownJSONOpt = eachData.getJSONArray("values");
                    final ArrayList<subcatDiloglist> SpinnerOptions;
                    SpinnerOptions = new ArrayList<>();
                    for (int j = 0; j < dropDownJSONOpt.length(); j++) {
                        subcatDiloglist subDiloglist = new subcatDiloglist();
                        subDiloglist.setId(dropDownJSONOpt.getJSONObject(j).getString("id"));
                        subDiloglist.setName(dropDownJSONOpt.getJSONObject(j).getString("name"));
                        subDiloglist.setHasSub(dropDownJSONOpt.getJSONObject(j).getBoolean("has_sub"));
                        subDiloglist.setHasCustom(dropDownJSONOpt.getJSONObject(j).getBoolean("has_template"));
                        //String optionString = dropDownJSONOpt.getJSONObject(j).getString("name");
                        SpinnerOptions.add(subDiloglist);
                    }
                    final SpinnerAndListAdapter spinnerAndListAdapter;
                    spinnerAndListAdapter = new SpinnerAndListAdapter(getActivity(), SpinnerOptions, true);
                    final Spinner spinner = new Spinner(getActivity());

                    allViewInstance.add(spinner);
                    spinner.setAdapter(spinnerAndListAdapter);
                    spinner.setSelection(0, false);

                    spinner.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            System.out.println("Real touch felt.");
                            spinnerTouched = true;
                            return false;
                        }
                    });

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            if (spinnerTouched) {
                                //String variant_name = dropDownJSONOpt.getJSONObject(position).getString("name");
                                if (position != 0) {
                                    final subcatDiloglist subcatDiloglistitem = (subcatDiloglist) selectedItemView.getTag();
                                    if (subcatDiloglistitem.isHasSub()) {


                                        if (SettingsMain.isConnectingToInternet(getActivity())) {

                                            SettingsMain.showDilog(getActivity());

                                            //for serlecting the Categoreis if Categoreis have SubCategoreis
                                            try {
                                                if (eachData.getString("field_type_name").equals("ad_cats1")) {

                                                    JsonObject params = new JsonObject();
                                                    params.addProperty("subcat", subcatDiloglistitem.getId());

                                                    Log.d("info sendSearch SubCats", "" + params.toString());

                                                    Call<ResponseBody> myCall = restService.postGetSearcSubCats(params, UrlController.AddHeaders(getActivity()));
                                                    myCall.enqueue(new Callback<ResponseBody>() {
                                                        @Override
                                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                                                            try {
                                                                if (responseObj.isSuccessful()) {
                                                                    Log.d("info GetSubCats Resp", "" + responseObj.toString());

                                                                    JSONObject response = new JSONObject(responseObj.body().string());
                                                                    if (response.getBoolean("success")) {
                                                                        Log.d("info GetSubCats object", "" + response.getJSONObject("data"));
                                                                        spinnerTouched = false;

                                                                        adforest_ShowDialog(response.getJSONObject("data"), subcatDiloglistitem, SpinnerOptions
                                                                                , spinnerAndListAdapter, spinner, eachData.getString("field_type_name"));

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
                                                            SettingsMain.hideDilog();
                                                            Log.d("info GetAdnewPost error", String.valueOf(t));
                                                            Log.d("info GetAdnewPost error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                                                        }
                                                    });
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            //for serlecting the location if location have sabLocations
                                            try {
                                                if (eachData.getString("field_type_name").equals("ad_country")) {

                                                    JsonObject params1 = new JsonObject();
                                                    params1.addProperty("ad_country", subcatDiloglistitem.getId());
                                                    Log.d("info sendSearch Loctn", params1.toString() + eachData.getString("field_type_name"));

                                                    Call<ResponseBody> myCall = restService.postGetSearcSubLocation(params1, UrlController.AddHeaders(getActivity()));
                                                    myCall.enqueue(new Callback<ResponseBody>() {
                                                        @Override
                                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                                                            try {
                                                                if (responseObj.isSuccessful()) {
                                                                    Log.d("info SubSearch Resp", "" + responseObj.toString());

                                                                    JSONObject response = new JSONObject(responseObj.body().string());
                                                                    if (response.getBoolean("success")) {
                                                                        Log.d("info SearchLctn object", "" + response.getJSONObject("data"));
                                                                        spinnerTouched = false;

                                                                        adforest_ShowDialog(response.getJSONObject("data"), subcatDiloglistitem, SpinnerOptions
                                                                                , spinnerAndListAdapter, spinner, eachData.getString("field_type_name"));

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
                                                                Log.d("info SearchLctn ", "NullPointert Exception" + t.getLocalizedMessage());
                                                                settingsMain.hideDilog();
                                                            } else {
                                                                SettingsMain.hideDilog();
                                                                Log.d("info SearchLctn error", String.valueOf(t));
                                                                Log.d("info SearchLctn error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                                                            }
                                                        }
                                                    });

                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            SettingsMain.hideDilog();
                                            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
                                        }
                                        spinnerTouched = false;
                                    }

                                    try {

                                        Log.d("true===== ", "in Main ====  " + subcatDiloglistitem.isHasCustom());

                                        if (eachData.getBoolean("has_cat_template"))
                                            if (subcatDiloglistitem.isHasCustom()) {
                                                linearLayoutCustom.removeAllViews();
                                                allViewInstanceforCustom.clear();
                                                catID = subcatDiloglistitem.getId();
                                                adforest_showCustom();
                                                ison = true;
                                                Log.d("true===== ", "add All");


                                            } else {
                                                if (ison) {
                                                    linearLayoutCustom.removeAllViews();
                                                    allViewInstanceforCustom.clear();
                                                    ison = false;
                                                    Log.d("true===== ", "remove All");

                                                }
                                            }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                } else {
                                    if (ison) {
                                        linearLayoutCustom.removeAllViews();
                                        allViewInstanceforCustom.clear();
                                        ison = false;
                                        Log.d("true===== ", "remove All");
                                    }
                                }
                            }
                            spinnerTouched = false;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                        }

                    });
                    linearLayout.addView(spinner, 1);

                    if (eachData.getString("field_type_name").equals("ad_cats1")) {
                        linearLayoutCustom = new LinearLayout(getActivity());
                        linearLayoutCustom.setPadding(5, 5, 5, 5);
                        linearLayoutCustom.setOrientation(LinearLayout.VERTICAL);
                        linearLayout.addView(linearLayoutCustom, 2);
                    }

                    cardView.addView(linearLayout);
                    viewProductLayout.addView(cardView);
                }
                if (eachData.getString("field_type").equals("textfield")) {
                    TextInputLayout til = new TextInputLayout(getActivity());
                    til.setHint(eachData.getString("title"));
                    EditText et = new EditText(getActivity());
                    til.addView(et);
                    allViewInstance.add(et);
                    cardView.addView(til);
                    viewProductLayout.addView(cardView);
                }
                if (eachData.getString("field_type").equals("glocation_textfield")) {
                    TextInputLayout til = new TextInputLayout(getActivity());

                    til.setHint(eachData.getString("title"));
                    AutoCompleteTextView et = new AutoCompleteTextView(getActivity());

                    if (this.mGoogleApiClient == null)
                        mGoogleApiClient = new GoogleApiClient
                                .Builder(getActivity())
                                .addApi(Places.GEO_DATA_API)
                                .addApi(Places.PLACE_DETECTION_API)
                                .enableAutoManage(getActivity(), this)
                                .addConnectionCallbacks(this)
                                .build();

                    et.setOnItemClickListener(mAutocompleteClickListener);
                    // Create Filter
                    AutocompleteFilter typeFilter = null;
                    if (settingsMain.getAlertDialogMessage("location_type").equals("regions"))
                        typeFilter = new AutocompleteFilter.Builder()
                                .setTypeFilter(TYPE_FILTER_ADDRESS)
                                .build();
                    else {
                        typeFilter = new AutocompleteFilter.Builder()
                                .setTypeFilter(TYPE_FILTER_REGIONS)
                                .build();
                    }

                    mPlaceArrayAdapter = new PlaceArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, typeFilter);
                    et.setAdapter(mPlaceArrayAdapter);
                    til.addView(et);
                    allViewInstance.add(et);
                    cardView.addView(til);
                    viewProductLayout.addView(cardView);
                }

                if (eachData.getString("field_type").equals("range_textfield")) {
                    LinearLayout linearLayout1 = new LinearLayout(getActivity());
                    linearLayout1.setOrientation(LinearLayout.HORIZONTAL);

                    TextInputLayout til = new TextInputLayout(getActivity());
                    TextInputLayout til2 = new TextInputLayout(getActivity());

                    til.setHint(eachData.getJSONArray("data").getJSONObject(0).getString("title"));
                    til2.setHint(eachData.getJSONArray("data").getJSONObject(1).getString("title"));

                    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params2.weight = 1;

                    EditText et = new EditText(getActivity());
                    et.setInputType(InputType.TYPE_CLASS_NUMBER);
                    EditText et2 = new EditText(getActivity());
                    et2.setInputType(InputType.TYPE_CLASS_NUMBER);
                    til.addView(et);
                    til2.addView(et2);

                    til.setLayoutParams(params2);
                    til2.setLayoutParams(params2);
                    linearLayout1.addView(til);
                    linearLayout1.addView(til2);

                    linearLayout.addView(linearLayout1);
                    allViewInstance.add(linearLayout1);
                    cardView.addView(linearLayout);
                    viewProductLayout.addView(cardView);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            jsonObjectFilterSpinner = jsonObject.getJSONObject("topbar");


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
                            adforest_submitQuery(adforest_getDataFromDynamicViews(), dropDownJSONOpt.getJSONObject(i).getString("key"));
                            imageViewCollapse.performClick();

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
    }

    private void adforest_showCustom() {

        if (linearLayoutCustom != null) {

            if (SettingsMain.isConnectingToInternet(getActivity())) {

                JsonObject params = new JsonObject();
                params.addProperty("cat_id", catID);
                Log.d("info sendSearch CatID", catID);
                Call<ResponseBody> myCall = restService.postGetSearchDynamicFields(params, UrlController.AddHeaders(getActivity()));
                myCall.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                        try {
                            if (responseObj.isSuccessful()) {
                                Log.d("info searchDynamic Resp", "" + responseObj.toString());

                                JSONObject response = new JSONObject(responseObj.body().string());
                                if (response.getBoolean("success")) {
                                    Log.d("info searchDynamic obj", "" + response.getJSONArray("data"));
                                    adforest_setViewsForCustom(response);
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
                            Log.d("info searchDynamic ", "NullPointert Exception" + t.getLocalizedMessage());
                            settingsMain.hideDilog();
                        } else {
                            SettingsMain.hideDilog();
                            Log.d("info searchDynamic err", String.valueOf(t));
                            Log.d("info searchDynamic err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                        }
                    }
                });
            } else {
                Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
            }


        }
    }


    private void adforest_ShowDialog(JSONObject data, final subcatDiloglist main,
                                     final ArrayList<subcatDiloglist> spinnerOptionsout,
                                     final SpinnerAndListAdapter spinnerAndListAdapterout,
                                     final Spinner spinner1, final String field_type_name) {

        Log.d("info Dialog Data===== ", "adforest_ShowDialog");
        try {
            Log.d("info Dialog Data===== ", data.getJSONArray("values").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Dialog dialog = new Dialog(getActivity(), R.style.PauseDialog);

        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.dialog_sub_cat);

        dialog.setTitle(main.getName());
        ListView listView = dialog.findViewById(R.id.listView);

        final ArrayList<subcatDiloglist> listitems = new ArrayList<>();
        final JSONArray listArray;
        try {
            listArray = data.getJSONArray("values");
            for (int j = 0; j < listArray.length(); j++) {
                subcatDiloglist subDiloglist = new subcatDiloglist();
                subDiloglist.setId(listArray.getJSONObject(j).getString("id"));
                subDiloglist.setName(listArray.getJSONObject(j).getString("name"));
                subDiloglist.setHasSub(listArray.getJSONObject(j).getBoolean("has_sub"));
                subDiloglist.setHasCustom(listArray.getJSONObject(j).getBoolean("has_template"));
                listitems.add(subDiloglist);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final SpinnerAndListAdapter spinnerAndListAdapter1 = new SpinnerAndListAdapter(getActivity(), listitems);
        listView.setAdapter(spinnerAndListAdapter1);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final subcatDiloglist subcatDiloglistitem = (subcatDiloglist) view.getTag();

                //Log.d("helllo" , spinnerOptionsout.adforest_get(1).getId() + " === " + spinnerOptionsout.adforest_get(1).getName());

                if (!spinnerOptionsout.get(1).getId().equals(subcatDiloglistitem.getId())) {

                    if (subcatDiloglistitem.isHasSub()) {


                        if (SettingsMain.isConnectingToInternet(getActivity())) {

                            SettingsMain.showDilog(getActivity());
                            if (field_type_name.equals("ad_cats1")) {
                                JsonObject params = new JsonObject();
                                params.addProperty("subcat", subcatDiloglistitem.getId());

                                Log.d("info sendDiSubCats", params.toString() + field_type_name);

                                Call<ResponseBody> myCall = restService.postGetSearcSubCats(params, UrlController.AddHeaders(getActivity()));
                                myCall.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                                        try {
                                            if (responseObj.isSuccessful()) {
                                                Log.d("info DiSubCats Resp", "" + responseObj.toString());

                                                JSONObject response = new JSONObject(responseObj.body().string());
                                                if (response.getBoolean("success")) {
                                                    Log.d("info DidSubCats object", "" + response.getJSONObject("data"));

                                                    adforest_ShowDialog(response.getJSONObject("data"), subcatDiloglistitem, spinnerOptionsout
                                                            , spinnerAndListAdapterout, spinner1, field_type_name);

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
                                            Log.d("info DidSubCats ", "NullPointert Exception" + t.getLocalizedMessage());
                                            settingsMain.hideDilog();
                                        } else {
                                            SettingsMain.hideDilog();
                                            Log.d("info DiaSubCats error", String.valueOf(t));
                                            Log.d("info DiaSubCats error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                                        }
                                    }
                                });
                            }
                            if (field_type_name.equals("ad_country")) {

                                JsonObject params1 = new JsonObject();
                                params1.addProperty("ad_country", subcatDiloglistitem.getId());
                                Log.d("info DiSubLocation", params1.toString() + field_type_name);

                                Call<ResponseBody> myCall = restService.postGetSearcSubLocation(params1, UrlController.AddHeaders(getActivity()));
                                myCall.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                                        try {
                                            if (responseObj.isSuccessful()) {
                                                Log.d("info DiSubLocation Resp", "" + responseObj.toString());

                                                JSONObject response = new JSONObject(responseObj.body().string());
                                                if (response.getBoolean("success")) {
                                                    Log.d("info DiSubLocation obj", "" + response.getJSONObject("data"));

                                                    adforest_ShowDialog(response.getJSONObject("data"), subcatDiloglistitem, spinnerOptionsout
                                                            , spinnerAndListAdapterout, spinner1, field_type_name);

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
                                            Log.d("info DiSubLocation ", "NullPointert Exception" + t.getLocalizedMessage());
                                            settingsMain.hideDilog();
                                        } else {
                                            SettingsMain.hideDilog();
                                            Log.d("info DiSubLocation err", String.valueOf(t));
                                            Log.d("info DiSubLocation err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                                        }
                                    }
                                });

                            }
                        } else {
                            SettingsMain.hideDilog();
                            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
                        }


                    } else {

                        for (int ii = 0; ii < spinnerOptionsout.size(); ii++) {
                            if (spinnerOptionsout.get(ii).getId().equals(subcatDiloglistitem.getId())) {
                                spinnerOptionsout.remove(ii);
                                Log.d("info ===== ", "else of list inner is 1st button into for loop");
                                break;
                            }
                        }
                        Log.d("info ===== ", "else of list inner is 1st button out of for loop");

                        spinnerOptionsout.add(1, subcatDiloglistitem);
                        spinner1.setSelection(1, false);
                        spinnerAndListAdapterout.notifyDataSetChanged();

                    }

                    Log.d("true===== ", "in dalog ====  " + subcatDiloglistitem.isHasCustom());

                    if (subcatDiloglistitem.isHasCustom()) {
                        linearLayoutCustom.removeAllViews();
                        allViewInstanceforCustom.clear();
                        catID = subcatDiloglistitem.getId();
                        adforest_showCustom();
                        Log.d("true===== ", "inter add All");

                    } else {
                        linearLayoutCustom.removeAllViews();
                        allViewInstanceforCustom.clear();
                        ison = false;
                        Log.d("true===== ", "inter remove All");
                    }
                } else {
                    spinner1.setSelection(1, false);
                    Log.d("info ===== ", "else of chk is 1st button out");

                }
                dialog.dismiss();
            }
        });

        Button Send = dialog.findViewById(R.id.send_button);
        Button Cancel = dialog.findViewById(R.id.cancel_button);

        try {
            Send.setText(jsonObject.getJSONObject("extra").getString("dialog_send"));
            Cancel.setText(jsonObject.getJSONObject("extra").getString("dialg_cancel"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Send.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));
        Cancel.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));

        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < spinnerOptionsout.size(); i++) {
                    if (spinnerOptionsout.get(i).getId().equals(main.getId())) {
                        spinnerOptionsout.remove(i);
                        Log.d("info ===== ", "send button in");
                        break;
                    }
                }

                spinnerOptionsout.add(1, main);
                spinnerAndListAdapterout.notifyDataSetChanged();
                spinner1.setSelection(1, false);
                Log.d("info ===== ", "send button out");

                dialog.dismiss();
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    public JsonObject adforest_getDataFromDynamicViews() {
        JsonObject optionsObj = null;

        try {
            JSONArray customOptnList = jsonObject.getJSONArray("data");
            optionsObj = new JsonObject();
            for (int noOfViews = 0; noOfViews < customOptnList.length(); noOfViews++) {
                JSONObject eachData = customOptnList.getJSONObject(noOfViews);

                if (eachData.getString("field_type").equals("select")) {
                    Spinner spinner = (Spinner) allViewInstance.get(noOfViews);

                    subcatDiloglist subcatDiloglist1 = (subcatDiloglist) spinner.getSelectedView().getTag();
                    JSONArray dropDownJSONOpt = eachData.getJSONArray("values");
                    String variant_name = dropDownJSONOpt.getJSONObject(spinner.getSelectedItemPosition()).getString("id");
                    Log.d("value id", variant_name + "");
                    Log.d("zia", "zia = " + subcatDiloglist1.getId());

                    optionsObj.addProperty(eachData.getString("field_type_name"),
                            "" + subcatDiloglist1.getId());
                }
                if (eachData.getString("field_type").equals("textfield")) {
                    TextView textView = (TextView) allViewInstance.get(noOfViews);
                    if (!textView.getText().toString().equalsIgnoreCase(""))
                        optionsObj.addProperty(eachData.getString("field_type_name"), textView.getText().toString());
                    else
                        optionsObj.addProperty(eachData.getString("field_type_name"), textView.getText().toString());
                    Log.d("variant_name", textView.getText().toString() + "");
                }

                if (eachData.getString("field_type").equals("glocation_textfield")) {
                    TextView textView = (TextView) allViewInstance.get(noOfViews);
                    if (!textView.getText().toString().equalsIgnoreCase(""))
                        optionsObj.addProperty(eachData.getString("field_type_name"), textView.getText().toString());
                    else
                        optionsObj.addProperty(eachData.getString("field_type_name"), textView.getText().toString());
                    Log.d("variant_name", textView.getText().toString() + "");
                }
                if (eachData.getString("field_type").equals("range_textfield")) {
                    LinearLayout linearLayout = (LinearLayout) allViewInstance.get(noOfViews);

                    TextInputLayout textView = (TextInputLayout) linearLayout.getChildAt(0);
                    TextInputLayout textView2 = (TextInputLayout) linearLayout.getChildAt(1);

                    if (textView.getEditText() != null && textView2.getEditText() != null)
                        optionsObj.addProperty(eachData.getString("field_type_name"), textView.getEditText().getText().toString() + "-" +
                                textView2.getEditText().getText().toString());
                }
            }

            hideSoftKeyboard();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("array us", (optionsObj != null ? optionsObj.toString() : null) + " ==== size====  " + allViewInstance.size());

        return optionsObj;
    }

    public JsonObject adforest_getDataFromDynamicViewsForCustom() {
        JsonObject optionsObj = null;

        if (jsonObjectforCustom != null) {
            try {
                JSONArray customOptnList = jsonObjectforCustom.getJSONArray("data");
                optionsObj = new JsonObject();
                for (int noOfViews = 0; noOfViews < customOptnList.length(); noOfViews++) {
                    JSONObject eachData = customOptnList.getJSONObject(noOfViews);

                    if (eachData.getString("field_type").equals("select")) {
                        Spinner spinner = (Spinner) allViewInstanceforCustom.get(noOfViews);

                        subcatDiloglist subcatDiloglist1 = (subcatDiloglist) spinner.getSelectedView().getTag();
                        JSONArray dropDownJSONOpt = eachData.getJSONArray("values");
                        String variant_name = dropDownJSONOpt.getJSONObject(spinner.getSelectedItemPosition()).getString("id");
                        Log.d("value id", variant_name + "");
                        Log.d("zia", "zia = " + subcatDiloglist1.getId());

                        optionsObj.addProperty(eachData.getString("field_type_name"),
                                "" + subcatDiloglist1.getId());
                    }
                    if (eachData.getString("field_type").equals("textfield")) {
                        TextView textView = (TextView) allViewInstanceforCustom.get(noOfViews);
                        if (!textView.getText().toString().equalsIgnoreCase(""))
                            optionsObj.addProperty(eachData.getString("field_type_name"), textView.getText().toString());
                        else
                            optionsObj.addProperty(eachData.getString("field_type_name"), textView.getText().toString());
                        Log.d("variant_name", textView.getText().toString() + "");
                    }

                    if (eachData.getString("field_type").equals("radio")) {
                        RadioGroup radioGroup = (RadioGroup) allViewInstanceforCustom.get(noOfViews);
                        RadioButton selectedRadioBtn = getActivity().findViewById(radioGroup.getCheckedRadioButtonId());
                        Log.d("variant_name", selectedRadioBtn.getTag().toString() + "");
                        optionsObj.addProperty(eachData.getString("field_type_name"),
                                "" + selectedRadioBtn.getTag().toString());
                    }
                }

                hideSoftKeyboard();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d("array us custom", (optionsObj != null ? optionsObj.toString() : null) + " ==== size====  " + allViewInstanceforCustom.size());

        return optionsObj;
    }

    void adforest_setViewsForCustom(JSONObject jsonObjec) {

        try {
            jsonObjectforCustom = jsonObjec;
            Log.d("Custom data ===== ", jsonObjectforCustom.toString());
            JSONArray customOptnList = jsonObjectforCustom.getJSONArray("data");

            for (int noOfCustomOpt = 0; noOfCustomOpt < customOptnList.length(); noOfCustomOpt++) {
                CardView cardView = new CardView(getActivity());
                cardView.setCardElevation(2);
                cardView.setUseCompatPadding(true);
                cardView.setRadius(0);
                cardView.setContentPadding(10, 10, 10, 10);
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params1.topMargin = 10;
                params1.bottomMargin = 10;
                cardView.setLayoutParams(params1);

                JSONObject eachData = customOptnList.getJSONObject(noOfCustomOpt);
                TextView customOptionsName = new TextView(getActivity());
                customOptionsName.setTextSize(12);
                customOptionsName.setAllCaps(true);
                customOptionsName.setTextColor(Color.BLACK);
                customOptionsName.setPadding(10, 15, 10, 15);
                customOptionsName.setText(eachData.getString("title"));

                LinearLayout linearLayout = new LinearLayout(getActivity());
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                linearLayout.addView(customOptionsName);
                if (eachData.getString("field_type").equals("select")) {

                    final JSONArray dropDownJSONOpt = eachData.getJSONArray("values");
                    final ArrayList<subcatDiloglist> SpinnerOptions;
                    SpinnerOptions = new ArrayList<>();
                    for (int j = 0; j < dropDownJSONOpt.length(); j++) {
                        subcatDiloglist subDiloglist = new subcatDiloglist();
                        subDiloglist.setId(dropDownJSONOpt.getJSONObject(j).getString("id"));
                        subDiloglist.setName(dropDownJSONOpt.getJSONObject(j).getString("name"));
                        subDiloglist.setHasSub(dropDownJSONOpt.getJSONObject(j).getBoolean("has_sub"));
                        //String optionString = dropDownJSONOpt.getJSONObject(j).getString("name");
                        SpinnerOptions.add(subDiloglist);
                    }
                    final SpinnerAndListAdapter spinnerAndListAdapter;

                    spinnerAndListAdapter = new SpinnerAndListAdapter(getActivity(), SpinnerOptions);

                    final Spinner spinner = new Spinner(getActivity());

                    allViewInstanceforCustom.add(spinner);
                    spinner.setAdapter(spinnerAndListAdapter);
                    spinner.setSelection(0, false);

                    spinner.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            spinnerTouched = true;
                            return false;
                        }
                    });

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            if (spinnerTouched) {
                                //String variant_name = dropDownJSONOpt.getJSONObject(position).getString("name");
                                if (position != 0) {
                                    final subcatDiloglist subcatDiloglistitem = (subcatDiloglist) selectedItemView.getTag();

//                                    Toast.makeText(getActivity(), subcatDiloglistitem.getName(), Toast.LENGTH_SHORT).show();

                                }
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                        }

                    });
                    linearLayout.addView(spinner, 1);
                    cardView.addView(linearLayout);
                    linearLayoutCustom.addView(cardView);
                }
                if (eachData.getString("field_type").equals("textfield")) {
                    TextInputLayout til = new TextInputLayout(getActivity());
                    til.setHint(eachData.getString("title"));
                    EditText et = new EditText(getActivity());
                    til.addView(et);
                    allViewInstanceforCustom.add(et);
                    linearLayoutCustom.addView(til);
                }

                if (eachData.getString("field_type").equals("radio")) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.topMargin = 3;
                    params.bottomMargin = 3;

                    final JSONArray radioButtonJSONOpt = eachData.getJSONArray("values");
                    RadioGroup rg = new RadioGroup(getActivity()); //create the RadioGroup
                    allViewInstanceforCustom.add(rg);
                    for (int j = 0; j < radioButtonJSONOpt.length(); j++) {

                        RadioButton rb = new RadioButton(getActivity());
                        rg.addView(rb, params);
                        if (j == 0)
                            rb.setChecked(true);
                        rb.setLayoutParams(params);
                        rb.setTag(radioButtonJSONOpt.getJSONObject(j).getString("id"));
                        rb.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        String optionString = radioButtonJSONOpt.getJSONObject(j).getString("name");
                        rb.setText(optionString);
                        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                View radioButton = group.findViewById(checkedId);
                                String variant_name = radioButton.getTag().toString();
                                Toast.makeText(getActivity(), variant_name + "", Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                    linearLayout.addView(rg, params);
                    linearLayoutCustom.addView(linearLayout);
                }

                if (eachData.getString("field_type").equals("range_textfield")) {
                    LinearLayout linearLayout1 = new LinearLayout(getActivity());
                    linearLayout1.setOrientation(LinearLayout.HORIZONTAL);

                    TextInputLayout til = new TextInputLayout(getActivity());
                    TextInputLayout til2 = new TextInputLayout(getActivity());

                    til.setHint(eachData.getJSONArray("data").getJSONObject(0).getString("title"));
                    til2.setHint(eachData.getJSONArray("data").getJSONObject(1).getString("title"));

                    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params2.weight = 1;

                    EditText et = new EditText(getActivity());
                    et.setInputType(InputType.TYPE_CLASS_NUMBER);
                    EditText et2 = new EditText(getActivity());
                    et2.setInputType(InputType.TYPE_CLASS_NUMBER);
                    til.addView(et);
                    til2.addView(et2);

                    til.setLayoutParams(params2);
                    til2.setLayoutParams(params2);
                    linearLayout1.addView(til);
                    linearLayout1.addView(til2);

                    linearLayout.addView(linearLayout1);
                    allViewInstanceforCustom.add(linearLayout1);
                    cardView.addView(linearLayout);
                    linearLayoutCustom.addView(cardView);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void hideSoftKeyboard() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId;
            if (item != null) {
                placeId = String.valueOf(item.placeId);
                Log.i("sdfsdf", "Selected: " + item.description);
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
                Log.i("sdfsdf", "Fetching details for ID: " + item.placeId);
            }
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e("addfadsfa", "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            Log.e("addfadsfa", "Place query did not complete. Error: " +
                    place.getLatLng().toString());

        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(),
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);

    }

    @Override
    public void onStop() {
        super.onStop();

        adforest_showFiler();
    }

    @Override
    public void onStart() {
        super.onStart();

        adforest_showFiler();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.mGoogleApiClient != null) {
            this.mGoogleApiClient.connect();
        }

        adforest_showFiler();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.mGoogleApiClient != null) {
            this.mGoogleApiClient.disconnect();
        }

        adforest_showFiler();
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

        allViewInstance.clear();
    }

    void adforest_showFiler() {

        if (MyRecyclerView.getAdapter() != null && MyRecyclerView.getAdapter().getItemCount() >= 0) {
            linearLayoutFilter.setVisibility(View.VISIBLE);
            if (MyRecyclerView.getAdapter().getItemCount() == 0) {
                relativeLayoutSpiner.setVisibility(View.GONE);
            } else
                relativeLayoutSpiner.setVisibility(View.VISIBLE);

        } else
            linearLayoutFilter.setVisibility(View.GONE);
    }

    void adforest_addToFavourite(String Id) {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("ad_id", Id);
            Log.d("info sendFavourite", Id);
            Call<ResponseBody> myCall = restService.postAddToFavourite(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info AdToFav Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();

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
                        Log.d("info AdToFav ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info AdToFav error", String.valueOf(t));
                        Log.d("info AdToFav error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private static void adforest_recylerview_autoScroll(final int duration, final int pixelsToMove, final int delayMillis,
                                                        final RecyclerView recyclerView, final GridLayoutManager gridLayoutManager, final ItemSearchFeatureAdsAdapter itemFeatureAdsAdapter) {
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
}
