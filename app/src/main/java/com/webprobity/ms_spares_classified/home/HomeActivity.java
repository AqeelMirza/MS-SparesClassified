package com.webprobity.ms_spares_classified.home;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;
import com.webprobity.ms_spares_classified.Notification.Config;
import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.blog.BlogFragment;
import com.webprobity.ms_spares_classified.messages.Message;
import com.webprobity.ms_spares_classified.profile.FragmentProfile;
import com.webprobity.ms_spares_classified.profile.MyAds;
import com.webprobity.ms_spares_classified.profile.MyAds_Favourite;
import com.webprobity.ms_spares_classified.profile.MyAds_Featured;
import com.webprobity.ms_spares_classified.profile.MyAds_Inactive;
import com.webprobity.ms_spares_classified.signinorup.MainActivity;
import com.webprobity.ms_spares_classified.utills.Admob;
import com.webprobity.ms_spares_classified.utills.GPSTracker;
import com.webprobity.ms_spares_classified.utills.Network.RestService;
import com.webprobity.ms_spares_classified.utills.SettingsMain;
import com.webprobity.ms_spares_classified.utills.CircleTransform;
import com.webprobity.ms_spares_classified.utills.UrlController;
import com.squareup.picasso.Picasso;
import com.xw.repo.BubbleSeekBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    SettingsMain settingsMain;
    ImageView imageViewProfile;
    String[] permissionsRequired = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,};
    UpdateFragment updatfrag;
    TextView textViewUserName;
    RestService restService;
    FragmentHome fragmentHome;
    GPSTracker gps;
    double latitude, longitude;
    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private EditText edtSeach;
    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false, isAdOpen = true;

    public void updateApi(UpdateFragment listener) {
        updatfrag = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        System.gc();
        settingsMain = new SettingsMain(this);

        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);

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
                Intent intent = new Intent(HomeActivity.this, AddNewAdPost.class);
                startActivity(intent);
            }
        });

        toolbar.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //noinspection deprecation
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        if (settingsMain.getAppOpen()) {
            restService = UrlController.createService(RestService.class);
            fab.setVisibility(View.GONE);
        } else
            restService = UrlController.createService(RestService.class, settingsMain.getUserEmail(), settingsMain.getUserPassword(), this);

        if (header != null) {
            TextView textViewUserEmail = header.findViewById(R.id.textView);
            textViewUserName = header.findViewById(R.id.username);
            imageViewProfile = header.findViewById(R.id.imageView);

            int[] colors = {Color.parseColor(settingsMain.getMainColor()), Color.parseColor(settingsMain.getMainColor())};
            //create a new gradient color
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT, colors);
            gd.setCornerRadius(0f);

            header.setBackground(gd);

            if (!TextUtils.isEmpty(settingsMain.getUserEmail())) {
                textViewUserEmail.setText(settingsMain.getUserEmail());
            }
            if (!TextUtils.isEmpty(settingsMain.getUserName())) {
                textViewUserName.setText(settingsMain.getUserName());
            }
            if (settingsMain.getAppOpen()) {
                if (!TextUtils.isEmpty(settingsMain.getGuestImage())) {
                    Picasso.with(HomeActivity.this).load(settingsMain.getGuestImage())
                            .transform(new CircleTransform())
                            .error(R.drawable.placeholder)
                            .placeholder(R.drawable.placeholder)
                            .into(imageViewProfile);
                }
            } else {
                if (!TextUtils.isEmpty(settingsMain.getUserImage())) {
                    Picasso.with(HomeActivity.this).load(settingsMain.getUserImage())
                            .transform(new CircleTransform())
                            .error(R.drawable.placeholder)
                            .placeholder(R.drawable.placeholder)
                            .into(imageViewProfile);
                }
            }
        }
        fragmentHome = new FragmentHome();
        chkPermissson();
        gps = new GPSTracker(HomeActivity.this);
        if (settingsMain.getShowNearBy() && settingsMain.getAdsPositionSorter()
                && !settingsMain.getUserLogin().equals("0")) {
            if (gps.canGetLocation()) {
            } else {
                gps.showSettingsAlert();
            }
        }
        if (!settingsMain.getNotificationTitle().equals("")) {
            String title, message, image;
            title = settingsMain.getNotificationTitle();
            message = settingsMain.getNotificationMessage();
            image = settingsMain.getNotificationImage();

            adforest_showNotificationDialog(title, message, image);
        }
    }

    private void adforest_showNotificationDialog(String title, String message, String image) {

        final Dialog dialog;
        dialog = new Dialog(HomeActivity.this, R.style.customDialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_notification_layout);
        ImageView imageView = dialog.findViewById(R.id.notificationImage);
        TextView tv_title = dialog.findViewById(R.id.notificationTitle);
        TextView tV_message = dialog.findViewById(R.id.notificationMessage);
        Button button = dialog.findViewById(R.id.cancel_button);
        button.setText(settingsMain.getGenericAlertCancelText());
        button.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.parseColor("#00000000")));


        if (!TextUtils.isEmpty(image)) {
            Picasso.with(HomeActivity.this).load(image)
                    .fit()
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(imageView);
        }

        tv_title.setText(title);
        tV_message.setText(message);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public void adforest_InterstitalAds() {
        if (settingsMain.getAdsShow() && !settingsMain.getAdsId().equals("")
                && settingsMain.getAdsDisplayTime() != 0 && settingsMain.getAdsInitialTime() != 0) {
            if (isAdOpen) {
                Admob.loadInterstitial(this);
            }
        }
    }

    public void adforest_bannersAds() {
        if (settingsMain.getAdsShow() && !settingsMain.getAdsId().equals("")) {
            if (settingsMain.getAdsPostion().equals("top")) {
                LinearLayout frameLayout = (LinearLayout) findViewById(R.id.adView);
                Admob.adforest_Displaybanners(this, frameLayout);
            } else {
                LinearLayout frameLayout = (LinearLayout) findViewById(R.id.adViewBelow);
                FrameLayout maimFrame = (FrameLayout) findViewById(R.id.frameContainer);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.bottomMargin = 80;
                maimFrame.setLayoutParams(layoutParams);
                Admob.adforest_Displaybanners(this, frameLayout);
            }
        }
    }

    public void changeImage() {
        if (!TextUtils.isEmpty(settingsMain.getUserImage())) {
            Picasso.with(HomeActivity.this).load(settingsMain.getUserImage())
                    .transform(new CircleTransform())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(imageViewProfile);
        }
        textViewUserName.setText(settingsMain.getUserName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        MenuItem searchViewItem = menu.findItem(R.id.action_search);

        MenuItem action_location = menu.findItem(R.id.action_location);
        if (settingsMain.getShowNearBy() && settingsMain.getAdsPositionSorter() && !settingsMain.getUserLogin().equals("0")) {
            action_location.setVisible(true);
        } else
            action_location.setVisible(false);
        action_location.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                gps = new GPSTracker(HomeActivity.this);
                if (gps.canGetLocation() && gps.isCheckPermission()) {
                    final Dialog dialog = new Dialog(HomeActivity.this, R.style.customDialog);

                    dialog.setCanceledOnTouchOutside(true);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_location_seekbar);

                    final BubbleSeekBar bubbleSeekBar = dialog.findViewById(R.id.seakBar);


                    bubbleSeekBar.getConfigBuilder()
                            .max(settingsMain.getLocationSliderNumber())
                            .sectionCount(settingsMain.getLocationSliderStep())
                            .secondTrackColor(Color.parseColor(settingsMain.getMainColor()))
                            .build();

                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.parseColor("#00000000")));
                    Button Send = dialog.findViewById(R.id.send_button);
                    Button Cancel = dialog.findViewById(R.id.cancel_button);
                    TextView locationText = dialog.findViewById(R.id.locationText);

                    Send.setText(settingsMain.getLocationBtnSubmit());
                    Cancel.setText(settingsMain.getLocationBtnClear());
                    locationText.setText(settingsMain.getLocationText());
                    Send.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));
                    Cancel.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));

                    Send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            latitude = gps.getLatitude();
                            longitude = gps.getLongitude();
                            adforest_changeNearByStatus(Double.toString(latitude), Double.toString(longitude),
                                    Integer.toString(bubbleSeekBar.getProgress()));
                            dialog.dismiss();
                        }
                    });
                    Cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            adforest_changeNearByStatus("", ""
                                    , Integer.toString(bubbleSeekBar.getProgress()));
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
                if (!gps.canGetLocation())
                    gps.showSettingsAlert();


                return true;
            }
        });

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();

                if (!query.equals("")) {

                    FragmentManager fm = getSupportFragmentManager();
                    Fragment fragment = fm.findFragmentByTag("FragmentCatSubNSearch");
                    Fragment fragment2 = fm.findFragmentById(R.id.frameContainer);

                    FragmentCatSubNSearch fragment_search = new FragmentCatSubNSearch();
                    Bundle bundle = new Bundle();
                    bundle.putString("id", "");
                    bundle.putString("title", query);

                    fragment_search.setArguments(bundle);

                    if (fragment != fragment2) {
                        replaceFragment(fragment_search, "FragmentCatSubNSearch");
                        return true;
                    } else {
                        updatfrag.updatefrag(query);
                        return true;
                    }
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.left_enter, R.anim.right_out);
        }
//        if (isAdOpen) {
//            isAdOpen = false;
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Admob.adforest_cancelInterstitial();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        item.setChecked(true);

        if (id == R.id.nav_blog) {
            replaceFragment(new BlogFragment(), "BlogFragment");
        }
        if (id == R.id.nav_msspares_link) {
            // replaceFragment(new BlogFragment(), "BlogFragment");
            openApp(this, "itp.com.magikdigi");
        }
        if (id == R.id.search) {

            Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
            intent.putExtra("id", "");
            startActivity(intent);
            overridePendingTransition(R.anim.right_enter, R.anim.left_out);

        } else if (id == R.id.profile) {
            replaceFragment(new FragmentProfile(), "FragmentProfile");
        } else if (id == R.id.myAds) {
            if (settingsMain.getAppOpen()) {
                settingsMain.setUserLogin("0");
                settingsMain.setFireBaseId("");
                HomeActivity.this.finish();
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
            } else
                replaceFragment(new MyAds(), "MyAds");
        } else if (id == R.id.favAds) {
            replaceFragment(new MyAds_Favourite(), "MyAds_Favourite");
        } else if (id == R.id.packages) {
            replaceFragment(new PackagesFragment(), "PackagesFragment");
        } else if (id == R.id.home) {
            FragmentManager fm = HomeActivity.this.getSupportFragmentManager();
            for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
            }
        } else if (id == R.id.inActiveAds) {
            if (settingsMain.getAppOpen()) {
                settingsMain.setUserLogin("0");
                settingsMain.setFireBaseId("");
                HomeActivity.this.finish();
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.putExtra("page", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
            } else
                replaceFragment(new MyAds_Inactive(), "MyAds_Inactive");
        } else if (id == R.id.featureAds) {
            replaceFragment(new MyAds_Featured(), "MyAds_Featured");
        } else if (id == R.id.nav_log_out) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(Config.TOPIC_GLOBAL);

            AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
            alert.setTitle(settingsMain.getAlertDialogTitle("info"));
            alert.setCancelable(false);
            alert.setMessage(settingsMain.getAlertDialogMessage("confirmMessage"));
            alert.setPositiveButton(settingsMain.getAlertOkText(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,
                                    int which) {
                    settingsMain.setUserLogin("0");
                    settingsMain.setFireBaseId("");
                    HomeActivity.this.finish();
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                    adforest_AddFirebaseid();
                    if (settingsMain.getCheckOpen()) {
                        settingsMain.isAppOpen(true);
                    }
                    dialog.dismiss();
                }
            });
            alert.setNegativeButton(settingsMain.getAlertCancelText(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alert.show();
        } else if (id == R.id.message) {
            Intent intent = new Intent(HomeActivity.this, Message.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_enter, R.anim.left_out);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    /**
     * Open another app.
     *
     * @param context     current Context, like Activity, App, or Service
     * @param packageName the full package name of the app to open
     * @return true if likely successful, false if unsuccessful
     */
    public void openApp(Context context, String packageName) {
       /* PackageManager manager = context.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
                //throw new ActivityNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
*/

        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void replaceFragment(Fragment someFragment, String tag) {

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(tag);
        Fragment fragment2 = fm.findFragmentById(R.id.frameContainer);

        if (fragment != fragment2) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
            transaction.replace(R.id.frameContainer, someFragment, tag);
            transaction.addToBackStack(tag);
            transaction.commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if (allgranted) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        proceedAfterPermission();
                    }
                }, 500);
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permissionsRequired[3])
                    || ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permissionsRequired[4])) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Need Multiple Permissions");
                //builder.setMessage("This app needs Camera and Location permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(HomeActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        HomeActivity.this.finish();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(HomeActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceedAfterPermission();
            }
        }
    }

    public void AdsNDAnalytics() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("AdsType", settingsMain.getAdsType());
                if (settingsMain.getAdsType().equals("interstital")) {
                    adforest_InterstitalAds();
                } else {
                    adforest_bannersAds();
                }

            }
        }, 1500);
    }

    private void proceedAfterPermission() {
        AdsNDAnalytics();
        startFragment(fragmentHome, "FragmentHome");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(HomeActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceedAfterPermission();
            }
        }
    }


    private void chkPermissson() {

        if (ActivityCompat.checkSelfPermission(HomeActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(HomeActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(HomeActivity.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(HomeActivity.this, permissionsRequired[3]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(HomeActivity.this, permissionsRequired[4]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permissionsRequired[3])
                    || ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, permissionsRequired[4])) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Need Multiple Permissions");
                //builder.setMessage("This app needs Camera and Location permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(HomeActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HomeActivity.this.finish();
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Need Multiple Permissions");
                //builder.setMessage("This app needs Camera and Location permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant Camera and Location", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        HomeActivity.this.finish();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(HomeActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.apply();
        } else {
            //You already have the permission, just go ahead.
            proceedAfterPermission();
        }
    }

    private void adforest_AddFirebaseid() {
        if (SettingsMain.isConnectingToInternet(this)) {


            JsonObject params = new JsonObject();


            params.addProperty("firebase_id", "");

            Call<ResponseBody> myCall = restService.postFirebaseId(params, UrlController.AddHeaders(this));
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
                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void adforest_changeNearByStatus(final String nearby_latitude, final String nearby_longitude, final String nearby_distance) {
        if (SettingsMain.isConnectingToInternet(this)) {


            JsonObject params = new JsonObject();
            params.addProperty("nearby_latitude", nearby_latitude);
            params.addProperty("nearby_longitude", nearby_longitude);
            params.addProperty("nearby_distance", nearby_distance);
            Log.d("info SendNearBy Status", params.toString());

            SettingsMain.showDilog(HomeActivity.this);
            Call<ResponseBody> myCall = restService.postChangeNearByStatus(params, UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info NearBy Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                SettingsMain.hideDilog();
                                FragmentManager fm = getSupportFragmentManager();
                                Fragment fragment = fm.findFragmentByTag("FragmentCatSubNSearch");
                                Fragment fragment2 = fm.findFragmentById(R.id.frameContainer);

                                FragmentCatSubNSearch fragment_search = new FragmentCatSubNSearch();
                                Bundle bundle = new Bundle();
                                bundle.putString("nearby_latitude", nearby_latitude);
                                bundle.putString("nearby_longitude", nearby_longitude);
                                bundle.putString("nearby_distance", nearby_distance);

                                settingsMain.setLatitude(nearby_latitude);
                                settingsMain.setLongitude(nearby_longitude);
                                settingsMain.setDistance(nearby_distance);

                                fragment_search.setArguments(bundle);

                                if (fragment != fragment2) {
                                    replaceFragment(fragment_search, "FragmentCatSubNSearch");
                                } else {
                                    updatfrag.updatefrag(nearby_latitude, nearby_longitude, nearby_distance);
                                }

                            } else
                                Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info FireBase ", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info FireBase err", String.valueOf(t));
                        Log.d("info FireBase err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            Toast.makeText(this, "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    public interface UpdateFragment {
        void updatefrag(String s);

        void updatefrag(String latitude, String longitude, String distance);
    }


}






