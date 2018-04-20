package com.webprobity.ms_spares_classified.profile;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.adapters.PlaceArrayAdapter;
import com.webprobity.ms_spares_classified.adapters.SpinnerAndListAdapter;
import com.webprobity.ms_spares_classified.home.HomeActivity;
import com.webprobity.ms_spares_classified.modelsList.subcatDiloglist;
import com.webprobity.ms_spares_classified.public_profile.social_icons;
import com.webprobity.ms_spares_classified.utills.AnalyticsTrackers;
import com.webprobity.ms_spares_classified.utills.CustomBorderDrawable;
import com.webprobity.ms_spares_classified.utills.Network.RestService;
import com.webprobity.ms_spares_classified.utills.SettingsMain;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.webprobity.ms_spares_classified.utills.UrlController;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.google.android.gms.location.places.AutocompleteFilter.TYPE_FILTER_ADDRESS;
import static com.google.android.gms.location.places.AutocompleteFilter.TYPE_FILTER_REGIONS;

public class EditProfile extends Fragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;

    SettingsMain settingsMain;
    TextView verifyBtn, textViewRateNo, textViewUserName, textViewLastLogin;
    TextView textViewAdsSold, textViewTotalList, textViewInactiveAds;

    TextView textViewName, textViewPhoneNumber, textViewLocation, textViewMainTitle, textViewImage,
            textViewAccType, textViewIntroduction;
    TextView btnCancel, btnSend, btnChangePwd;

    ImageView btnSeletImage;
    EditText editTextName, editTextPhone, editTextIntroduction;
    List<View> allViewInstanceforCustom = new ArrayList<>();
    List<View> fieldsValidationforCustom = new ArrayList<>();

    AutoCompleteTextView autoCompleteTextViewLocation;

    RatingBar ratingBar;
    ImageView imageViewProfile;
    JSONObject jsonObjectforCustom, extraText;
    Spinner spinnerACCType;
    LinearLayout viewSocialIconsLayout;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String userChoosenTask;
    private JSONObject jsonObject;
    private JSONObject jsonObjectChnge, jsonObject_select_pic;
    RestService restService;
    boolean checkValidation = true;
    LinearLayout publicProfileCustomIcons;

    public EditProfile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        settingsMain = new SettingsMain(getActivity());

        publicProfileCustomIcons = view.findViewById(R.id.publicProfileCustomIcons);

        publicProfileCustomIcons.setVisibility(View.GONE);
//        publicProfileCustomIcons.setBackgroundResource(R.drawable.socialicons);

        textViewName = view.findViewById(R.id.textViewName);
        textViewPhoneNumber = view.findViewById(R.id.textViewPhone);
        textViewLocation = view.findViewById(R.id.textViewLocation);
        textViewAccType = view.findViewById(R.id.textViewAccount_type);
        textViewMainTitle = view.findViewById(R.id.textView);
        textViewIntroduction = view.findViewById(R.id.textViewIntroduction);
        textViewImage = view.findViewById(R.id.textViewSetImage);
        btnCancel = view.findViewById(R.id.textViewCancel);
        btnSeletImage = view.findViewById(R.id.imageSelected);
        btnSend = view.findViewById(R.id.textViewSend);
        btnChangePwd = view.findViewById(R.id.textViewChangePwd);
        textViewLastLogin = view.findViewById(R.id.loginTime);
        verifyBtn = view.findViewById(R.id.verified);
        textViewRateNo = view.findViewById(R.id.numberOfRate);
        textViewUserName = view.findViewById(R.id.text_viewName);
        spinnerACCType = view.findViewById(R.id.spinner);

        imageViewProfile = view.findViewById(R.id.image_view);
        ratingBar = view.findViewById(R.id.ratingBar);
        viewSocialIconsLayout = view.findViewById(R.id.editProfileCustomLayout);
        viewSocialIconsLayout.setVisibility(View.INVISIBLE);

        LayerDrawable stars = (LayerDrawable) this.ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#ffcc00"), PorterDuff.Mode.SRC_ATOP);

        textViewAdsSold = view.findViewById(R.id.share);
        textViewTotalList = view.findViewById(R.id.addfav);
        textViewInactiveAds = view.findViewById(R.id.report);

        editTextName = view.findViewById(R.id.editTextName);
        editTextPhone = view.findViewById(R.id.editTextPhone);

        autoCompleteTextViewLocation = view.findViewById(R.id.editTexLocation);

        if (mGoogleApiClient == null)
            mGoogleApiClient = new GoogleApiClient
                    .Builder(getActivity())
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addConnectionCallbacks(EditProfile.this)
                    .build();
        autoCompleteTextViewLocation.setOnItemClickListener(mAutocompleteClickListener);

        // Create Filter
        AutocompleteFilter typeFilter = null;
        if (settingsMain.getAlertDialogMessage("location_type").equals("regions"))
            typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(TYPE_FILTER_ADDRESS)
                    .build();
        else {
            typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(TYPE_FILTER_REGIONS)
                    .build();
        }

        mPlaceArrayAdapter = new PlaceArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, typeFilter);
        autoCompleteTextViewLocation.setAdapter(mPlaceArrayAdapter);

        btnSeletImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adforest_sendData();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        adforest_setAllViewsText();

        ((HomeActivity) getActivity()).changeImage();

        btnChangePwd.setTextColor(Color.parseColor(settingsMain.getMainColor()));
        btnChangePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adforest_showDilogChangePassword();
            }
        });

        ratingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    RatingFragment fragment = new RatingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("id", settingsMain.getUserLogin());
                    bundle.putBoolean("isprofile", true);
                    fragment.setArguments(bundle);

                    replaceFragment(fragment, "RatingFragment");
                }
                return true;
            }
        });
        editTextIntroduction = (EditText) view.findViewById(R.id.textArea_information);

        editTextIntroduction.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
        return view;
    }

    private void adforest_sendData() {
        checkValidation = true;

        if (SettingsMain.isConnectingToInternet(getActivity())) {


            subcatDiloglist subDiloglist = (subcatDiloglist) spinnerACCType.getSelectedView().getTag();
            JsonObject params = new JsonObject();
            if (jsonObjectforCustom != null && adforest_getDataFromDynamicViewsForCustom() != null) {
                params.add("social_icons", adforest_getDataFromDynamicViewsForCustom());
                if (fieldsValidationforCustom.size() > 0) {

                    for (int i = 0; i < fieldsValidationforCustom.size(); i++) {

                        if (fieldsValidationforCustom.get(i) instanceof EditText) {
                            EditText editText = (EditText) fieldsValidationforCustom.get(i);
                            if (!URLUtil.isValidUrl(editText.getText().toString()) && !editText.getText().toString().equals("")) {
                                editText.setError("!");
                                checkValidation = false;
                            }
                        }
                    }
                }
            }
                params.addProperty(editTextName.getTag().toString(), editTextName.getText().toString());
                try {
                    String phoneNumber = editTextPhone.getText().toString();
                    if (extraText.getBoolean("is_verification_on")) {
                        if (phoneNumber.contains("+"))
                            params.addProperty(editTextPhone.getTag().toString(), phoneNumber);
                        else
                            params.addProperty(editTextPhone.getTag().toString(), "+" + phoneNumber);
                    } else
                        params.addProperty(editTextPhone.getTag().toString(), phoneNumber);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                params.addProperty(autoCompleteTextViewLocation.getTag().toString(), autoCompleteTextViewLocation.getText().toString());
                params.addProperty(spinnerACCType.getTag().toString(), subDiloglist.getId());
                params.addProperty(editTextIntroduction.getTag().toString(), editTextIntroduction.getText().toString());

                Log.d("info Send UpdatePofile", "" + params.toString());
                if (!checkValidation) {
                    Toast.makeText(getContext(), "Invalid URL specified", Toast.LENGTH_SHORT).show();
                } else {
                    SettingsMain.showDilog(getActivity());
                    Call<ResponseBody> req = restService.postUpdateProfile(params, UrlController.UploadImageAddHeaders(getActivity()));
                    req.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseobj) {
                            try {
                                if (responseobj.isSuccessful()) {
                                    Log.d("info UpdateProfile Resp", "" + responseobj.toString());

                                    JSONObject response = new JSONObject(responseobj.body().string());
                                    if (response.getBoolean("success")) {
                                        Log.d("info UpdateProfile obj", "" + response.toString());
                                        settingsMain.setUserName(response.getJSONObject("data").getString("display_name"));
                                        settingsMain.setUserPhone(response.getJSONObject("data").getString("phone"));
                                        settingsMain.setUserImage(response.getJSONObject("data").getString("profile_img"));
                                        ((HomeActivity) getActivity()).changeImage();

                                        Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                                        SettingsMain.reload(getActivity(), "EditProfile");
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
                                Log.d("info UpdateProfile", "NullPointert Exception" + t.getLocalizedMessage());
                                SettingsMain.hideDilog();
                            } else {
                                SettingsMain.hideDilog();
                                Log.d("info UpdateProfile err", String.valueOf(t));
                                Log.d("info UpdateProfile err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                            }
                        }
                    });
                }

            } else {
                SettingsMain.hideDilog();
                Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
            }
        }

    private void selectImage() {

        final CharSequence[] items;
        try {
            items = new CharSequence[]{jsonObject_select_pic.getString("camera"), jsonObject_select_pic.getString("library")
                    , jsonObject_select_pic.getString("cancel")};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(jsonObject_select_pic.getString("title"));
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    boolean result = SettingsMain.checkPermission(getActivity());

                    if (item == 0) {
                        userChoosenTask = "Take Photo";
                        if (result)
                            cameraIntent();

                    } else if (item == 1) {
                        userChoosenTask = "Choose from Library";
                        if (result)
                            galleryIntent();

                    } else if (item == 3) {
                        dialog.dismiss();
                    }
                }
            });
            builder.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SettingsMain.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if (userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                }
// else {
//                    //code for deny
//                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (thumbnail != null) {
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        }
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cameraImageUpload(destination.getAbsolutePath());


        btnSeletImage.setImageURI(Uri.parse(destination.getAbsolutePath()));
    }

    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                Uri tempUri = SettingsMain.getImageUri(getActivity(), bm);
                File finalFile = new File(SettingsMain.getRealPathFromURI(getActivity(), tempUri));

                Log.d("imageval", "" + finalFile.length());
                Log.d("imageval", "" + tempUri.toString());

                galleryImageUpload(tempUri);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        btnSeletImage.setImageBitmap(bm);
    }

    private void galleryImageUpload(final Uri absolutePath) {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());
            final File finalFile = new File(SettingsMain.getRealPathFromURI(getActivity(), absolutePath));
            RequestBody requestFile =
                    RequestBody.create(
                            MediaType.parse(getContext().getContentResolver().getType(absolutePath)),
                            finalFile
                    );
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("profile_img", finalFile.getName(), requestFile);

            Call<ResponseBody> req = restService.postUploadProfileImage(body, UrlController.UploadImageAddHeaders(getActivity()));
            req.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    if (response.isSuccessful()) {
                        Log.v("info Upload Responce", response.toString());
                        JSONObject responseobj = null;
                        try {
                            responseobj = new JSONObject(response.body().string());
                            if (responseobj.getBoolean("success")) {
                                try {
                                    Toast.makeText(getActivity(), responseobj.get("message").toString(), Toast.LENGTH_SHORT).show();
                                    Log.d("info data object", "" + responseobj.getJSONObject("data"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //noinspection ResultOfMethodCallIgnored
                                finalFile.delete();
                            } else {
                                Toast.makeText(getActivity(), responseobj.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            SettingsMain.hideDilog();
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                            SettingsMain.hideDilog();
                        }

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
                        Log.d("info Upload profile", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info Upload profile err", String.valueOf(t));
                        Log.d("info Upload profile err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }


    }

    private void cameraImageUpload(final String absolutePath) {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());
            final File file = new File(absolutePath);

            // Parsing any Media type file
            RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
            MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("profile_img", file.getName(), requestBody);

            Call<ResponseBody> req = restService.postUploadProfileImage(fileToUpload, UrlController.UploadImageAddHeaders(getActivity()));
            req.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    if (response.isSuccessful()) {
                        Log.v("info Upload Responce", response.toString());
                        JSONObject responseobj = null;
                        try {
                            responseobj = new JSONObject(response.body().string());
                            if (responseobj.getBoolean("success")) {
                                try {
                                    Toast.makeText(getActivity(), responseobj.get("message").toString(), Toast.LENGTH_SHORT).show();
                                    Log.d("info data object", "" + responseobj.getJSONObject("data"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //noinspection ResultOfMethodCallIgnored
                                file.delete();
                            } else {
                                Toast.makeText(getActivity(), responseobj.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            SettingsMain.hideDilog();
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                            SettingsMain.hideDilog();
                        }

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
                        Log.d("info Upload profile", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info Upload profile err", String.valueOf(t));
                        Log.d("info Upload profile err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }


    }

    private void adforest_setAllViewsText() {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());
            restService = UrlController.createService(RestService.class, settingsMain.getUserEmail(), settingsMain.getUserPassword(), getActivity());
            Call<ResponseBody> myCall = restService.getEditProfileDetails(UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info Edit Profile ", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info Edit ProfileGet", "" + response.getJSONObject("data"));

                                jsonObject = response.getJSONObject("data");
                                extraText = response.getJSONObject("extra_text");

                                textViewLastLogin.setText(jsonObject.getJSONObject("profile_extra").getString("last_login"));
                                textViewUserName.setText(jsonObject.getJSONObject("profile_extra").getString("display_name"));
                                getActivity().setTitle(response.getJSONObject("data").getString("page_title_edit"));

                                settingsMain.setUserName(jsonObject.getJSONObject("profile_extra").getString("display_name"));
                                settingsMain.setUserImage(jsonObject.getJSONObject("profile_extra").getString("profile_img"));

                                ((HomeActivity) getActivity()).changeImage();

                                Picasso.with(getActivity()).load(jsonObject.getJSONObject("profile_extra").getString("profile_img"))
                                        .error(R.drawable.placeholder)
                                        .placeholder(R.drawable.placeholder)
                                        .into(imageViewProfile);

                                Picasso.with(getActivity()).load(jsonObject.getJSONObject("profile_extra").getString("profile_img"))
                                        .error(R.drawable.placeholder)
                                        .placeholder(R.drawable.placeholder)
                                        .into(btnSeletImage);
                                verifyBtn.setText(jsonObject.getJSONObject("profile_extra").getJSONObject("verify_buton").getString("text"));
                                verifyBtn.setBackground(CustomBorderDrawable.customButton(0, 0, 0, 0,
                                        jsonObject.getJSONObject("profile_extra").getJSONObject("verify_buton").getString("color"),
                                        jsonObject.getJSONObject("profile_extra").getJSONObject("verify_buton").getString("color"),
                                        jsonObject.getJSONObject("profile_extra").getJSONObject("verify_buton").getString("color"), 3));

                                textViewAdsSold.setText(jsonObject.getJSONObject("profile_extra").getString("ads_sold"));
                                textViewTotalList.setText(jsonObject.getJSONObject("profile_extra").getString("ads_total"));
                                textViewInactiveAds.setText(jsonObject.getJSONObject("profile_extra").getString("ads_inactive"));

                                ratingBar.setNumStars(5);
                                ratingBar.setRating(Float.parseFloat(jsonObject.getJSONObject("profile_extra").getJSONObject("rate_bar").getString("number")));
                                textViewRateNo.setText(jsonObject.getJSONObject("profile_extra").getJSONObject("rate_bar").getString("text"));

                                textViewMainTitle.setText(response.getJSONObject("extra_text").getString("profile_edit_title"));
                                btnSend.setText(response.getJSONObject("extra_text").getString("save_btn"));
                                btnCancel.setText(response.getJSONObject("extra_text").getString("cancel_btn"));

                                btnSeletImage.setContentDescription(response.getJSONObject("extra_text").getString("select_image"));

                                jsonObjectChnge = response.getJSONObject("extra_text").getJSONObject("change_pass");
                                jsonObject_select_pic = response.getJSONObject("extra_text").getJSONObject("select_pic");

                                btnChangePwd.setText(jsonObjectChnge.getString("title"));
                                textViewName.setText(jsonObject.getJSONObject("display_name").getString("key"));
                                editTextName.setText(jsonObject.getJSONObject("display_name").getString("value"));
                                editTextName.setTag(jsonObject.getJSONObject("display_name").getString("field_name"));
                                textViewAccType.setText(jsonObject.getJSONObject("account_type").getString("key"));
                                spinnerACCType.setTag(jsonObject.getJSONObject("account_type").getString("field_name"));
                                textViewPhoneNumber.setText(jsonObject.getJSONObject("phone").getString("key"));
                                editTextPhone.setText(jsonObject.getJSONObject("phone").getString("value"));
                                editTextPhone.setTag(jsonObject.getJSONObject("phone").getString("field_name"));
                                textViewImage.setText(jsonObject.getJSONObject("profile_img").getString("key"));
                                textViewLocation.setText(jsonObject.getJSONObject("location").getString("key"));

                                textViewIntroduction.setText(jsonObject.getJSONObject("introduction").getString("key"));
                                editTextIntroduction.setText(jsonObject.getJSONObject("introduction").getString("value"));
                                editTextIntroduction.setTag(jsonObject.getJSONObject("introduction").getString("field_name"));


                                autoCompleteTextViewLocation.setText(jsonObject.getJSONObject("location").getString("value"));
                                autoCompleteTextViewLocation.setTag(jsonObject.getJSONObject("location").getString("field_name"));

                                if (response.getJSONObject("data").getBoolean("is_show_social")) {
                                    Log.d("Info custom", "====Add all===");
                                    social_icons.adforest_setViewsForCustom(response.getJSONObject("data"), publicProfileCustomIcons, getContext());
                                }
                                final JSONArray dropDownJSONOpt = jsonObject.getJSONArray("account_type_select");
                                final ArrayList<subcatDiloglist> SpinnerOptions;
                                SpinnerOptions = new ArrayList<>();
                                for (int j = 0; j < dropDownJSONOpt.length(); j++) {
                                    subcatDiloglist subDiloglist = new subcatDiloglist();
                                    subDiloglist.setId(dropDownJSONOpt.getJSONObject(j).getString("key"));
                                    subDiloglist.setName(dropDownJSONOpt.getJSONObject(j).getString("value"));

                                    SpinnerOptions.add(subDiloglist);
                                }

                                final SpinnerAndListAdapter spinnerAndListAdapter;
                                spinnerAndListAdapter = new SpinnerAndListAdapter(getActivity(), SpinnerOptions, true);

                                spinnerACCType.setAdapter(spinnerAndListAdapter);
                                if (response.getJSONObject("data").getBoolean("is_show_social")) {
                                    Log.d("Info custom", "====Add all===");
                                    adforest_setViewsForCustom(jsonObject);
                                }
                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        SettingsMain.hideDilog();
                    } catch (IOException e) {
                        e.printStackTrace();
                        SettingsMain.hideDilog();
                    }
                    SettingsMain.hideDilog();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    SettingsMain.hideDilog();
                    Log.d("info Edit Profile error", String.valueOf(t));
                    Log.d("info Edit Profile error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });

        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    void adforest_setViewsForCustom(JSONObject jsonObjec) {

        allViewInstanceforCustom.clear();
        fieldsValidationforCustom.clear();
        try {
            jsonObjectforCustom = jsonObjec;
            Log.d("info Custom data ===== ", jsonObjectforCustom.getJSONArray("social_icons").toString());
            JSONArray customOptnList = jsonObjectforCustom.getJSONArray("social_icons");

            viewSocialIconsLayout.setVisibility(View.VISIBLE);
            for (int noOfCustomOpt = 0; noOfCustomOpt < customOptnList.length(); noOfCustomOpt++) {
                CardView cardView = new CardView(getActivity());
                cardView.setCardElevation(2);
                cardView.setUseCompatPadding(true);
                cardView.setRadius(0);
                cardView.setPaddingRelative(10, 10, 10, 10);
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params1.topMargin = 10;
                params1.bottomMargin = 10;
                cardView.setLayoutParams(params1);

                JSONObject eachData = customOptnList.getJSONObject(noOfCustomOpt);

                TextInputLayout til = new TextInputLayout(getActivity());
                til.setHint(eachData.getString("key"));
                EditText et = new EditText(getActivity());
                et.setTextSize(14);
                if (!eachData.getString("value").equals("")) {
                    et.setText(eachData.getString("value"));
                }

                et.getBackground().clearColorFilter();
                allViewInstanceforCustom.add(et);
                fieldsValidationforCustom.add(et);
                til.addView(et);
                cardView.addView(til);

                viewSocialIconsLayout.addView(cardView);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JsonObject adforest_getDataFromDynamicViewsForCustom() {
        JsonObject optionsObj = null;
        try {
            JSONArray customOptnList = jsonObjectforCustom.getJSONArray("social_icons");
            optionsObj = new JsonObject();
            for (int noOfViews = 0; noOfViews < customOptnList.length(); noOfViews++) {
                JSONObject eachData = customOptnList.getJSONObject(noOfViews);

                TextView textView = (TextView) allViewInstanceforCustom.get(noOfViews);
                if (!textView.getText().toString().equalsIgnoreCase(""))
                    optionsObj.addProperty(eachData.getString("field_name"), textView.getText().toString());
                else
                    optionsObj.addProperty(eachData.getString("field_name"), textView.getText().toString());
                Log.d("variant_name", textView.getText().toString() + "");
            }

//            hideSoftKeyboard();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("array us custom", (optionsObj != null ? optionsObj.toString() : null) + " ==== size====  " + allViewInstanceforCustom.size());

        return optionsObj;
    }

    public void hideSoftKeyboard() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId;
            if (item != null) {
                placeId = String.valueOf(item.placeId);
                Log.i("sdfsdf", "Selected: " + item.description);
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
                Log.i("sdfsdf", "Fetching details for ID: " + item.placeId);
            }
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e("addfadsfa", "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            Log.e("addfadsfa", "Place query did not complete. Error: " +
                    place.getLatLng().toString());

        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(),
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);

    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    void adforest_showDilogChangePassword() {

        final Dialog dialog = new Dialog(getActivity(), R.style.customDialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_password);
        //noinspection ConstantConditions
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.parseColor("#00000000")));

        Button Send = dialog.findViewById(R.id.send_button);
        Button Cancel = dialog.findViewById(R.id.cancel_button);

        Send.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));
        Cancel.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));

        final EditText editTextOld = dialog.findViewById(R.id.editText);
        final EditText editTextNew = dialog.findViewById(R.id.editText2);
        final EditText editTextConfirm = dialog.findViewById(R.id.editText3);

        try {
            Send.setText(btnSend.getText());
            Cancel.setText(btnCancel.getText());

            editTextOld.setHint(jsonObjectChnge.getString("old_pass"));
            editTextNew.setHint(jsonObjectChnge.getString("new_pass"));
            editTextConfirm.setHint(jsonObjectChnge.getString("new_pass_con"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(editTextOld.getText().toString()) &&
                        !TextUtils.isEmpty(editTextNew.getText().toString()) &&
                        !TextUtils.isEmpty(editTextConfirm.getText().toString()))
                    if (editTextNew.getText().toString().equals(editTextConfirm.getText().toString())) {

                        if (SettingsMain.isConnectingToInternet(getActivity())) {

                            SettingsMain.showDilog(getActivity());

                            JsonObject params = new JsonObject();
                            params.addProperty("old_pass", editTextOld.getText().toString());
                            params.addProperty("new_pass", editTextNew.getText().toString());
                            params.addProperty("new_pass_con", editTextConfirm.getText().toString());

                            Log.d("info sendChange Passwrd", params.toString());
                            Call<ResponseBody> myCall = restService.postChangePasswordEditProfile(params, UrlController.AddHeaders(getActivity()));
                            myCall.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                                    try {
                                        if (responseObj.isSuccessful()) {
                                            Log.d("info ChangePassword Res", "" + responseObj.toString());

                                            JSONObject response = new JSONObject(responseObj.body().string());
                                            if (response.getBoolean("success")) {
                                                dialog.dismiss();
                                                settingsMain.setUserPassword(editTextNew.getText().toString());
                                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
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
                                    if (t instanceof TimeoutException) {
                                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                                        SettingsMain.hideDilog();
                                    }
                                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                                        SettingsMain.hideDilog();
                                    }
                                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                                        Log.d("info ChangePassword ", "NullPointert Exception" + t.getLocalizedMessage());
                                        SettingsMain.hideDilog();
                                    } else {
                                        SettingsMain.hideDilog();
                                        Log.d("info ChangePassword err", String.valueOf(t));
                                        Log.d("info ChangePassword err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                                    }
                                }
                            });
                        } else {
                            SettingsMain.hideDilog();
                            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        try {
                            Toast.makeText(getActivity(), "" + jsonObjectChnge.getString("err_pass"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void replaceFragment(Fragment someFragment, String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
        transaction.replace(R.id.frameContainer, someFragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    @Override
    public void onResume() {
        if (settingsMain.getAnalyticsShow() && !settingsMain.getAnalyticsId().equals(""))
            AnalyticsTrackers.getInstance().trackScreenView("Edit Profile");
        super.onResume();
    }
}
