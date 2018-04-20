package com.webprobity.ms_spares_classified.ad_detail;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.home.AddNewAdPost;
import com.webprobity.ms_spares_classified.utills.Admob;
import com.webprobity.ms_spares_classified.utills.AnalyticsTrackers;
import com.webprobity.ms_spares_classified.utills.SettingsMain;

public class Ad_detail_activity extends AppCompatActivity {

    SettingsMain settingsMain;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_detail_activity);

        settingsMain = new SettingsMain(this);
        intent = getIntent();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(settingsMain.getMainColor()));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(settingsMain.getMainColor())));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Ad_detail_activity.this, AddNewAdPost.class);
                startActivity(intent);
            }
        });

        toolbar.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));

        if (settingsMain.getAppOpen()) {
            fab.setVisibility(View.GONE);
        }
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (settingsMain.getAdsType().equals("banner")) {
            if (settingsMain.getAdsShow() && !settingsMain.getAdsId().equals("")) {
                if (settingsMain.getAdsPostion().equals("top")) {
                    LinearLayout frameLayout = (LinearLayout) findViewById(R.id.AdDetailsAdmob);
                    Admob.adforest_Displaybanners(Ad_detail_activity.this, frameLayout);
                } else {
                    LinearLayout frameLayout = (LinearLayout) findViewById(R.id.AdDetailsAdmobBottom);
                    RelativeLayout maimFrame = (RelativeLayout) findViewById(R.id.adDetailsLayout);
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);

                    int width = size.x;
                    int height = size.y;

                    Admob.adforest_Displaybanners(this, frameLayout);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    layoutParams.bottomMargin = height/16;
                    maimFrame.setLayoutParams(layoutParams);
                    CoordinatorLayout.LayoutParams layoutParams2 = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams2.bottomMargin = height/6;
                    layoutParams2.setMarginStart(16);
                    layoutParams2.setMarginEnd(16);
                    layoutParams2.leftMargin=16;
                    layoutParams2.rightMargin=16;
                    layoutParams2.gravity= Gravity.BOTTOM|Gravity.END;
                    fab.setLayoutParams(layoutParams2);
                }
            }
        }

        FragmentAdDetail fragmentAdDetail = new FragmentAdDetail();

        Bundle bundle = new Bundle();
        bundle.putString("id", intent.getStringExtra("adId"));
        fragmentAdDetail.setArguments(bundle);

        startFragment(fragmentAdDetail, "FragmentAdDetail");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_enter, R.anim.right_out);
    }

    public void startFragment(Fragment someFragment, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(tag);

        if (fragment == null) {
            fragment = someFragment;
            fm.beginTransaction()
                    .add(R.id.frameContainer, fragment, tag).commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        if (settingsMain.getAnalyticsShow() && !settingsMain.getAnalyticsId().equals(""))
            AnalyticsTrackers.getInstance().trackScreenView("Ad Details");

        super.onResume();
    }
}
