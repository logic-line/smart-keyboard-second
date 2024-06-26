package com.sikderithub.keyboard;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.sikderithub.keyboard.Models.Config;
import com.sikderithub.keyboard.Models.GenericResponse;
import com.sikderithub.keyboard.Models.Gk;
import com.sikderithub.keyboard.Models.NotificationData;
import com.sikderithub.keyboard.Models.Update;
import com.sikderithub.keyboard.Utils.Common;
import com.sikderithub.keyboard.Utils.LogKey;
import com.sikderithub.keyboard.internet.MyApi;
import com.sikderithub.keyboard.local.Dao.QuestionDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyApp extends Application {
    private static final String TAG = "MyAppLog";
    private static MyApp INSTANCE;
    public static MyApi myApi;
    private static Update cachedUpdate = new Update();
    private static int lastQuestionId;
    private static Config cachedConfig = new Config();
    public static NotificationData cachedNotification = null;
    private static FirebaseAnalytics firebaseAnalytics = null;

    public static MyApi getMyApi() {
        if (myApi == null) {
            myApi = MyApi.Companion.invoke();
        }
        return myApi;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        FirebaseApp.initializeApp(this);
        MobileAds.initialize(this, initializationStatus -> {

        });
        //getConfigFromServer();
        Common.setInstallTimeIfNotExits();
        registerFcmTopic();
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }
    }

    private void registerFcmTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        Log.d(TAG, msg);
                    }
                });
    }

    public static Config getConfig(){
        return cachedConfig;
    }
    public static Update getUpdateInfo(){
        Log.d(TAG, "getUpdateInfo: "+new Gson().toJson(cachedUpdate));
        return cachedUpdate;
    }

   public static synchronized MyApp getInstance(){
        return INSTANCE;
   }

   public  static Context getApContext(){
        return INSTANCE.getApplicationContext();
   }


   public static void getLatestQuestion(){
        QuestionDatabase.databaseWriteExecutor.execute(() -> {
            lastQuestionId = QuestionDatabase.getDatabase(getInstance())
                    .questionDAO()
                    .getLastQuestionId();

            Log.d(TAG, "getLatestQuestion: "+lastQuestionId);

            MyApp.getMyApi()
                    .getLatestQuestion(String.valueOf(lastQuestionId))
                    .enqueue(new Callback<GenericResponse<List<Gk>>>() {
                        @Override
                        public void onResponse(@NonNull Call<GenericResponse<List<Gk>>> call, Response<GenericResponse<List<Gk>>> response) {
                            if(response.isSuccessful() && response.body()!=null){
                                if (!response.body().error){
                                    QuestionDatabase.databaseWriteExecutor.execute(() -> {
                                        if(response.body().data.size()>0){

//                                            QuestionDatabase.getDatabase(getInstance())
//                                                            .questionDAO()
//                                                                    .clearQuestionTable();;

                                            QuestionDatabase.getDatabase(getInstance())
                                                    .questionDAO()
                                                    .insert(response.body().data);
                                        }


                                    });

                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<GenericResponse<List<Gk>>> call, Throwable t) {

                        }
                    });

        });
   }

   public static void getConfigFromServer(){
        MyApp.getMyApi().getConfig(BuildConfig.VERSION_CODE)
                .enqueue(new Callback<GenericResponse<Config>>() {
                    @Override
                    public void onResponse(Call<GenericResponse<Config>> call, Response<GenericResponse<Config>> response) {
                        if(response.isSuccessful() && response.body()!=null){
                            if (!response.body().error){
                                QuestionDatabase.databaseWriteExecutor.execute(() -> {
                                    QuestionDatabase.getDatabase(getInstance())
                                            .questionDAO()
                                            .insertOrUpdateConfig(response.body().data);

                                    if(response.body().data.update!=null){
                                        QuestionDatabase.getDatabase(getInstance())
                                                .questionDAO()
                                                .insertOrUpdateUpdateInfo(response.body().data.update);

                                        Log.d(TAG, "onResponse: Updated "+new Gson().toJson(response.body().data.update));
                                    }else {Log.d(TAG, "onResponse: Updated to null");
                                        QuestionDatabase.getDatabase(getInstance())
                                                .questionDAO()
                                                .clearUpdateInfo();
                                    }


                                });

                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse<Config>> call, Throwable t) {

                    }
                });
   }

   public static void initLocalConfig(){
       QuestionDatabase.databaseWriteExecutor.execute(() -> {
           Config c = QuestionDatabase.getDatabase(getInstance())
                   .questionDAO()
                   .getConfig();

           if(c!=null){
               cachedConfig = c;
           }else {
               cachedConfig = new Config();
           }

           Update update = QuestionDatabase.getDatabase(getInstance())
                   .questionDAO()
                   .getUpdateInfo();

           if(update!=null){
               Log.d(TAG, "initLocalConfig: "+new Gson().toJson(update));
               cachedUpdate = update;
           }else {
               cachedUpdate = new Update();
           }

           Log.d(TAG, "initLocalConfig: "+new Gson().toJson(cachedUpdate));

            cachedNotification = QuestionDatabase.getDatabase(getInstance())
                   .questionDAO()
                   .getLastNotification();
       });
   }

   public static void removeNotification(int id){
        cachedNotification = null;
        QuestionDatabase.databaseWriteExecutor.execute(() -> {
            QuestionDatabase.getDatabase(getInstance())
                    .questionDAO()
                    .removeNotification(id);
        });
   }

   public static void removeSavedGk(int id){
        QuestionDatabase.databaseWriteExecutor.execute(()->{
            QuestionDatabase.getDatabase(getInstance())
                    .questionDAO()
                    .removeNotification(id);
        });


   }

   public static void deleteCustomTheme(int id){
        QuestionDatabase.databaseWriteExecutor.execute(()->{
            QuestionDatabase.getDatabase(getInstance())
                    .questionDAO()
                    .deleteCustomTheme(id);
        });
   }
   public static void logEvent(LogKey logKey, HashMap<String, String> data){
        Bundle bundle = new Bundle();
       if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
           data.forEach(new BiConsumer<String, String>() {
               @Override
               public void accept(String s, String s2) {
                   bundle.putString(s, s2);
               }
           });
       }
       firebaseAnalytics.logEvent(logKey.name(), bundle);
   }
}
