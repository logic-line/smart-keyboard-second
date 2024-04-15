package com.sikderithub.keyboard.customView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.sikderithub.keyboard.Models.GenericResponse;
import com.sikderithub.keyboard.Models.SocialLink;
import com.sikderithub.keyboard.MyApp;
import com.sikderithub.keyboard.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SocialPreference extends Preference {

    private static final String TAG = "SocialLink";
    private String webLink, fbLink, youtubeLink;


    public SocialPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.custom_preference_social);
    }

    @Override
    protected void onBindView(View holder) {
        super.onBindView(holder);

        LottieAnimationView btnFacebook = (LottieAnimationView) holder.findViewById(R.id.btnFacebook);
        LottieAnimationView btnYoutube = (LottieAnimationView) holder.findViewById(R.id.btnYoutube);
        LottieAnimationView btnWeb = (LottieAnimationView) holder.findViewById(R.id.btnWebIcon);

        btnFacebook.setOnClickListener(v -> customMethod("btnFacebook"));
        btnYoutube.setOnClickListener(v -> customMethod("btnYoutube"));
        btnWeb.setOnClickListener(v -> customMethod("btnWeb"));
        getAllLink();
    }


    private void customMethod(String buttonId) {
        switch (buttonId) {
            case "btnFacebook":
                openURL(fbLink);
                break;
            case "btnWeb":
                openURL(webLink);
                break;
            case "btnYoutube":
                openURL(youtubeLink);
                break;
            default:
                break;
        }
    }


    private void getAllLink() {

        MyApp.getMyApi()
                .getSocialLink()
                .enqueue(new Callback<GenericResponse<SocialLink>>() {
                    @Override
                    public void onResponse(Call<GenericResponse<SocialLink>> call, Response<GenericResponse<SocialLink>> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            SocialLink model = response.body().data;

                            Log.d(TAG, "onResponse: " + model);

                            webLink = model.web_link;
                            fbLink = model.facebook_link;
                            youtubeLink = model.youtube_link;


                        } else {
                            Log.d(TAG, "onResponse: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse<SocialLink>> call, Throwable t) {

                    }
                });
    }

    private void openURL(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        getContext().startActivity(intent);
    }
}