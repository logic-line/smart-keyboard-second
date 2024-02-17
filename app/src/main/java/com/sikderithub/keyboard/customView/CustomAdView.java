package com.sikderithub.keyboard.customView;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
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

import com.sikderithub.keyboard.Models.UpdateAdsStatusResponse;
import com.sikderithub.keyboard.MyApp;
import com.sikderithub.keyboard.R;
import com.sikderithub.keyboard.Utils.Common;
import com.sikderithub.keyboard.Utils.Constants;
import com.sikderithub.keyboard.Utils.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomAdView extends FrameLayout {
    public static final int EMOJI_ADS = 1;
    public static final int TOP_ADS = 2;
    private static final String TAG = "EmojiAdView";
    //private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"; //test ad id
    //private static final String AD_UNIT_ID = "ca-app-pub-8326396827024206/8824334669"; //real ad id
    private final Context context;
    private View containerView;
    private FrameLayout adContainerView;
    private AdView adView;
    private CustomBannerAd customBannerAd = null;
    private int position = 0;
    private int shownAdType = 0;
    private SharedPreferences sharedPreferences;

    public CustomAdView(@NonNull Context context) {
        super(context);
        this.context = context;

        initViews();
    }

    public CustomAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomAdView);

        position = a.getInt(R.styleable.CustomAdView_position, 1);
        a.recycle();
        requestLayout();
        invalidate();

        initViews();
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private void initViews() {
        sharedPreferences = context.getSharedPreferences(Constants.AD_SHOW_TIME_SHARED_PREFERENCE, Context.MODE_PRIVATE);

        this.setVisibility(GONE);
        containerView = this;
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(com.sikderithub.keyboard.R.layout.custom_ad_view, this);

        adContainerView = findViewById(com.sikderithub.keyboard.R.id.ad_view_container);


        adContainerView.setVisibility(GONE);
        this.containerView.setVisibility(GONE);
    }

    public void loadBannerAds() {
        this.containerView.setVisibility(GONE);
        if (!Common.isAdShownAllowed()) {
            Log.d(TAG, "initViews: ad status " + MyApp.getConfig().emoji_view_ad_status);
            Log.d(TAG, "loadBannerAd: ad shown allowed " + Common.isAdShownAllowed());
            adContainerView.setVisibility(GONE);
            return;
        }

        Log.d(TAG, "loadBannerAd: ad shown allowed " + Common.isAdShownAllowed());

        //if the ad type is changed then we have to reload the ads

        Log.d(TAG, "loadBannerAds: shownAds type " + shownAdType);
        Log.d(TAG, "loadBannerAds: loading ad type " + MyApp.getConfig().emoji_view_ad_type);

        if (position == CustomBannerAd.EMOJI_ADS && MyApp.getConfig().emoji_view_ad_type != shownAdType) {
            Log.d(TAG, "loadBannerAds: removing views");
            adContainerView.removeAllViews();

            if (MyApp.getConfig().emoji_view_ad_type == 1) {
                adView = null;
            } else if (MyApp.getConfig().emoji_view_ad_type == 2) {
                customBannerAd = null;
            }

            shownAdType = MyApp.getConfig().emoji_view_ad_type;
            this.containerView.setVisibility(GONE);
        }

        int emoji_ad_type = MyApp.getConfig().emoji_view_ad_type;
        if ((emoji_ad_type == 1 && adView != null) || (emoji_ad_type == 2 && customBannerAd != null)) {

            Log.d(TAG, "loadBannerAd: Ad already laoded");
            this.containerView.setVisibility(VISIBLE);
            return;
        }

        if (position == CustomBannerAd.EMOJI_ADS) {

            long currentTime = Calendar.getInstance().getTime().getTime();
            long prevTime = sharedPreferences.getLong(Constants.KEY_EMOJI_AD_TIME, -1);
            int interval = MyApp.getConfig().emoji_ad_interval;
            if (!Common.isIntervalExpired(currentTime, prevTime, interval)) {
                Log.d(TAG, "loadBannerAds: interval not expired");
                return;
            }


            adContainerView.setVisibility(GONE);
            this.containerView.setVisibility(GONE);

            if (MyApp.getConfig().emoji_view_ad_type == 1) {
                //admob ads
                loadAdmobAds();
            } else if (MyApp.getConfig().emoji_view_ad_type == 2) {
                //custom ads
                loadCustomBannerAds();
            } else {
                //0 = None
                adContainerView.setVisibility(GONE);
            }
        } else if (position == CustomBannerAd.TOP_ADS) {

            long currentTime = Calendar.getInstance().getTime().getTime();
            long prevTime = sharedPreferences.getLong(Constants.KEY_TOP_AD_TIME, -1);
            int interval = MyApp.getConfig().top_ad_interval;
            if (!Common.isIntervalExpired(currentTime, prevTime, interval))
                return;


            MyApp.getConfig().top_view_ad_type = 1;
            Log.d(TAG, "loadBannerAds: ad position top");
            if (MyApp.getConfig().top_view_ad_type == 1) {
                //admob ads
                loadAdmobAds();
            } else if (MyApp.getConfig().top_view_ad_type == 2) {
                //custom ads
                loadCustomBannerAds();
            } else {
                //0 = None
                adContainerView.setVisibility(GONE);
            }
        }
    }

    private void loadAdmobAds() {
        Log.d(TAG, "loadBanner: ");
        // Create an ad request.
        adView = new AdView(getContext());
        adView.setAdUnitId(Constants.EMOJI_AD_UNIT_ID);
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
                Log.d(TAG, "onAdFailedToLoad: " + loadAdError.getMessage());
                adView = null;
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
                if (position == CustomAdView.EMOJI_ADS) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(Constants.KEY_EMOJI_AD_TIME, Calendar.getInstance().getTime().getTime());
                    editor.apply();
                } else if (position == CustomAdView.TOP_ADS) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(Constants.KEY_TOP_AD_TIME, Calendar.getInstance().getTime().getTime());
                    editor.apply();
                }
                Log.d(TAG, "onAdLoaded: " + adContainerView.getVisibility());
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();


        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

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

    private void loadCustomBannerAds() {
        CustomBannerAd.Builder adViewBuilder = new CustomBannerAd.Builder(getContext());

        adViewBuilder.setAdListener(new CustomAdListener() {
            @Override
            public void onAdClicked(int adUId) {
                Log.d(TAG, "onAdClicked: ");
                Utils.getAdsId(getContext(), new Utils.AdIdCallback() {
                    @Override
                    public void adId(String adId) {
                        if (adId != null) {

                            MyApp.myApi.updateBannerAdsStatus("" + adUId, adId, "Click").enqueue(new Callback<UpdateAdsStatusResponse>() {
                                @Override
                                public void onResponse(Call<UpdateAdsStatusResponse> call, Response<UpdateAdsStatusResponse> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Log.d(TAG, "onResponse: clicked status updated");
                                    } else {
                                        Log.d(TAG, "onResponse: " + response.message());
                                    }
                                }

                                @Override
                                public void onFailure(Call<UpdateAdsStatusResponse> call, Throwable t) {
                                    Log.d(TAG, "onFailure: ");
                                }
                            });
                        }

                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull String message) {
                Log.d(TAG, "onAdFailedToLoad: ");
                containerView.setVisibility(GONE);
            }

            @Override
            public void onAdImpression() {
                Log.d(TAG, "onAdImpression: ");
                containerView.setVisibility(VISIBLE);
            }

            @Override
            public void onAdLoaded(int adUId) {
                Log.d(TAG, "onAdLoaded: ");

                if (position == CustomAdView.EMOJI_ADS) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(Constants.KEY_EMOJI_AD_TIME, Calendar.getInstance().getTime().getTime());
                    editor.apply();
                } else if (position == CustomAdView.TOP_ADS) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(Constants.KEY_TOP_AD_TIME, Calendar.getInstance().getTime().getTime());
                    editor.apply();
                }

                Utils.getAdsId(getContext(), new Utils.AdIdCallback() {
                    @Override
                    public void adId(String adId) {
                        if (adId != null)
                            MyApp.myApi.updateBannerAdsStatus("" + adUId, adId, "Show").enqueue(new Callback<UpdateAdsStatusResponse>() {
                                @Override
                                public void onResponse(Call<UpdateAdsStatusResponse> call, Response<UpdateAdsStatusResponse> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Log.d(TAG, "onResponse: status updated");
                                    } else {
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


        adViewBuilder.loadAds(position);


        customBannerAd = adViewBuilder.build();

        if (customBannerAd != null) {
            adContainerView.removeAllViews();
            adContainerView.addView(customBannerAd);

            adContainerView.setVisibility(VISIBLE);
            containerView.setVisibility(VISIBLE);
        }
    }
}
