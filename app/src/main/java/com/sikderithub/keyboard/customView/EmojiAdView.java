package com.sikderithub.keyboard.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sikderithub.keyboard.R;
import com.google.android.gms.ads.AdView;

public class EmojiAdView extends FrameLayout {

    private FrameLayout adContainerView;
    private AdView adView;
    private static final String TAG = "EmojiAdView";
    //private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"; //test ad id
    //private static final String AD_UNIT_ID = "ca-app-pub-8326396827024206/5013044172"; //real ad id
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

        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(com.sikderithub.keyboard.R.layout.emoji_ad_view, this);

        adContainerView = findViewById(com.sikderithub.keyboard.R.id.ad_view_container);

    }

    /*public void loadBannerAds(){
        if(MyApp.getConfig().emoji_view_ad_status==0 || !Common.isAdShownAllowed()){
            Log.d(TAG, "initViews: ad status " + MyApp.getConfig().emoji_view_ad_status);
            Log.d(TAG, "loadBannerAd: ad shown not allowed "+Common.isAdShownAllowed());
            adContainerView.setVisibility(GONE);
            return;
        }

        Log.d(TAG, "loadBannerAd: ad shown not allowed "+Common.isAdShownAllowed());

        if(adContainerView.getChildCount()>0){
            Log.d(TAG, "loadBannerAd: Ad already laoded");
            return;
        }


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
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adContainerView.setVisibility(VISIBLE);
                requestLayout();
                //setAdAsShown();
                Log.d(TAG, "onAdLoaded: "+adContainerView.getVisibility());
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();


        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }*/

    /*private AdSize getAdSize() {
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
    }*/
}
