package com.webprobity.ms_spares_classified.messages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.messaging.FirebaseMessaging;
import com.webprobity.ms_spares_classified.Notification.Config;
import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.utills.AnalyticsTrackers;
import com.webprobity.ms_spares_classified.utills.SettingsMain;

public class ChatActivity extends AppCompatActivity {

    SettingsMain settingsMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        settingsMain = new SettingsMain(this);

        toolbar.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(settingsMain.getMainColor()));
        }
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        if (!intent.getStringExtra("senderId").equals("")) {

            ChatFragment chatFragment = new ChatFragment();
            Bundle bundle = new Bundle();
            bundle.putString("adId", intent.getStringExtra("adId"));
            bundle.putString("senderId", intent.getStringExtra("senderId"));
            bundle.putString("recieverId", intent.getStringExtra("recieverId"));
            bundle.putString("type", intent.getStringExtra("type"));
            chatFragment.setArguments(bundle);
            startFragment(chatFragment);
        } else {
            RecievedOffersList recievedOffersList = new RecievedOffersList();
            Bundle bundle = new Bundle();
            bundle.putString("adId", intent.getStringExtra("adId"));
            recievedOffersList.setArguments(bundle);
            startFragment(recievedOffersList);
        }
        if (settingsMain.getAnalyticsShow() && !settingsMain.getAnalyticsId().equals("")) {
            AnalyticsTrackers.initialize(this);
            Log.d("analyticsID", settingsMain.getAnalyticsId());
            AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP, settingsMain.getAnalyticsId());
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            ChatActivity.this.recreate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startFragment(Fragment someFragment) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.frameContainer);

        if (fragment == null) {
            fragment = someFragment;
            fm.beginTransaction()
                    .add(R.id.frameContainer, fragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_enter, R.anim.right_out);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    protected void onResume() {
        if (settingsMain.getAnalyticsShow() && !settingsMain.getAnalyticsId().equals(""))
            AnalyticsTrackers.getInstance().trackScreenView("Chat Box");
        super.onResume();
    }
}
