package com.sikderithub.keyboard.customView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import android.preference.Preference;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.sikderithub.keyboard.R;

public class SocialPreference extends Preference {

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
    }



    private void customMethod(String buttonId) {
        switch (buttonId) {
            case "btnFacebook":
                openURL("https://www.facebook.com/groups/1178280536938323/?ref=share_group_link");
                break;
            case "btnWeb":
                openURL("https://sikderithub.com");
                break;
            case "btnYoutube":
                openURL("https://youtube.com/@SmartKeyboard-gt7ch?si=jMQSKU_JQTUI-qjS");
                break;
            default:
                break;
        }
    }

    private void openURL(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        getContext().startActivity(intent);
    }
}