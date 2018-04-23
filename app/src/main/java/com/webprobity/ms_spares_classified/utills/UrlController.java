package com.webprobity.ms_spares_classified.utills;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;
import com.webprobity.ms_spares_classified.utills.Network.AuthenticationInterceptor;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.entity.StringEntity;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UrlController {
    private static String Purchase_code = "e7c217b4-5ac8-45e9-90be-6c4f5678868e";
    //e7c217b4-5ac8-45e9-90be-6c4f5678868e
    private static String Custom_Security = "spares_code";
   // private static String IP_ADDRESS = "http://tr.webprobity.com/";
    private static String IP_ADDRESS = "http://sell.ms-spares.in/";
    private static String Base_URL  =    IP_ADDRESS+ "wp-json/adforest/v1/";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(Base_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = builder.build();

    public static <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }

    public static <S> S createService(
            Class<S> serviceClass, String username, String password, Context context) {
        if (!TextUtils.isEmpty(username)
                && !TextUtils.isEmpty(password)) {
            String authToken = Credentials.basic(username, password);
            return createService(serviceClass, authToken, context);
        }
        return createService(serviceClass, null, null, context);
    }

    public static <S> S createService(
            Class<S> serviceClass, final String authToken, Context context) {
        if (!TextUtils.isEmpty(authToken)) {
            AuthenticationInterceptor interceptor =
                    new AuthenticationInterceptor(authToken, context);

            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor);

                builder.client(httpClient.build());
                retrofit = builder.build();
            }
        }
        return retrofit.create(serviceClass);
    }

    public static Map<String, String> AddHeaders(Context context) {
        Map<String, String> map = new HashMap<>();
        if (SettingsMain.isSocial(context)) {
            map.put("AdForest-Login-Type", "social");
        }
        map.put("Purchase-Code", Purchase_code);
        map.put("custom-security", Custom_Security);
        map.put("Content-Type", "application/json");
        map.put("Cache-Control", "max-age=640000");
        Log.d("Headers", map.toString());
        return map;
    }

    public static Map<String, String> UploadImageAddHeaders(Context context) {
        Map<String, String> map = new HashMap<>();
        if (SettingsMain.isSocial(context)) {
            map.put("AdForest-Login-Type", "social");
        }
        map.put("Purchase-Code", Purchase_code);
        map.put("custom-security", Custom_Security);
        map.put("Cache-Control", "max-age=640000");
        Log.d("Headers", map.toString());
        return map;
    }
}
