package com.banglakeyboard.pro.customView;

import static android.os.Build.VERSION_CODES.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.banglakeyboard.pro.Models.UpdateAdsStatusResponse;
import com.banglakeyboard.pro.MyApp;
import com.banglakeyboard.pro.R;
import com.banglakeyboard.pro.Utils.Common;
import com.banglakeyboard.pro.Utils.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmojiAdView extends FrameLayout {
    private View containerView;
    private FrameLayout adContainerView;
    private AdView adView;
    private static final String TAG = "EmojiAdView";
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741";
    private Context context;
    public EmojiAdView(@NonNull Context context) {
        super(context);
        this.context = context;

        initViews();
    }

    public EmojiAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        initViews();
    }

    private void initViews(){
        this.setVisibility(GONE);
        containerView = this;
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(com.banglakeyboard.pro.R.layout.emoji_ad_view, this);

        adContainerView = findViewById(com.banglakeyboard.pro.R.id.ad_view_container);

    }

    public void loadBannerAds(){
        this.containerView.setVisibility(GONE);
        if(!Common.isAdShownAllowed()){
            Log.d(TAG, "initViews: ad status " + MyApp.getConfig().emoji_view_ad_status);
            Log.d(TAG, "loadBannerAd: ad shown allowed "+Common.isAdShownAllowed());
            adContainerView.setVisibility(GONE);
            return;
        }

        Log.d(TAG, "loadBannerAd: ad shown not allowed "+Common.isAdShownAllowed());

        if(adContainerView.getChildCount()>0){
            Log.d(TAG, "loadBannerAd: Ad already laoded");
            this.containerView.setVisibility(VISIBLE);
            return;
        }

        if (MyApp.getConfig().emoji_view_ad_type == 1){
            //admob ads
            loadAdmobAds();
        }else if (MyApp.getConfig().emoji_view_ad_type == 2){
            //custom ads
            loadCustomBannerAds();
        }else {
            //0 = None
            adContainerView.setVisibility(GONE);
        }

    }

    private void loadAdmobAds(){
        Log.d(TAG, "loadBanner: ");
        // Create an ad request.
        adView = new AdView(getContext());
        adView.setAdUnitId(AD_UNIT_ID);
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d(TAG, "onAdFailedToLoad: "+loadAdError.getMessage());
                adContainerView.setVisibility(GONE);
                containerView.setVisibility(GONE);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                containerView.setVisibility(VISIBLE);
                adContainerView.setVisibility(VISIBLE);
                requestLayout();
                //setAdAsShown();
                Log.d(TAG, "onAdLoaded: "+adContainerView.getVisibility());
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();


        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);

        Display display = wm.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(getContext(), adWidth);
    }

    private void loadCustomBannerAds(){
        CustomBannerAd.Builder adViewBuilder = new CustomBannerAd.Builder(getContext());

        adViewBuilder.setAdListener(new CustomAdListener() {
            @Override
            public void onAdClicked(int adUId) {
                Log.d(TAG, "onAdClicked: ");
                Utils.getAdsId(getContext(), new Utils.AdIdCallback() {
                    @Override
                    public void adId(String adId) {
                        if (adId != null)
                            MyApp.myApi.updateBannerAdsStatus("" + adUId, adId, "Clicked").enqueue(new Callback<UpdateAdsStatusResponse>() {
                                @Override
                                public void onResponse(Call<UpdateAdsStatusResponse> call, Response<UpdateAdsStatusResponse> response) {
                                    if (response.isSuccessful() && response.body() != null){
                                        Log.d(TAG, "onResponse: clicked status updated");
                                    }else {
                                        Log.d(TAG, "onResponse: " + response.message());
                                    }
                                }

                                @Override
                                public void onFailure(Call<UpdateAdsStatusResponse> call, Throwable t) {
                                    Log.d(TAG, "onFailure: ");
                                }
                            });
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull String message) {
                Log.d(TAG, "onAdFailedToLoad: ");
            }

            @Override
            public void onAdImpression() {
                Log.d(TAG, "onAdImpression: ");
                containerView.setVisibility(VISIBLE);
            }

            @Override
            public void onAdLoaded(int adUId) {
                Log.d(TAG, "onAdLoaded: ");
                Utils.getAdsId(getContext(), new Utils.AdIdCallback() {
                    @Override
                    public void adId(String adId) {
                        if (adId != null)
                            MyApp.myApi.updateBannerAdsStatus("" + adUId, adId, "Showed").enqueue(new Callback<UpdateAdsStatusResponse>() {
                                @Override
                                public void onResponse(Call<UpdateAdsStatusResponse> call, Response<UpdateAdsStatusResponse> response) {
                                    if (response.isSuccessful() && response.body() != null){
                                        Log.d(TAG, "onResponse: status updated");
                                    }else {
                                        Log.d(TAG, "onResponse: " + response.message());
                                    }
                                }

                                @Override
                                public void onFailure(Call<UpdateAdsStatusResponse> call, Throwable t) {
                                    Log.d(TAG, "onFailure: ");
                                }
                            });
                    }
                });
            }
        });

        adViewBuilder.loadAds();

        CustomBannerAd customBannerAd = adViewBuilder.build();

        if (customBannerAd != null){
            adContainerView.removeAllViews();
            adContainerView.addView(customBannerAd);

        }
    }
}
