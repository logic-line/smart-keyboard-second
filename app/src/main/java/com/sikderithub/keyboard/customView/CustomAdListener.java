package com.sikderithub.keyboard.customView;

import androidx.annotation.NonNull;

public interface CustomAdListener {
    void onAdClicked(int adUId);

    void onAdFailedToLoad(@NonNull String message);

    void onAdImpression();

    void onAdLoaded(int adUId);

}
