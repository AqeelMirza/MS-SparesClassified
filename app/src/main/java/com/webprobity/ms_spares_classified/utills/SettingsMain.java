package com.webprobity.ms_spares_classified.utills;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.webprobity.ms_spares_classified.R;

import java.io.ByteArrayOutputStream;

public class SettingsMain {
    public static final String PREF_NAME = "com.adforest";
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private static Dialog dialog1;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    // Constructor
    @SuppressLint("CommitPrefEdits")
    public SettingsMain(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /* Checking Internet Connection */
    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {

                        return true;
                    }
        }
        return false;
    }

    public static void showDilog(Context context) {
        SettingsMain settingsMain = new SettingsMain(context);

        dialog1 = new Dialog(context, R.style.AppTheme);
        dialog1.setContentView(R.layout.dilog_progressbar);
        dialog1.setCancelable(false);
        TextView textView = dialog1.findViewById(R.id.id_title);
        textView.setText(settingsMain.getAlertDialogMessage("waitMessage"));
        dialog1.show();
    }

    public static void hideDilog() {
        dialog1.dismiss();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static String getRealPathFromURI(Context inContext, Uri uri) {
        @SuppressLint("Recycle") Cursor cursor = inContext.getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                return cursor.getString(idx);

            }
        } catch (Exception e) {
            Log.d("info GetReal Path Error", e.toString());
        } finally {
            try {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
            } catch (Exception ex) {
                Log.d("info GetReal Path Error", ex.toString());

            }
        }

        return "";
    }

    public static void reload(Context context, String tag) {
        Fragment frg;
        FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();

        frg = manager.findFragmentByTag(tag);
        final FragmentTransaction ft = manager.beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commit();
    }

    public static boolean isSocial(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String restoredText = pref.getString("isSocial", "false");
        return restoredText.equals("true");
    }

    public String getAlertDialogMessage(String type) {
        return pref.getString(type, "You Are Not Connected To Internet. Please Check Your Internet Connection and Try Again");
    }

    public void setAlertDialogMessage(String type, String msg) {
        editor.putString(type, msg);
        editor.commit();
    }

    public String getKey(String name) {
        return pref.getString(name, "");
    }

    public void setKey(String name, String value) {
        editor.putString(name, value);
        editor.commit();
    }

    public String getAlertDialogTitle(String type) {
        return pref.getString(type, "Error");
    }

    public void setAlertDialogTitle(String type, String title) {
        editor.putString(type, title);
        editor.commit();
    }

    public String getAlertOkText() {
        return pref.getString("AlertOkText", "OK");
    }

    public void setAlertOkText(String msg) {
        editor.putString("AlertOkText", msg);
        editor.commit();
    }

    public String getAlertCancelText() {
        return pref.getString("AlertCancelText", "CANCEL");
    }

    public void setAlertCancelText(String msg) {
        editor.putString("AlertCancelText", msg);
        editor.commit();
    }

    public void setUserPhone(String userPhone) {
        editor.putString("phone", userPhone);
        editor.commit();
    }

    public String getUserImage() {
        return pref.getString("image", "0");
    }

    public void setUserImage(String userImage) {
        editor.putString("image", userImage);
        editor.commit();
    }

    public String getUserLogin() {
        return pref.getString("login", "0");
    }

    public void setUserLogin(String userLogin) {
        editor.putString("login", userLogin);
        editor.commit();
    }

    public String getMainColor() {
        return pref.getString("mainColor", "000000");
    }

    public void setMainColor(String mainColor) {
        editor.putString("mainColor", mainColor);
        editor.commit();
    }

    public String getUserName() {
        return pref.getString("username", "UserName");
    }

    public void setUserName(String userName) {
        editor.putString("username", userName);
        editor.commit();

    }

    public String getUserEmail() {
        return pref.getString("useremail", "UserEmail");
    }

    public void setUserEmail(String userEmail) {
        editor.putString("useremail", userEmail);
        editor.commit();

    }

    public String getUserPassword() {
        return pref.getString("userPassword", "0");
    }

    public void setUserPassword(String UserPassword) {
        editor.putString("userPassword", UserPassword);
        editor.commit();
    }

    public String getAdsType() {
        return pref.getString("adsType", "adsType");
    }

    public void setAdsType(String adsType) {
        editor.putString("adsType", adsType);
        editor.commit();
    }

    public boolean getAdsShow() {
        return pref.getBoolean("showAd", false);
    }

    public void setAdsShow(boolean value) {
        editor.putBoolean("showAd", value);
        editor.commit();
    }

    public void setAdsPosition(String value) {
        editor.putString("adsPosition", value);
        editor.commit();
    }

    public String getAdsPostion() {
        return pref.getString("adsPosition", "adsPosition");
    }

    public String getAdsId() {
        return pref.getString("ad_id", "");
    }

    public void setAdsId(String value) {
        editor.putString("ad_id", value);
        editor.commit();
    }

    public int getAdsInitialTime() {
        return Integer.parseInt(pref.getString("time_initial", "30"));
    }

    public void setAdsInitialTime(String value) {
        editor.putString("time_initial", value);
    }

    public int getAdsDisplayTime() {
        return Integer.parseInt(pref.getString("time", "30"));
    }

    public void setAdsDisplayTime(String value) {
        editor.putString("time", value);
    }

    public boolean getAnalyticsShow() {
        return pref.getBoolean("AnalyticsShow", false);
    }

    public void setAnalyticsShow(boolean value) {
        editor.putBoolean("AnalyticsShow", value);
        editor.commit();
    }

    public String getAnalyticsId() {
        return pref.getString("analyticsId", "");
    }

    public void setAnalyticsId(String analyticsId) {
        editor.putString("analyticsId", analyticsId);
        editor.commit();
    }

    public void setGoogleButn(boolean value) {
        editor.putBoolean("googleButton", value);
        editor.commit();
    }

    public boolean getGooglButn() {
        return pref.getBoolean("googleButton", false);
    }

    public void setfbButn(boolean value) {
        editor.putBoolean("fbButton", value);
        editor.commit();
    }

    public boolean getfbButn() {
        return pref.getBoolean("fbButton", false);
    }

    public String getFireBaseId() {
        return pref.getString("firebaseid", "");
    }

    public void setFireBaseId(String value) {
        editor.putString("firebaseid", value);
        editor.commit();
    }

    public boolean getRTL() {
        return pref.getBoolean("RTL", false);
    }

    public void setRTL(boolean value) {
        editor.putBoolean("RTL", value);
        editor.commit();
    }

    public String getYoutubeApi() {
        return pref.getString("youTubeApi", "");
    }

    public void setYoutubeApi(String value) {
        editor.putString("youTubeApi", value);
        editor.commit();
    }

    public String getGenericAlertTitle() {
        return pref.getString("title", "Confirm!");
    }

    public void setGenericAlertTitle(String title) {
        editor.putString("title", title);
        editor.commit();
    }

    public String getGenericAlertMessage() {
        return pref.getString("text", "Are You Sure You Want To Do This!");
    }

    public void setGenericAlertMessage(String title) {
        editor.putString("text", title);
        editor.commit();
    }

    public String getGenericAlertOkText() {
        return pref.getString("btn_ok", "OK");
    }

    public void setGenericAlertOkText(String title) {
        editor.putString("btn_ok", title);
        editor.commit();
    }

    public String getGenericAlertCancelText() {
        return pref.getString("btn_no", "Cancel");
    }

    public void setGenericAlertCancelText(String title) {
        editor.putString("btn_no", title);
        editor.commit();
    }

    public void isAppOpen(boolean appOpen) {
        editor.putBoolean("app_open", appOpen);
        editor.commit();
    }

    public boolean getAppOpen() {
        return pref.getBoolean("app_open", false);
    }

    public void checkOpen(boolean appOpen) {
        editor.putBoolean("checkOpen", appOpen);
        editor.commit();
    }

    public String getGuestImage() {
        return pref.getString("guest_image", "");
    }

    public void setGuestImage(String message) {
        editor.putString("guest_image", message);
        editor.commit();
    }

    public boolean getCheckOpen() {
        return pref.getBoolean("checkOpen", false);
    }

    public String getNoLoginMessage() {
        return pref.getString("noLoginmessage", "Please login to perform this action.");
    }

    public void setNoLoginMessage(String message) {
        editor.putString("noLoginmessage", message);
        editor.commit();
    }

    public boolean isFeaturedScrollEnable() {
        return pref.getBoolean("featured_scroll_enabled", false);
    }

    public void setFeaturedScrollEnable(boolean featuredScrollEnable) {
        editor.putBoolean("featured_scroll_enabled", featuredScrollEnable);
        editor.commit();
    }

    public int getFeaturedScroolDuration() {
        return pref.getInt("featured_duration", 40);

    }

    public void setFeaturedScroolDuration(int duration) {
        editor.putInt("featured_duration", duration);
        editor.commit();
    }

    public int getFeaturedScroolLoop() {
        return pref.getInt("featured_loop", 40);

    }

    public void setFeaturedScroolLoop(int duration) {
        editor.putInt("featured_loop", duration);
        editor.commit();
    }

    public String getAppLogo() {
        return pref.getString("appLogo", "");

    }

    public void setAppLogo(String appLogo) {
        editor.putString("appLogo", appLogo);
        editor.commit();
    }

    public void setPaymentCompletedMessage(String paymentCompletedMessage) {
        editor.putString("message", paymentCompletedMessage);
        editor.commit();
    }

    public String getpaymentCompletedMessage() {
        return pref.getString("message", "Order Places Succ");
    }


    //region LocationPopup

    public int getLocationSliderNumber() {
        return pref.getInt("locationSliderNumber", 250);

    }

    public void setLocationSliderNumber(int locationSliderNumber) {
        editor.putInt("locationSliderNumber", locationSliderNumber);
        editor.commit();
    }

    public int getLocationSliderStep() {
        return pref.getInt("locationSliderStep", 5);

    }

    public void setLocationSliderStep(int locationSliderStep) {
        editor.putInt("locationSliderStep", locationSliderStep);
        editor.commit();
    }

    public String getLocationText() {
        return pref.getString("locationText", "Select distance in (KM)");
    }

    public void setLocationText(String locationText) {
        editor.putString("locationText", locationText);
        editor.commit();
    }

    public String getLocationBtnSubmit() {
        return pref.getString("locationBtnSubmit", "");
    }

    public void setLocationBtnSubmit(String locationBtnSubmit) {
        editor.putString("locationBtnSubmit", locationBtnSubmit);
        editor.commit();
    }

    public String getLocationBtnClear() {
        return pref.getString("locationBtnClear", "");
    }

    public void setLocationBtnClear(String locationBtnClear) {
        editor.putString("locationBtnClear", locationBtnClear);
        editor.commit();
    }

    //endregion

    //region GPSPopup

    public String getGpsTitle() {
        return pref.getString("gpsTitle", "GPS Settings");
    }

    public void setGpsTitle(String gpsTitle) {
        editor.putString("gpsTitle", gpsTitle);
        editor.commit();
    }

    public String getGpsText() {
        return pref.getString("gpsText", "GPS is not enabled. Do you want to go to settings menu?");
    }

    public void setGpsText(String gpsText) {
        editor.putString("gpsText", gpsText);
        editor.commit();
    }

    public String getGpsConfirm() {
        return pref.getString("gpsConfirm", "Settings");
    }

    public void setGpsConfirm(String gpsConfirm) {
        editor.putString("gpsConfirm", gpsConfirm);
        editor.commit();
    }

    public String getGpsCancel() {
        return pref.getString("gpsCancel", "Clear");
    }

    public void setGpsCancel(String gpsCancel) {
        editor.putString("gpsCancel", gpsCancel);
        editor.commit();
    }

    public void setShowNearby(boolean b) {
        editor.putBoolean("show_nearby", b);
        editor.commit();
    }

    public boolean getShowNearBy() {
        return pref.getBoolean("show_nearby", false);
    }

    public boolean getAdsPositionSorter() {
        return pref.getBoolean("ads_position_sorter", false);
    }

    public void setAdsPositionSorter(boolean b) {
        editor.putBoolean("ads_position_sorter", b);
        editor.commit();
    }

    public String getLatitude() {
        return pref.getString("nearby_latitude", "");
    }

    public void setLatitude(String latitude) {
        editor.putString("nearby_latitude", latitude);
        editor.commit();
    }

    public String getLongitude() {
        return pref.getString("nearby_longitude", "");
    }

    public void setLongitude(String longitude) {
        editor.putString("nearby_longitude", longitude);
        editor.commit();
    }

    public String getDistance() {
        return pref.getString("nearby_distance", "");
    }

    public void setDistance(String longitude) {
        editor.putString("nearby_distance", longitude);
        editor.commit();
    }

    //endregion

    public String getNotificationTitle() {
        return pref.getString("notificationTitle", "");
    }

    public void setNotificationTitle(String notificationTitle) {
        editor.putString("notificationTitle", notificationTitle);
        editor.commit();
    }

    public String getNotificationMessage() {
        return pref.getString("notificationMessage", "");
    }

    public void setNotificationMessage(String notificationMessage) {
        editor.putString("notificationMessage", notificationMessage);
        editor.commit();
    }

    public String getNotificationImage() {
        return pref.getString("notificationImage", "");
    }

    public void setNotificationImage(String notificationImage) {
        editor.putString("notificationImage", notificationImage);
        editor.commit();
    }

    public String getNotificationTime() {
        return pref.getString("notificatioTime", "");
    }

    public void setNotificationTime(String notificatioTime) {
        editor.putString("notificatioTime", notificatioTime);
        editor.commit();
    }
}