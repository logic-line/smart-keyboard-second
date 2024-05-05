package com.sikderithub.keyboard;

import android.content.Intent;
import android.icu.util.Calendar;
import android.icu.util.TimeUnit;
import android.util.Log;
import android.util.TimeUtils;
import android.view.inputmethod.EditorInfo;

import com.android.inputmethod.latin.PhoneticBangla;
import com.android.inputmethod.latin.setup.SetupWizardActivity;
import com.android.inputmethod.utils.MyLatinIME;
import com.google.gson.Gson;
import com.sikderithub.keyboard.Activity.UpdateActivity;
import com.sikderithub.keyboard.Utils.LogKey;
import com.sikderithub.keyboard.Utils.Utils;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SikderKeyboard extends MyLatinIME {
    private static final String TAG = "SikderKeyBaord";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");


        //phoneticKeyMap();

    }

    private void phoneticKeyMap() {
        PhoneticBangla phoneticBangla = new PhoneticBangla();

        printKeyMap(phoneticBangla.bbr);
        printKeyMap(phoneticBangla.jbr);
        printKeyMap(phoneticBangla.kar);
        printKeyMap(phoneticBangla.srb);
        printKeyMap(phoneticBangla.other);
    }

    private void printKeyMap(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key + "=> " + value);
        }
    }

    @Override
    public void onStartInput(EditorInfo editorInfo, boolean restarting) {
        super.onStartInput(editorInfo, restarting);
    }

    @Override
    public void onStartInputView(EditorInfo editorInfo, boolean restarting) {


        MyApp.getConfigFromServer();
//        MyApp.getLatestQuestion();

        MyApp.initLocalConfig();

        if(MyApp.getUpdateInfo().version_code>BuildConfig.VERSION_CODE && MyApp.getUpdateInfo().status==1){
            //updateAvailable
            if(MyApp.getUpdateInfo().update_type==1){
                try{
                    startActivity(new Intent(this, UpdateActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }catch (Exception e){
                    Log.d(TAG, "onStartInputView: start activity error " + e.getMessage());
                }

            }
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("onStartInputView", "openKeyboardInputView");
        data.put("UUID", UUID.randomUUID().toString());
        data.put("Time", Utils.getCurrentDateTimeString());
        MyApp.logEvent(LogKey.KEYBOARD_OPEN, data);

        super.onStartInputView(editorInfo, restarting);



    }

    @Override
    public void onWindowHidden() {
        Log.d(TAG, "onWindowHidden: ");
        super.onWindowHidden();
    }

    @Override
    public void onWindowShown() {
        Log.d(TAG, "onWindowShown: ");
        super.onWindowShown();
    }

}
