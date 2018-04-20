package com.webprobity.ms_spares_classified.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;
import com.webprobity.ms_spares_classified.Notification.Config;
import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.SplashScreen;
import com.webprobity.ms_spares_classified.ad_detail.Ad_detail_activity;
import com.webprobity.ms_spares_classified.adapters.ItemMainAllCatAdapter;
import com.webprobity.ms_spares_classified.adapters.ItemMainAllLocationAds;
import com.webprobity.ms_spares_classified.adapters.ItemMainCAT_Related_All;
import com.webprobity.ms_spares_classified.adapters.ItemMainHomeRelatedAdapter;
import com.webprobity.ms_spares_classified.adapters.ItemSearchFeatureAdsAdapter;
import com.webprobity.ms_spares_classified.helper.CatSubCatOnclicklinstener;
import com.webprobity.ms_spares_classified.helper.GridSpacingItemDecoration;
import com.webprobity.ms_spares_classified.helper.OnItemClickListener;
import com.webprobity.ms_spares_classified.helper.MyAdsOnclicklinstener;
import com.webprobity.ms_spares_classified.helper.OnItemClickListener2;
import com.webprobity.ms_spares_classified.modelsList.catSubCatlistModel;
import com.webprobity.ms_spares_classified.modelsList.homeCatListModel;
import com.webprobity.ms_spares_classified.modelsList.homeCatRelatedList;
import com.webprobity.ms_spares_classified.modelsList.myAdsModel;
import com.webprobity.ms_spares_classified.utills.AnalyticsTrackers;
import com.webprobity.ms_spares_classified.utills.CustomBorderDrawable;
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

public class FragmentHome extends Fragment {
    public JSONObject jsonObjectSubMenu, responseData;
    String regId;
    ArrayList<homeCatListModel> listitems = new ArrayList<>();
    ArrayList<homeCatListModel> locationAdscat = new ArrayList<>();
    ArrayList<homeCatRelatedList> listitemsRelated = new ArrayList<>();
    ArrayList<catSubCatlistModel> featureAdsList = new ArrayList<>();
    ItemSearchFeatureAdsAdapter itemFeatureAdsAdapter;
    int[] iconsId = {R.drawable.ic_pages, R.drawable.ic_help_outline_black_24dp, R.drawable.ic_about_black_24dp, R.drawable.ic_file};
    LinearLayout featureAboveLayoyut, featurebelowLayoyut, featuredMidLayout;
    Menu menu;
    RestService restService;
    TextView textViewTitleFeature, textViewTitleFeatureBelow, textViewTitleFeatureMid;
    CardView catCardView;
    LinearLayout HomeCustomLayout, staticSlider;
    private SettingsMain settingsMain;
    private ArrayList<catSubCatlistModel> latesetAdsList = new ArrayList<>();
    private ArrayList<catSubCatlistModel> nearByAdsList = new ArrayList<>();
    private RecyclerView mRecyclerView, mRecyclerView2, featuredRecylerViewAbove, featuredRecylerViewBelow, featuredRecylerViewMid;
    private Context context;
    private String btnViewAllText;

    public FragmentHome() {
    }

    public static void adforest_recylerview_autoScroll(final int duration, final int pixelsToMove, final int delayMillis,
                                                       final RecyclerView recyclerView, final GridLayoutManager gridLayoutManager
            , final ItemSearchFeatureAdsAdapter itemFeatureAdsAdapter) {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home, container, false);

        context = getActivity();
        settingsMain = new SettingsMain(getActivity());


        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);

        // get menu from navigationView
        menu = navigationView.getMenu();

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(mRecyclerView, false);

        featureAboveLayoyut = view.findViewById(R.id.featureAboveLayoyut);
        featurebelowLayoyut = view.findViewById(R.id.featureAboveLayoutBelow);
        featuredMidLayout = view.findViewById(R.id.featureLayoutMid);
        textViewTitleFeature = view.findViewById(R.id.textView6);
        textViewTitleFeatureBelow = view.findViewById(R.id.textView7);
        textViewTitleFeatureMid = view.findViewById(R.id.textView8);
        featuredRecylerViewAbove = view.findViewById(R.id.recycler_view3);
        featuredRecylerViewBelow = view.findViewById(R.id.featuredRecylerViewBelow);
        featuredRecylerViewMid = view.findViewById(R.id.featuredRecylerViewMid);
        catCardView = view.findViewById(R.id.card_view);

        HomeCustomLayout = view.findViewById(R.id.HomeCustomLayout);
        staticSlider = view.findViewById(R.id.linear1);


        mRecyclerView2 = view.findViewById(R.id.recycler_view2);


        if (settingsMain.getAppOpen()) {
            restService = UrlController.createService(RestService.class);
        } else
            restService = UrlController.createService(RestService.class, settingsMain.getUserEmail(), settingsMain.getUserPassword(), getActivity());

        ((HomeActivity) getActivity()).changeImage();


        adforest_getAllData();

        if (!settingsMain.getUserLogin().equals("0")) {
            FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
        }
        Log.d("FireBaseId", settingsMain.getFireBaseId());
        SharedPreferences pref = getActivity().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("Firebase Regid", null);
        if (settingsMain.getFireBaseId().equals("")) {
            adforest_AddFirebaseid(regId);

        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void adforest_getAllData() {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());
            Call<ResponseBody> myCall = restService.getHomeDetails(UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info HomeGet Responce", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info data object", "" + response.getJSONObject("data"));
                                Log.d("info Home settings", "" + response.getJSONObject("settings"));
                                responseData = response.getJSONObject("data");

                                getActivity().setTitle(response.getJSONObject("data").getString("page_title"));

                                btnViewAllText = response.getJSONObject("data").getString("view_all");
                                JSONObject sharedSettings = response.getJSONObject("settings");

                                Log.d("bject", "" + menu.size());

                                jsonObjectSubMenu = response.getJSONObject("data").getJSONObject("menu").getJSONObject("submenu");

                                if (jsonObjectSubMenu.getBoolean("has_page")) {

                                    menu.findItem(R.id.custom).setVisible(true);

                                    JSONArray jsonArray = jsonObjectSubMenu.getJSONArray("pages");
                                    menu.findItem(R.id.custom).setTitle(jsonObjectSubMenu.getString("title"));
                                    menu.findItem(R.id.custom).getSubMenu().clear();
                                    for (int i = 0; i < jsonArray.length(); i++) {

                                        menu.findItem(R.id.custom).getSubMenu().add(0, jsonArray.getJSONObject(i).getInt("page_id"), Menu.NONE,
                                                jsonArray.getJSONObject(i).getString("page_title"));
                                        if (i >= 3) {
                                            menu.findItem(R.id.custom).getSubMenu().getItem(i)
                                                    .setIcon(R.drawable.ic_event);
                                        } else
                                            menu.findItem(R.id.custom).getSubMenu().getItem(i).setIcon(iconsId[jsonArray.getJSONObject(i).getInt("icon")]);

                                        menu.findItem(R.id.custom).getSubMenu().getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem menuItem) {

                                                FragmentCustomPages fragment_search = new FragmentCustomPages();
                                                Bundle bundle = new Bundle();

                                                bundle.putString("id", "" + menuItem.getItemId());

                                                fragment_search.setArguments(bundle);
                                                replaceFragment(fragment_search, "FragmentCustomPages");

                                                //Log.d("bject", menuItem.getGroupId() + " ==sdf == " + menuItem.getItemId() + " === " + menuItem.getTitle());
                                                return false;
                                            }
                                        });
                                    }
                                    menu.findItem(R.id.custom).getSubMenu().setGroupCheckable(0, true, true);
                                } else {
                                    menu.findItem(R.id.custom).setVisible(false);
                                }
                                menu.findItem(R.id.home).setTitle(response.getJSONObject("data").getJSONObject("menu").getString("home"));
                                menu.findItem(R.id.search).setTitle(response.getJSONObject("data").getJSONObject("menu").getString("search"));
                                menu.findItem(R.id.packages).setTitle(response.getJSONObject("data").getJSONObject("menu").getString("packages"));
                                if (settingsMain.getAppOpen()) {
                                    menu.findItem(R.id.message).setVisible(false);
                                    menu.findItem(R.id.profile).setVisible(false);
                                    menu.findItem(R.id.myAds).setTitle(response.getJSONObject("data").getJSONObject("menu").getString("login"));
                                    menu.findItem(R.id.inActiveAds).setTitle(response.getJSONObject("data").getJSONObject("menu").getString("register"));
                                    menu.findItem(R.id.myAds).setIcon(R.drawable.ic_login_icon);
                                    menu.findItem(R.id.inActiveAds).setIcon(R.drawable.ic_register_user);
                                    menu.findItem(R.id.featureAds).setVisible(false);
                                    menu.findItem(R.id.favAds).setVisible(false);
                                    menu.findItem(R.id.nav_log_out).setVisible(false);
                                } else {
                                    menu.findItem(R.id.message).setTitle(response.getJSONObject("data").getJSONObject("menu").getString("messages"));
                                    menu.findItem(R.id.profile).setTitle(response.getJSONObject("data").getJSONObject("menu").getString("profile"));
                                    menu.findItem(R.id.myAds).setTitle(response.getJSONObject("data").getJSONObject("menu").getString("my_ads"));
                                    menu.findItem(R.id.inActiveAds).setTitle(response.getJSONObject("data").getJSONObject("menu").getString("inactive_ads"));
                                    menu.findItem(R.id.featureAds).setTitle(response.getJSONObject("data").getJSONObject("menu").getString("featured_ads"));
                                    menu.findItem(R.id.favAds).setTitle(response.getJSONObject("data").getJSONObject("menu").getString("fav_ads"));
                                }
                                menu.findItem(R.id.other).setTitle(response.getJSONObject("data").getJSONObject("menu").getString("others"));
                                menu.findItem(R.id.nav_blog).setTitle(response.getJSONObject("data").getJSONObject("menu").getString("blog"));
                                menu.findItem(R.id.nav_log_out).setTitle(response.getJSONObject("data").getJSONObject("menu").getString("logout"));


                                if (responseData.getBoolean("ads_position_sorter")) {
                                    HomeCustomLayout.setVisibility(View.VISIBLE);
                                    adforest_showDynamicViews(responseData.getJSONArray("ads_position"));
                                } else {
                                    if (response.getJSONObject("data").getJSONArray("cat_icons").length() == 0) {
                                        catCardView.setVisibility(View.GONE);
                                    } else {
                                        staticSlider.setVisibility(View.VISIBLE);
                                        catCardView.setVisibility(View.VISIBLE);
                                        adforest_setAllCatgories(response.getJSONObject("data").getJSONArray("cat_icons"),
                                                response.getJSONObject("data").getInt("cat_icons_column"), mRecyclerView);
                                        adforest_setAllRelated(response.getJSONObject("data").getJSONArray("sliders"), mRecyclerView2);

                                        if (response.getJSONObject("data").getBoolean("is_show_featured")) {
                                            Log.d("info featured_ads", "" + response.getJSONObject("data").getJSONObject("featured_ads"));
                                            JSONObject featuredObject = response.getJSONObject("data").getJSONObject("featured_ads");
                                            String featuredPosition = response.getJSONObject("data").getString("featured_position");
                                            if (featuredPosition.equals("1")) {
                                                featureAboveLayoyut.setVisibility(View.VISIBLE);
                                                adforest_setAllFeaturedAds(featuredObject, featuredRecylerViewAbove, textViewTitleFeature);
                                            }
                                            if (featuredPosition.equals("2")) {
                                                featuredMidLayout.setVisibility(View.VISIBLE);
                                                adforest_setAllFeaturedAds(featuredObject, featuredRecylerViewMid, textViewTitleFeatureMid);
                                            }
                                            if (featuredPosition.equals("3")) {
                                                featurebelowLayoyut.setVisibility(View.VISIBLE);
                                                adforest_setAllFeaturedAds(featuredObject, featuredRecylerViewBelow, textViewTitleFeatureBelow);
                                            }
                                        }
                                    }
                                }

                                settingsMain.setKey("stripeKey", sharedSettings.getJSONObject("appKey").getString("stripe"));

                                settingsMain.setAdsShow(sharedSettings.getJSONObject("ads").getBoolean("show"));
                                if (settingsMain.getAdsShow()) {
                                    settingsMain.setAdsType(sharedSettings.getJSONObject("ads").getString("type"));
                                    if (settingsMain.getAdsType().equals("banner")) {
                                        settingsMain.setAdsPosition(sharedSettings.getJSONObject("ads").getString("position"));
                                    } else {
                                        settingsMain.setAdsInitialTime(sharedSettings.getJSONObject("ads").getString("time_initial"));
                                        settingsMain.setAdsDisplayTime(sharedSettings.getJSONObject("ads").getString("time"));
                                    }
                                    settingsMain.setAdsId(sharedSettings.getJSONObject("ads").getString("ad_id"));
                                }
                                settingsMain.setAnalyticsShow(sharedSettings.getJSONObject("analytics").getBoolean("show"));
                                if (sharedSettings.getJSONObject("analytics").getBoolean("show")) {
                                    settingsMain.setAnalyticsId(sharedSettings.getJSONObject("analytics").getString("id"));
                                    Log.d("analytica======>", sharedSettings.getJSONObject("analytics").getString("id"));
                                }
                                settingsMain.setYoutubeApi(sharedSettings.getJSONObject("appKey").getString("youtube"));
                                googleAnalytics();


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
                    Log.d("info HomeGet error", String.valueOf(t));
                    Log.d("info HomeGet error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });

        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    public void adforest_showDynamicViews(JSONArray jsonArray) {
        Log.d("info ads_position", jsonArray.toString());
        for (int i = 0; i < jsonArray.length(); i++) {
            try {

                if (jsonArray.get(i).equals("cat_icons")) {
                    CardView cardView = new CardView(getActivity());
                    cardView.setCardElevation(3);
                    cardView.setUseCompatPadding(true);
                    cardView.setRadius(0);
                    cardView.setContentPadding(5, 5, 5, 5);


                    RecyclerView recyclerView = new RecyclerView(getActivity());
                    recyclerView.setScrollContainer(false);
                    recyclerView.setPadding(5, 5, 5, 5);

                    recyclerView.setHasFixedSize(true);
                    recyclerView.setNestedScrollingEnabled(false);
                    ViewCompat.setNestedScrollingEnabled(recyclerView, false);
                    cardView.addView(recyclerView);

                    HomeCustomLayout.addView(cardView);
                    Log.d("info ads_position", jsonArray.get(i).toString());
                    if (responseData.getJSONArray("cat_icons").length() == 0) {
                        catCardView.setVisibility(View.GONE);
                    } else
                        adforest_setAllCatgories(responseData.getJSONArray("cat_icons"),
                                responseData.getInt("cat_icons_column"), recyclerView);
                }
                if (jsonArray.get(i).equals("sliders")) {

                    RecyclerView recyclerView = new RecyclerView(getActivity());
                    HomeCustomLayout.addView(recyclerView);
                    adforest_setAllRelated(responseData.getJSONArray("sliders"), recyclerView);
                }
                if (jsonArray.get(i).equals("featured_ads")) {

                    if (responseData.getBoolean("is_show_featured")) {

                        RecyclerView recyclerView = new RecyclerView(getActivity());
                        recyclerView.setPadding(4, 4, 4, 4);

                        TextView textView = new TextView(getActivity());
                        textView.setPadding(5, 5, 5, 5);
                        textView.setTextColor(Color.BLACK);
                        textView.setTextSize(18);

                        HomeCustomLayout.addView(textView);
                        HomeCustomLayout.addView(recyclerView);
                        adforest_setAllFeaturedAds(responseData.getJSONObject("featured_ads"), recyclerView, textView);
                    }
                }
                if (jsonArray.get(i).equals("latest_ads")) {
                    if (responseData.getBoolean("is_show_latest")) {
                        adforest_latesetAdsAndNearBy(responseData.getJSONObject("latest_ads"), latesetAdsList, "latest");
                    }
                }
                if (jsonArray.get(i).equals("cat_locations")) {
                    adforest_locationAds();
                }
                if (jsonArray.get(i).equals("nearby")) {
                    if (responseData.getBoolean("is_show_nearby")) {
                        adforest_latesetAdsAndNearBy(responseData.getJSONObject("nearby_ads"), nearByAdsList, "nearby");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void adforest_setAllFeaturedAds(JSONObject featureObject, RecyclerView featuredRecylerView,
                                            TextView textViewTitleFeature) {
        featureAdsList.clear();

        GridLayoutManager MyLayoutManager2 = new GridLayoutManager(getActivity(), 1);
        MyLayoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);

        featuredRecylerView.setHasFixedSize(true);
        featuredRecylerView.setNestedScrollingEnabled(false);

        featuredRecylerView.setLayoutManager(MyLayoutManager2);
        ViewCompat.setNestedScrollingEnabled(featuredRecylerView, false);

        try {

            textViewTitleFeature.setText(featureObject.getString("text"));
            if (featureObject.getJSONArray("ads").length() > 0)
                for (int i = 0; i < featureObject.getJSONArray("ads").length(); i++) {
                    Log.d("feature Object is = ", featureObject.getJSONArray("ads").toString());

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
            itemFeatureAdsAdapter = new ItemSearchFeatureAdsAdapter(getActivity(), featureAdsList);

            featuredRecylerView.setAdapter(itemFeatureAdsAdapter);

            if (settingsMain.isFeaturedScrollEnable()) {
                adforest_recylerview_autoScroll(settingsMain.getFeaturedScroolDuration(),
                        40, settingsMain.getFeaturedScroolLoop(),
                        featuredRecylerView, MyLayoutManager2, itemFeatureAdsAdapter);
            }

            itemFeatureAdsAdapter.setOnItemClickListener(new CatSubCatOnclicklinstener() {
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
        } catch (JSONException e)

        {
            e.printStackTrace();
        }

    }

    private void adforest_setAllCatgories(JSONArray jsonArray, int noOfCol, RecyclerView recyclerView) {
        listitems = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            homeCatListModel item = new homeCatListModel();
            item.setTitle(jsonArray.optJSONObject(i).optString("name"));
            item.setThumbnail(jsonArray.optJSONObject(i).optString("img"));
            item.setId(jsonArray.optJSONObject(i).optString("cat_id"));

            listitems.add(item);
        }

        GridLayoutManager MyLayoutManager = new GridLayoutManager(getActivity(), noOfCol);
        MyLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(MyLayoutManager);
        int spacing = 30; // 50px

        recyclerView.addItemDecoration(new GridSpacingItemDecoration(noOfCol, spacing, false));

        ItemMainAllCatAdapter adapter = new ItemMainAllCatAdapter(context, listitems, noOfCol);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(homeCatListModel item) {
                //Toast.makeText(getContext(), item.getTitle(), Toast.LENGTH_LONG).show();
                FragmentCatSubNSearch fragment_search = new FragmentCatSubNSearch();
                Bundle bundle = new Bundle();
                bundle.putString("id", item.getId());
                bundle.putString("title", "");

                fragment_search.setArguments(bundle);
                replaceFragment(fragment_search, "FragmentCatSubNSearch");

            }
        });
        Log.d("main cat", "" + listitems.toString());
    }

    private void adforest_setAllRelated(JSONArray jsonArray, RecyclerView recyclerView) {

        listitemsRelated.clear();

        GridLayoutManager MyLayoutManager2 = new GridLayoutManager(getActivity(), 1);
        MyLayoutManager2.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        recyclerView.setLayoutManager(MyLayoutManager2);
        ViewCompat.setNestedScrollingEnabled(recyclerView, false);

        Log.d("data array", "" + jsonArray.length());
        for (int each = 0; each < jsonArray.length(); each++) {
            homeCatRelatedList relateItem = new homeCatRelatedList();
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(each);

                relateItem.setTitle(jsonObject.getString("name"));
                relateItem.setViewAllBtnText(btnViewAllText);
                relateItem.setCatId(jsonObject.getString("cat_id"));
                JSONArray innerList = jsonObject.getJSONArray("data");

                ArrayList<catSubCatlistModel> list = new ArrayList<>();

                for (int i = 0; i < innerList.length(); i++) {
                    catSubCatlistModel item = new catSubCatlistModel();

                    item.setId(innerList.getJSONObject(i).getString("ad_id"));
                    item.setCardName(innerList.getJSONObject(i).getString("ad_title"));
                    item.setDate(innerList.getJSONObject(i).getString("ad_date"));
                    item.setPrice(innerList.getJSONObject(i).getJSONObject("ad_price").getString("price"));
                    item.setLocation(innerList.getJSONObject(i).getJSONObject("ad_location").getString("address"));
                    item.setImageResourceId(innerList.getJSONObject(i).getJSONArray("ad_images").getJSONObject(0).getString("thumb"));
                    list.add(item);
                }

                relateItem.setArrayList(list);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            listitemsRelated.add(relateItem);
        }


        ItemMainCAT_Related_All itemMainCAT_related_all = new ItemMainCAT_Related_All(context, listitemsRelated);
        recyclerView.setAdapter(itemMainCAT_related_all);

        itemMainCAT_related_all.setOnItemClickListener(new MyAdsOnclicklinstener() {
            @Override
            public void onItemClick(myAdsModel item) {

            }

            @Override
            public void delViewOnClick(View v, int position) {
                FragmentCatSubNSearch fragment_search = new FragmentCatSubNSearch();
                Bundle bundle = new Bundle();
                bundle.putString("id", v.getTag().toString());
                bundle.putString("title", "");

                fragment_search.setArguments(bundle);
                replaceFragment(fragment_search, "FragmentCatSubNSearch");
            }

            @Override
            public void editViewOnClick(View v, int position) {

            }
        });

    }

    private void adforest_latesetAdsAndNearBy(JSONObject jsonObject, ArrayList arrayList, String checkAdsType) {

        LinearLayout firstLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        firstLayout.setOrientation(LinearLayout.HORIZONTAL);
        firstLayout.setPadding(2, 2, 2, 2);
        firstLayout.setLayoutParams(params2);


        TextView title = new TextView(getActivity());
        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        params3.weight = 1;
        title.setPadding(3, 3, 3, 3);
        title.setTextColor(Color.BLACK);
        title.setLayoutParams(params3);
        title.setTextSize(18);

        TextView buttonAll = new TextView(getActivity());
        LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonAll.setLayoutParams(params4);

        buttonAll.setPaddingRelative(23, 10, 23, 10);
        buttonAll.setTextColor(Color.WHITE);
        buttonAll.setBackground(CustomBorderDrawable.customButton(0, 0, 0, 0, settingsMain.getMainColor(), settingsMain.getMainColor(), settingsMain.getMainColor(), 3));

        firstLayout.addView(title);
        firstLayout.addView(buttonAll);

        RecyclerView recyclerView = new RecyclerView(getActivity());
        recyclerView.setPadding(4, 4, 4, 4);


        HomeCustomLayout.addView(firstLayout);
        HomeCustomLayout.addView(recyclerView);
        adforest_setAllLatesetAds(title, buttonAll, recyclerView, jsonObject, arrayList, checkAdsType);
    }

    private void adforest_setAllLatesetAds(TextView title, final TextView viewAll, RecyclerView recyclerView,
                                           JSONObject jsonObject, ArrayList arrayList, final String checkAdsType) {

        arrayList.clear();
        GridLayoutManager MyLayoutManager2 = new GridLayoutManager(getActivity(), 1);
        MyLayoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);

        recyclerView.setHasFixedSize(true);

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(MyLayoutManager2);
        ViewCompat.setNestedScrollingEnabled(recyclerView, false);


        try {
            JSONObject object = jsonObject;

            JSONArray data = object.getJSONArray("ads");

            title.setText(object.getString("text"));
            viewAll.setText(responseData.getString("view_all"));

            Log.d("info Lateset Ads", "" + object.toString());

            for (int i = 0; i < data.length(); i++) {
                catSubCatlistModel item = new catSubCatlistModel();

                item.setId(data.getJSONObject(i).getString("ad_id"));
                item.setCardName(data.getJSONObject(i).getString("ad_title"));
                item.setDate(data.getJSONObject(i).getString("ad_date"));
                item.setPrice(data.getJSONObject(i).getJSONObject("ad_price").getString("price"));
                item.setLocation(data.getJSONObject(i).getJSONObject("ad_location").getString("address"));
                item.setImageResourceId(data.getJSONObject(i).getJSONArray("ad_images").getJSONObject(0).getString("thumb"));

                Log.d("Related ads Image", "" + data.getJSONObject(i).getJSONArray("ad_images").getJSONObject(0).getString("thumb"));
                arrayList.add(item);
            }

            ItemMainHomeRelatedAdapter adapter = new ItemMainHomeRelatedAdapter(getActivity(), arrayList);
            recyclerView.setAdapter(adapter);

            adapter.setOnItemClickListener(new OnItemClickListener2() {
                @Override
                public void onItemClick(catSubCatlistModel item) {
                    Log.d("item_id", item.getId());
                    Intent intent = new Intent(getActivity(), Ad_detail_activity.class);
                    intent.putExtra("adId", item.getId());
                    startActivity(intent);
                }
            });

            viewAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkAdsType.equals("nearby")) {
                        FragmentCatSubNSearch fragment_search = new FragmentCatSubNSearch();
                        Bundle bundle = new Bundle();
                        bundle.putString("nearby_latitude", settingsMain.getLatitude());
                        bundle.putString("nearby_longitude", settingsMain.getLongitude());
                        bundle.putString("nearby_distance", settingsMain.getDistance());

                        fragment_search.setArguments(bundle);
                        replaceFragment(fragment_search, "FragmentCatSubNSearch");
                    }
                    if (checkAdsType.equals("latest")) {
                        FragmentCatSubNSearch fragment_search = new FragmentCatSubNSearch();
                        Bundle bundle = new Bundle();
                        bundle.putString("id", "");
                        bundle.putString("title", "");

                        fragment_search.setArguments(bundle);
                        replaceFragment(fragment_search, "FragmentCatSubNSearch");
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void adforest_locationAds() {
        TextView title = new TextView(getActivity());
        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        title.setPadding(3, 3, 3, 3);
        title.setTextColor(Color.BLACK);
        title.setLayoutParams(params3);
        title.setTextSize(17);

        CardView cardView = new CardView(getActivity());
        cardView.setCardElevation(3);
        cardView.setUseCompatPadding(true);
        cardView.setRadius(0);
        cardView.setContentPadding(5, 5, 5, 5);


        RecyclerView recyclerView = new RecyclerView(getActivity());
        recyclerView.setScrollContainer(false);
        recyclerView.setPadding(5, 5, 5, 5);

        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(recyclerView, false);
        cardView.addView(recyclerView);

        HomeCustomLayout.addView(title);
        HomeCustomLayout.addView(cardView);
        try {
            title.setText(responseData.getString("cat_locations_title"));
            adforest_setAllLocationAds(responseData.getJSONArray("cat_locations"), recyclerView);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void adforest_setAllLocationAds(JSONArray jsonArray, RecyclerView recyclerView) {
        locationAdscat.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            homeCatListModel item = new homeCatListModel();
            try {
                Log.d("info location icons",jsonArray.getJSONObject(i).getString("count"));
                item.setTitle(jsonArray.optJSONObject(i).getString("name"));
                item.setThumbnail(jsonArray.optJSONObject(i).getString("img"));
                item.setId(jsonArray.optJSONObject(i).getString("cat_id"));
                item.setAdsCount(jsonArray.getJSONObject(i).getString("count"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            locationAdscat.add(item);
        }

        GridLayoutManager MyLayoutManager = new GridLayoutManager(getActivity(), 2);
        MyLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(MyLayoutManager);
        int spacing = 15; // 50px

        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, false));

        ItemMainAllLocationAds adapter = new ItemMainAllLocationAds(context, locationAdscat, 2);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(homeCatListModel item) {
                FragmentCatSubNSearch fragment_search = new FragmentCatSubNSearch();
                Bundle bundle = new Bundle();
                bundle.putString("ad_country", item.getId());

                fragment_search.setArguments(bundle);
                replaceFragment(fragment_search, "FragmentCatSubNSearch");

            }
        });

        Log.d("main cat", "" + jsonArray.toString());
    }

    void replaceFragment(Fragment someFragment, String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
        transaction.replace(R.id.frameContainer, someFragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    private void googleAnalytics() {
        if (settingsMain.getAnalyticsShow() && !settingsMain.getAnalyticsId().equals("")) {
            AnalyticsTrackers.initialize(getActivity());
            Log.d("analyticsID", settingsMain.getAnalyticsId());
            AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP, settingsMain.getAnalyticsId());
        }

        if (settingsMain.getAnalyticsShow() && !settingsMain.getAnalyticsId().equals("") && AnalyticsTrackers.getInstance() != null)
            AnalyticsTrackers.getInstance().trackScreenView("Home");
        super.onResume();
    }

    private void adforest_AddFirebaseid(String regId) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {


            JsonObject params = new JsonObject();


            params.addProperty("firebase_id", regId);
            Log.e("info send FireBase ", "Firebase reg id: " + regId);

            Call<ResponseBody> myCall = restService.postFirebaseId(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info FireBase Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info Data FireBase", response.getJSONObject("data").toString());
                                settingsMain.setFireBaseId(response.getJSONObject("data").getString("firebase_reg_id"));
                                Log.d("info FireBase ID", response.getJSONObject("data").getString("firebase_reg_id"));
                                Log.d("info FireBase", "Firebase id is set with server.!");
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
                        Log.d("info FireBase ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info FireBase err", String.valueOf(t));
                        Log.d("info FireBase err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else

        {
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }
}
