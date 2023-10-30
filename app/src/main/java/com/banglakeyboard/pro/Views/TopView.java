package com.banglakeyboard.pro.Views;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.inputmethod.latin.utils.InputTypeUtils;
import com.banglakeyboard.pro.Models.UpdateAdsStatusResponse;
import com.banglakeyboard.pro.Utils.Constants;
import com.banglakeyboard.pro.Utils.Utils;
import com.banglakeyboard.pro.customView.CustomAdListener;
import com.banglakeyboard.pro.customView.CustomAdView;
import com.banglakeyboard.pro.customView.CustomBannerAd;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.banglakeyboard.pro.BuildConfig;
import com.banglakeyboard.pro.CommonMethod;
import com.banglakeyboard.pro.MyApp;
import com.banglakeyboard.pro.R;
import com.banglakeyboard.pro.Utils.Common;
import com.banglakeyboard.pro.Utils.CustomThemeHelper;
import com.banglakeyboard.pro.Utils.PrefHelper;
import com.banglakeyboard.pro.Views.NativeAd.TemplateView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopView extends RelativeLayout {
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741";
    private static final String TAG = "TopView";
    private TemplateView mNativeAdView;
    private ImageView imgUpdate;
    private View view;
    private boolean isPasswordField = false;
    private FrameLayout adContainerView;
    private AdView adView;
    private SharedPreferences sharedPreferences;

    public void onFinishInputView(boolean finishingInput) {
        if (mNativeAdView!=null){
            mNativeAdView.destroyNativeAd();
        }
    }

    public void onStartInput(EditorInfo editorInfo) {
        if(InputTypeUtils.isPasswordInputType(editorInfo.inputType)){
            isPasswordField = true;
        }else{
            isPasswordField = false;
        }
    }

    private enum ContentType{
        AD,
        UPDATE,
        NONE
    }

    public TopView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.suggestionStripViewStyle);
    }

    public TopView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.layout_top_view, this);
        view = this;
        if(CustomThemeHelper.isCustomThemeApplicable(getContext()) && CustomThemeHelper.selectedCustomTheme!=null){
            setBackgroundColor(CustomThemeHelper.selectedCustomTheme.dominateColor);
        }
        mNativeAdView = findViewById(R.id.templateView);
        imgUpdate = findViewById(R.id.imgUpdate);
        adContainerView = findViewById(R.id.ad_view_container);

        setVisibility(GONE);

    }

    public void onStartInputView(boolean isRestarting){
        Log.d(TAG, "onStartInputView: ");
        if(isRestarting){
            Log.d(TAG, "onStartInputView: "+isRestarting);
            return;
        }

        ContentType ct = getContentType();
        Log.d(TAG, "ContentType: "+ct.name());

        if(ct==ContentType.NONE){
            setVisibility(GONE);
        }else {
            if(ct==ContentType.AD){
                //show ad content
                showAd();
            }else {
                //show update content
                showUpdateBanner();
            }

        }
    }

    private void showUpdateBanner() {
        loadUpdateBanner();
    }

    private void loadUpdateBanner() {

        mNativeAdView.setVisibility(GONE);

        String updateImgUrl = MyApp.getUpdateInfo().image_url;
        if(updateImgUrl!=null && !updateImgUrl.isEmpty()){
            imgUpdate.setVisibility(VISIBLE);
            setVisibility(VISIBLE);

            Glide.with(getContext())
                    .load(updateImgUrl)
                    .into(imgUpdate);

            if(MyApp.getUpdateInfo().referLink!=null && !MyApp.getUpdateInfo().referLink.isEmpty()){
                imgUpdate.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CommonMethod.INSTANCE.openLink(getContext(), MyApp.getUpdateInfo().referLink);
                    }
                });
            }
        }else {
            this.setVisibility(GONE);
        }
    }

    private void showAd() {

        sharedPreferences = getContext().getSharedPreferences(Constants.AD_SHOW_TIME_SHARED_PREFERENCE, Context.MODE_PRIVATE);

        imgUpdate.setVisibility(GONE);

        if(!Common.isAdShownAllowed()){
            Log.d(TAG, "showAd: ad shown not allowed");
            setVisibility(VISIBLE);
            adContainerView.setVisibility(GONE);
            return;
        }

        long currentTime = Calendar.getInstance().getTime().getTime();
        long prevTime = sharedPreferences.getLong(Constants.KEY_TOP_AD_TIME, -1);
        int interval = MyApp.getConfig().top_ad_interval;
        if (!Common.isIntervalExpired(currentTime, prevTime, interval))
            return;
        else {
            Log.d(TAG, "loadBannerAds: interval not expired");
        }

        adContainerView.setVisibility(GONE);

        if (MyApp.getConfig().top_view_ad_type == 1) {
            Log.d(TAG, "loadBannerAds: admob ads");
            //admob ads
            loadAdmobAds();
        } else if (MyApp.getConfig().top_view_ad_type == 2) {
            Log.d(TAG, "loadBannerAds: custom ads");
            //custom ads
            loadCustomBannerAds();
        } else {
            //0 = None
            Log.d(TAG, "loadBannerAds: none");
            adContainerView.setVisibility(GONE);
        }

        /*CustomAdView customAdView = new CustomAdView(getContext());
        customAdView.setPosition(CustomAdView.TOP_ADS);
        customAdView.loadBannerAds();

        adContainerView.addView(customAdView);
        Log.d(TAG, "showAd: ad container view child count " + adContainerView.getChildCount());
        adContainerView.setVisibility(VISIBLE);*/

    }

    private ContentType getContentType(){
//        if(true){
//            return ContentType.AD;
//        }
        if(isPasswordField){
            return ContentType.NONE;
        }
        if(isUpdateAvailable()){
            return ContentType.UPDATE;

        }else if(isAdIntervalPassed()){
            return ContentType.AD;
        }else {
            return ContentType.NONE;
        }

    }


    private int getViewVisibility() {
        if(isUpdateAvailable() || !isAdIntervalPassed()){
            return VISIBLE;
        }
        return GONE;
    }


    private boolean isUpdateAvailable(){
        return MyApp.getUpdateInfo().version_code > BuildConfig.VERSION_CODE && MyApp.getUpdateInfo().status==1;
    }

    private boolean isToadyAdShown(){
        SimpleDateFormat sdf = new SimpleDateFormat(Common.LAST_AD_DATE_FORMAT,  Locale.getDefault());
        String todayDateInString = sdf.format(new Date());
        return PrefHelper.getPref(todayDateInString, false);
    }

    private boolean isAdIntervalPassed(){
        long lastAdShownInMills = PrefHelper.getPref(PrefHelper.LAST_AD_SHOWN, 0L);
        long currentTimesInMills = System.currentTimeMillis();

        // Calculate the time difference in milliseconds
        long timeDifferenceInMilliseconds = currentTimesInMills - lastAdShownInMills;

        // Convert the time difference to minutes
        long timeDifferenceInMinutes = timeDifferenceInMilliseconds / (60 * 1000);
        // Check if the time difference is greater than 3 minutes

        return timeDifferenceInMinutes >= MyApp.getConfig().top_ad_interval;
    }

    private void setAdAsShown(){
        SimpleDateFormat sdf = new SimpleDateFormat(Common.LAST_AD_DATE_FORMAT,  Locale.getDefault());
        String todayDateInString = sdf.format(new Date());
        PrefHelper.putPref(todayDateInString, true);

        PrefHelper.putPref(PrefHelper.LAST_AD_SHOWN, System.currentTimeMillis());
    }

    private void loadNativeAd() {
        AdLoader adLoader = new AdLoader.Builder(getContext(), getResources().getString(R.string.admob_native_ad_gk_test))
                .forNativeAd(nativeAd -> {
//                        NativeTemplateStyle styles = new
//                                NativeTemplateStyle.Builder().withMainBackgroundColor(background).build();
//                        mNativeAdView.setStyles(styles);
                    mNativeAdView.setNativeAd(nativeAd);
                    setVisibility(VISIBLE);

                    mNativeAdView.setVisibility(VISIBLE);
                    requestLayout();
                    setAdAsShown();
                    Log.d(TAG, "loadNativeAd: showing");

                }).withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.d(TAG, "onAdFailedToLoad: "+loadAdError.getMessage());
                        mNativeAdView.setVisibility(GONE);
                        view.setVisibility(GONE);
                        measure(0, 0);
                    }
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void loadAdmobAds() {
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
                setVisibility(GONE);
                measure(0, 0);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adContainerView.setVisibility(VISIBLE);
                setVisibility(VISIBLE);
                requestLayout();
                setAdAsShown();
                Log.d(TAG, "onAdLoaded: "+adContainerView.getVisibility());

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(Constants.KEY_TOP_AD_TIME, Calendar.getInstance().getTime().getTime());
                editor.apply();
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

    private void loadCustomBannerAds() {
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
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull String message) {
                Log.d(TAG, "onAdFailedToLoad: ");
            }

            @Override
            public void onAdImpression() {
                Log.d(TAG, "onAdImpression: ");
                adContainerView.setVisibility(VISIBLE);
                setVisibility(VISIBLE);
                requestLayout();
                setAdAsShown();
            }

            @Override
            public void onAdLoaded(int adUId) {
                Log.d(TAG, "onAdLoaded: ");

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(Constants.KEY_TOP_AD_TIME, Calendar.getInstance().getTime().getTime());
                editor.apply();
                Utils.getAdsId(getContext(), new Utils.AdIdCallback() {
                    @Override
                    public void adId(String adId) {
                        if (adId != null)
                            MyApp.myApi.updateBannerAdsStatus("" + adUId, adId, "Showed").enqueue(new Callback<UpdateAdsStatusResponse>() {
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


        adViewBuilder.loadAds(CustomAdView.TOP_ADS);


        CustomBannerAd customBannerAd = adViewBuilder.build();

        if (customBannerAd != null) {
            adContainerView.removeAllViews();
            adContainerView.addView(customBannerAd);

        }
    }

}
