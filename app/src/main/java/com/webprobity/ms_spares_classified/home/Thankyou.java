package com.webprobity.ms_spares_classified.home;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.adapters.ItemBlogAdapter;
import com.webprobity.ms_spares_classified.blog.BlogDetailFragment;
import com.webprobity.ms_spares_classified.helper.BlogItemOnclicklinstener;
import com.webprobity.ms_spares_classified.modelsList.blogModel;
import com.webprobity.ms_spares_classified.utills.CustomBorderDrawable;
import com.webprobity.ms_spares_classified.utills.Network.RestService;
import com.webprobity.ms_spares_classified.utills.SettingsMain;
import com.webprobity.ms_spares_classified.utills.UrlController;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Thankyou extends AppCompatActivity implements View.OnClickListener {
    ImageView imageView, logoThankyou;
    Button contineBuyPackage;
    SettingsMain settingsMain;
    Activity activity;
    RestService restService;
    WebView webView;
    TextView tv_thankyou_title;
    String webViewData, titleData, buttonData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.setContentView(R.layout.activity_thankyou);
        settingsMain = new SettingsMain(this);
        activity = Thankyou.this;

        webViewData = getIntent().getStringExtra("data");
        titleData = getIntent().getStringExtra("order_thankyou_title");
        buttonData = getIntent().getStringExtra("order_thankyou_btn");

        imageView = (ImageView) findViewById(R.id.closeIcon);
        logoThankyou = (ImageView) findViewById(R.id.logoThankyou);
        webView = (WebView) findViewById(R.id.webViewThankyou);
        tv_thankyou_title = (TextView) findViewById(R.id.thankyourText);
        contineBuyPackage = (Button) findViewById(R.id.contineBuyPackage);

        imageView.setOnClickListener(this);
        contineBuyPackage.setOnClickListener(this);

        restService = UrlController.createService(RestService.class, settingsMain.getUserEmail(), settingsMain.getUserPassword(), activity);

        contineBuyPackage.setTextColor(Color.parseColor(settingsMain.getMainColor()));
        tv_thankyou_title.setTextColor(Color.parseColor(settingsMain.getMainColor()));
        contineBuyPackage.setBackground(CustomBorderDrawable.customButton(3, 3, 3, 3, settingsMain.getMainColor(), "#00000000", settingsMain.getMainColor(), 2));

        adforest_loadData();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeIcon:
                finish();
                break;
            case R.id.contineBuyPackage:
                finish();
                break;
        }
    }

    public void adforest_loadData() {

        Picasso.with(activity).load(settingsMain.getAppLogo())
                .error(R.drawable.logo)
                .placeholder(R.drawable.logo)
                .fit().centerInside()
                .into(logoThankyou);
        webView.setScrollContainer(false);
        webView.loadDataWithBaseURL(null, webViewData, "text/html", "UTF-8", null);
        tv_thankyou_title.setText(titleData);
        contineBuyPackage.setText(buttonData);

        Toast.makeText(Thankyou.this, settingsMain.getpaymentCompletedMessage(), Toast.LENGTH_SHORT).show();
    }

}
