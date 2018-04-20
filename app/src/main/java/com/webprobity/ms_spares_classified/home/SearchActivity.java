package com.webprobity.ms_spares_classified.home;

import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.utills.Admob;
import com.webprobity.ms_spares_classified.utills.AnalyticsTrackers;
import com.webprobity.ms_spares_classified.utills.SettingsMain;

public class SearchActivity extends AppCompatActivity {

    SettingsMain settingsMain;
    String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        settingsMain = new SettingsMain(this);

        id = getIntent().getStringExtra("id");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(settingsMain.getMainColor()));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView imageViewRefresh = (ImageView) findViewById(R.id.refresh);
        ImageView imageView = (ImageView) findViewById(R.id.collapse);
        imageView.setVisibility(View.GONE);

        imageViewRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.this.recreate();
            }
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setTitle("Search");
        toolbar.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));

        Fragment_search fragment_search = new Fragment_search();
        Bundle bundle = new Bundle();

        if (!id.equals("")) {
            bundle.putString("id", id);
        } else {
            bundle.putString("id", "");
        }
        fragment_search.setArguments(bundle);
        adforest_bannersAds();
        startFragment(fragment_search);
    }


    public void adforest_bannersAds() {
        if (settingsMain.getAdsShow() && !settingsMain.getAdsId().equals("")) {
            if (settingsMain.getAdsPostion().equals("top")) {
                LinearLayout frameLayout = (LinearLayout) findViewById(R.id.adSearcAd);
                Admob.adforest_Displaybanners(this, frameLayout);
            }
            else
            {
                LinearLayout frameLayout = (LinearLayout) findViewById(R.id.adSearcAdBottom);
                FrameLayout maimFrame = (FrameLayout) findViewById(R.id.frameContainer);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.bottomMargin = 80;
                maimFrame.setLayoutParams(layoutParams);
                Admob.adforest_Displaybanners(this, frameLayout);
            }
        }
    }

    public void startFragment(Fragment someFragment) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.frameContainer);

        if (fragment == null) {
            fragment = someFragment;

            fm.beginTransaction()
                    .add(R.id.frameContainer, fragment)
                    .commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getSupportFragmentManager().findFragmentByTag("Fragment_search") != null)
            getSupportFragmentManager().findFragmentByTag("Fragment_search").setRetainInstance(true);
    }

    @Override
    protected void onResume() {
        if (settingsMain.getAnalyticsShow() && !settingsMain.getAnalyticsId().equals("")) {
            AnalyticsTrackers.getInstance().trackScreenView("Advance Search");
        }
        super.onResume();
        if (getSupportFragmentManager().findFragmentByTag("Fragment_search") != null)
            getSupportFragmentManager().findFragmentByTag("Fragment_search").getRetainInstance();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_enter, R.anim.right_out);
    }
}
