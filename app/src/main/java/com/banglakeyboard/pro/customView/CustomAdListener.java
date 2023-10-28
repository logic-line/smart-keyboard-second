package com.banglakeyboard.pro.customView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.LoadAdError;

public interface CustomAdListener {
    void onAdClicked(int adUId);

    void onAdFailedToLoad(@NonNull String message);

    void onAdImpression();

    void onAdLoaded(int adUId);

}
