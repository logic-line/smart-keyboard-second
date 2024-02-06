package com.sikderithub.keyboard.customView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sikderithub.keyboard.Models.BannerAds;
import com.sikderithub.keyboard.Models.GenericResponse;
import com.sikderithub.keyboard.MyApp;
import com.sikderithub.keyboard.R;
import com.sikderithub.keyboard.Utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomBannerAd extends FrameLayout implements View.OnClickListener {
    private static final String TAG = "EmojiAdView";
    private final Context context;
    private ImageView bannerImage;
    private Button actionBtn;
    private TextView titleTv;
    private TextView descriptionTv;
    private CustomAdListener listener;
    private String actionUrl;
    private int adUId = -1;
    public static final int EMOJI_ADS= 1;
    public static final int TOP_ADS = 2;

    private CustomBannerAd(@NonNull Context context) {
        super(context);
        this.context = context;

        initViews();
    }

    /*public CustomBannerAd(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        initViews();
    }

    public CustomBannerAd(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        initViews();
    }*/


    private void initViews(){

        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.custom_banner_adview, this);

        bannerImage = findViewById(R.id.iv_banner);
        actionBtn = findViewById(R.id.btn_action);
        titleTv = findViewById(R.id.tv_title);
        descriptionTv = findViewById(R.id.tv_description);

        //adsLayout.setVisibility(GONE);

        bannerImage.setOnClickListener(this);
        actionBtn.setOnClickListener(this);
        titleTv.setOnClickListener(this);
        descriptionTv.setOnClickListener(this);
    }

    private void loadAd(int position){

        Utils.getAdsId(getContext(), new Utils.AdIdCallback() {
            @Override
            public void adId(String adId) {
                Log.d(TAG, "adId: " + adId);
                if (adId != null){
                    Call<GenericResponse<BannerAds>> adsResponse = null;

                    if (position == TOP_ADS){
                        adsResponse = MyApp.myApi.getBannerAds(adId, "top");
                    }else if (position == EMOJI_ADS){
                        adsResponse = MyApp.myApi.getBannerAds(adId, "emoji");
                    }

                    if (adsResponse != null)
                        adsResponse.enqueue(new Callback<GenericResponse<BannerAds>>() {
                        @Override
                        public void onResponse(Call<GenericResponse<BannerAds>> call, Response<GenericResponse<BannerAds>> response) {
                            if(response.isSuccessful() && response.body()!=null){
                                if (!response.body().error){
                                    Log.d(TAG, "onResponse: response successful");
                                    BannerAds bannerAds = response.body().data;
                                    bindData(bannerAds);
                                }else {
                                    Log.d(TAG, "onResponse: error " + response.body().msg);
                                    if (listener != null){
                                        listener.onAdFailedToLoad(response.body().msg);
                                    }
                                }
                            }else {
                                if (listener != null){
                                    listener.onAdFailedToLoad("Unsuccessful Response");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<GenericResponse<BannerAds>> call, Throwable t) {
                            if (listener != null){
                                listener.onAdFailedToLoad(t.getMessage());
                            }
                        }
                    });
                }else {
                    Log.d(TAG, "loadAd: adId is null");
                }
            }
        });

    }

    private void bindData(BannerAds bannerAds){
        Log.d(TAG, "bindData: ");
        adUId = bannerAds.id;
        try {
            Glide.with(context)
                    .load(bannerAds.image)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.d(TAG, "onLoadFailed: image load failed");
                            bannerImage.setVisibility(GONE);

                            titleTv.setText(bannerAds.title);
                            descriptionTv.setText(bannerAds.description);

                            titleTv.setVisibility(VISIBLE);
                            descriptionTv.setVisibility(VISIBLE);
                            actionBtn.setVisibility(VISIBLE);

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "onResourceReady: image load successful");
                            titleTv.setVisibility(GONE);
                            descriptionTv.setVisibility(GONE);

                            bannerImage.setVisibility(VISIBLE);
                            actionBtn.setVisibility(VISIBLE);

                            listener.onAdImpression();
                            return false;
                        }
                    })
                    .into(bannerImage);

            actionBtn.setText("Open");
            actionUrl = bannerAds.action_url;

            if (listener!= null){
                listener.onAdLoaded(bannerAds.id);
                listener.onAdImpression();
            }else {
                Log.d(TAG, "loadAd: listener is null");
            }
        }catch (Exception e){
            if (listener != null){
                listener.onAdFailedToLoad(Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    public void setAdListener(CustomAdListener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if (actionUrl != null){
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setData(Uri.parse(actionUrl));
            viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PackageManager pm = getContext().getPackageManager();
            if (viewIntent.resolveActivity(pm) != null) {
                context.startActivity(viewIntent);
            }

            if (listener!= null && adUId != -1){
                listener.onAdClicked(adUId);
            }
        }
    }

    public static class Builder{
        private final Context context;
        private boolean isLoadCalled = false;
        private CustomAdListener listener;
        private int position = 0;

        public Builder(Context context){
            this.context = context;
        }

        public Builder loadAds(int position){
            isLoadCalled = true;
            this.position = position;
            return this;
        }

        public Builder setAdListener(CustomAdListener listener){
            this.listener = listener;
            return this;
        }

        public CustomBannerAd build(){
            CustomBannerAd adsView = null;
            if (context != null){
                adsView = new CustomBannerAd(context);
            }
            if (adsView != null){
                if (listener != null){
                    adsView.setAdListener(listener);
                }
                if (isLoadCalled){
                    adsView.loadAd(position);
                }
            }
            return adsView;
        }

    }

}
