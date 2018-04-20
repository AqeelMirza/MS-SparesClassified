package com.webprobity.ms_spares_classified.utills;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by apple on 11/21/17.
 */

public class Admob {
    private static SettingsMain settingsMain;
    static Runnable loader=null;
    public static final String TAG = Admob.class.getSimpleName();
    private static ScheduledFuture loaderHandler;

    public static void loadInterstitial(final Activity activity) {
        settingsMain =new SettingsMain(activity);
        try {
            loader = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Loading Admob interstitial...");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final InterstitialAd interstitial = new InterstitialAd(activity);
                            interstitial.setAdUnitId(settingsMain.getAdsId());
                            AdRequest adRequest = new AdRequest.Builder().build();
                            interstitial.loadAd(adRequest);
                            interstitial.setAdListener(new AdListener() {
                                public void onAdLoaded() {
                                    if (interstitial != null && interstitial.isLoaded()) {
                                        adforest_ADsdisplayInterstitial(interstitial);
                                    }
                                }
                                @Override
                                public void onAdFailedToLoad(int i) {
                                    loadInterstitial(activity);
                                    Log.d(TAG, "Ad failed to loadvand error code is " + i);
                                }
                            });
                        }
                    });
                }
            };
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            loaderHandler = scheduler.scheduleWithFixedDelay(loader, settingsMain.getAdsInitialTime(),
                    settingsMain.getAdsDisplayTime(), TimeUnit.SECONDS);

        } catch (Exception e) {
            Log.d("AdException===>", e.toString());
        }
    }

    public static void adforest_Displaybanners(final Activity activity, final LinearLayout frameLayout){
        settingsMain =new SettingsMain(activity);
        final AdView mAdView = new AdView(activity);
        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdUnitId(settingsMain.getAdsId());
        frameLayout.addView(mAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {

//                mAdView.setVisibility(View.VISIBLE);
//                frameLayout.setVisibility(View.INVISIBLE);
            }


            @Override
            public void onAdFailedToLoad(int i) {
                adforest_Displaybanners(activity,frameLayout);
                Log.d(TAG, "Ad failed to loadvand error code is " + i);
            }


            @Override
            public void onAdLeftApplication() {
            }


            @Override
            public void onAdOpened() {
            }


            @Override
            public void onAdLoaded() {
                frameLayout.setVisibility(View.VISIBLE);
                Log.d(TAG, "Ad has has loaded to load");
            }
        });
    }

    private static void adforest_ADsdisplayInterstitial(final InterstitialAd interstitialAd) {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    public static void adforest_cancelInterstitial() {
        if (loaderHandler != null) {
            loaderHandler.cancel(true);
        }
    }
}


