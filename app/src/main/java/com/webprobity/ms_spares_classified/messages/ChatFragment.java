package com.webprobity.ms_spares_classified.messages;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;
import com.webprobity.ms_spares_classified.Notification.Config;
import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.adapters.ChatAdapter;
import com.webprobity.ms_spares_classified.modelsList.ChatMessage;
import com.webprobity.ms_spares_classified.utills.AnalyticsTrackers;
import com.webprobity.ms_spares_classified.utills.Network.RestService;
import com.webprobity.ms_spares_classified.utills.SettingsMain;
import com.webprobity.ms_spares_classified.utills.UrlController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment implements View.OnClickListener {

    private EditText msg_edittext;
    ArrayList<ChatMessage> chatlist;

    ChatAdapter chatAdapter;
    ListView msgListView;
    int nextPage = 1;
    boolean hasNextPage = false;
    String adId, senderId, recieverId, type;
    SettingsMain settingsMain;

    TextView adName, adPrice, adDate;
    SwipeRefreshLayout swipeRefreshLayout;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    RestService restService;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat_layout, container, false);
        settingsMain = new SettingsMain(getActivity());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            adId = bundle.getString("adId", "0");
            senderId = bundle.getString("senderId", "0");
            recieverId = bundle.getString("recieverId", "0");
            type = bundle.getString("type", "0");
        }

        adDate = view.findViewById(R.id.verified);
        adName = view.findViewById(R.id.loginTime);
        adPrice = view.findViewById(R.id.text_viewName);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        msg_edittext = view.findViewById(R.id.messageEditText);
        msgListView = view.findViewById(R.id.msgListView);
        ImageButton sendButton = view
                .findViewById(R.id.sendMessageButton);
        sendButton.setOnClickListener(this);

        sendButton.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));
        // ----Set autoscroll of listview when a new message arrives----//
        msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        msgListView.setStackFromBottom(true);
        restService = UrlController.createService(RestService.class, settingsMain.getUserEmail(), settingsMain.getUserPassword(), getActivity());
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (hasNextPage) {
                    swipeRefreshLayout.setRefreshing(true);
                    msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
                    msgListView.setStackFromBottom(false);
                    adforest_loadMore(nextPage);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        chatlist = new ArrayList<>();
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    Toast.makeText(getActivity(),"there",Toast.LENGTH_SHORT).show();
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);


                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {

                    String date = intent.getStringExtra("date");
                    String img = intent.getStringExtra("img");
                    String text = intent.getStringExtra("text");
                    String type = intent.getStringExtra("type");

                    String adIdCheck = intent.getStringExtra("adIdCheck");
                    String recieverIdCheck = intent.getStringExtra("recieverIdCheck");
                    String senderIdCheck = intent.getStringExtra("senderIdCheck");

                    if (adId.equals(adIdCheck) && recieverId.equals(recieverIdCheck)
                            && senderId.equals(senderIdCheck)) {
                        Log.d("Instant Message", "true");
                        ChatMessage item = new ChatMessage();
                        item.setImage(img);
                        item.setBody(text);
                        item.setDate(date);
                        item.setMine(type.equals("message"));
                        chatlist.add(item);
                        msgListView.setAdapter(chatAdapter);
                        chatAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("Instant Message", adIdCheck + recieverIdCheck + senderIdCheck);
                        Log.d("Instant Message", adId + senderId + recieverId);

                    }
                }
            }
        };

        adforest_getChat();
        return view;
    }

    private void adforest_getChat() {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("ad_id", adId);
            params.addProperty("sender_id", senderId);
            params.addProperty("receiver_id", recieverId);
            params.addProperty("type", type);

            Log.d("info sendChat object", "" + params.toString());

            Call<ResponseBody> myCall = restService.postGetChatORLoadMore(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info Chat Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info Chat object", "" + response.getJSONObject("data"));

                                getActivity().setTitle(response.getJSONObject("data").getString("page_title"));

                                JSONObject jsonObjectPagination = response.getJSONObject("data").getJSONObject("pagination");

                                adName.setText(response.getJSONObject("data").getString("ad_title"));
                                adPrice.setText(response.getJSONObject("data").getJSONObject("ad_price").getString("price"));
                                adDate.setText(response.getJSONObject("data").getString("ad_date"));

                                nextPage = jsonObjectPagination.getInt("next_page");
                                hasNextPage = jsonObjectPagination.getBoolean("has_next_page");

                                adforest_intList(response.getJSONObject("data").getJSONArray("chat"));

                                chatAdapter = new ChatAdapter(getActivity(), chatlist);
                                msgListView.setAdapter(chatAdapter);

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
                        Log.d("info Chat Exception ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info Chat error", String.valueOf(t));
                        Log.d("info Chat error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void adforest_loadMore(int nextPag) {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            JsonObject params = new JsonObject();
            params.addProperty("ad_id", adId);
            params.addProperty("sender_id", senderId);
            params.addProperty("receiver_id", recieverId);
            params.addProperty("type", type);

            params.addProperty("page_number", nextPag);

            Log.d("info LoadMore Chat", "" + params.toString());

            Call<ResponseBody> myCall = restService.postGetChatORLoadMore(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info LoadChat Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info LoadChat object", "" + response.getJSONObject("data"));

                                JSONObject jsonObjectPagination = response.getJSONObject("data").getJSONObject("pagination");

                                nextPage = jsonObjectPagination.getInt("next_page");
                                hasNextPage = jsonObjectPagination.getBoolean("has_next_page");

                                JSONArray jsonArray = (response.getJSONObject("data").getJSONArray("chat"));

                                Collections.reverse(chatlist);

                                try {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        ChatMessage item = new ChatMessage();
                                        item.setImage(jsonArray.getJSONObject(i).getString("img"));
                                        item.setBody(jsonArray.getJSONObject(i).getString("text"));
                                        item.setDate(jsonArray.getJSONObject(i).getString("date"));
                                        item.setMine(jsonArray.getJSONObject(i).getString("type").equals("message"));

                                        chatlist.add(item);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Collections.reverse(chatlist);
                                msgListView.setAdapter(chatAdapter);
                                chatAdapter.notifyDataSetChanged();

                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                    } catch (JSONException e) {
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                        e.printStackTrace();
                    } catch (IOException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    SettingsMain.hideDilog();
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info LoadChat Excptn ", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info LoadChat error", String.valueOf(t));
                        Log.d("info LoadChat error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        } else {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }


    }

    private void adforest_intList(JSONArray jsonArray) {
        chatlist.clear();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                ChatMessage item = new ChatMessage();
                item.setBody(jsonArray.getJSONObject(i).getString("text"));
                item.setImage(jsonArray.getJSONObject(i).getString("img"));
                item.setDate(jsonArray.getJSONObject(i).getString("date"));
                item.setMine(jsonArray.getJSONObject(i).getString("type").equals("message"));

                chatlist.add(item);
            }
            Collections.reverse(chatlist);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    public void adforest_sendTextMessage() {

        msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        msgListView.setStackFromBottom(true);

        String message = msg_edittext.getEditableText().toString();
        if (!message.equalsIgnoreCase("")) {

            if (SettingsMain.isConnectingToInternet(getActivity())) {

                SettingsMain.showDilog(getActivity());

                JsonObject params = new JsonObject();
                params.addProperty("ad_id", adId);
                params.addProperty("sender_id", senderId);
                params.addProperty("receiver_id", recieverId);
                params.addProperty("type", type);
                params.addProperty("message", message);


                Log.d("info sendMessage Object", "" + params.toString());

                Call<ResponseBody> myCall = restService.postSendMessage(params, UrlController.AddHeaders(getActivity()));
                myCall.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                        try {
                            if (responseObj.isSuccessful()) {
                                Log.d("info sendMessage Resp", "" + responseObj.toString());

                                JSONObject response = new JSONObject(responseObj.body().string());
                                if (response.getBoolean("success")) {
                                    Log.d("info sendMessage object", "" + response.getJSONObject("data"));

                                    adforest_intList(response.getJSONObject("data").getJSONArray("chat"));

                                    chatAdapter = new ChatAdapter(getActivity(), chatlist);
                                    msgListView.setAdapter(chatAdapter);

                                    msg_edittext.setText("");
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
                            SettingsMain.hideDilog();
                        }
                        if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                            Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                            SettingsMain.hideDilog();
                        }
                        if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                            Log.d("info sendMessage", "NullPointert Exception" + t.getLocalizedMessage());
                            SettingsMain.hideDilog();
                        } else {
                            SettingsMain.hideDilog();
                            Log.d("info sendMessage error", String.valueOf(t));
                            Log.d("info sendMessage error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                        }
                    }
                });
            } else {
                SettingsMain.hideDilog();
                Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
            }


        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendMessageButton:
                adforest_sendTextMessage();
        }
    }

    @Override
    public void onResume() {
        if (settingsMain.getAnalyticsShow() && !settingsMain.getAnalyticsId().equals(""))
            AnalyticsTrackers.getInstance().trackScreenView("Chat Box");
        super.onResume();
        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRegistrationBroadcastReceiver);

        super.onPause();
    }
}
