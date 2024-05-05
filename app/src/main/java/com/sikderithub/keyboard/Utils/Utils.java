package com.sikderithub.keyboard.Utils;

import android.content.Context;
import android.os.AsyncTask;


import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    private static final String TAG = "Utils";
    public static void getAdsId(Context context, AdIdCallback mAdIdCallback){

        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                AdvertisingIdClient.Info idInfo = null;
                try {
                    idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                } catch (GooglePlayServicesNotAvailableException |
                         GooglePlayServicesRepairableException | IOException e) {
                    e.printStackTrace();
                }
                String advertId = null;
                try{
                    advertId = idInfo.getId();
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

                return advertId;
            }

            @Override
            protected void onPostExecute(String advertId) {
                if (mAdIdCallback != null){
                    mAdIdCallback.adId(advertId);
                }
            }

        };
        task.execute();
    }

    public static String getCurrentDateTimeString() {
        // Create a Calendar instance
        Calendar calendar = Calendar.getInstance();

        // Get the current date and time
        Date currentDate = calendar.getTime();

        String format = "yyyy-MM-dd HH:mm:ss";

        // Create a date formatter with the specified format
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);

        // Format the date and time
        return dateFormat.format(currentDate);
    }

    public interface AdIdCallback{
        void adId(String adId);
    }
}
