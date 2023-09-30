package com.android.inputmethod.utils;

import android.util.Log;

import com.banglakeyboard.pro.Models.Gk;
import com.banglakeyboard.pro.MyApp;
import com.banglakeyboard.pro.local.Dao.QuestionDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GkEngine {
    private static final String TAG = "GkEngine";

    public static class CurrentGk {
        public Gk gk;
        public int position;

        public CurrentGk(Gk gk, int position) {
            this.gk = gk;
            this.position = position;
        }
    }

    private static List<Gk> cachedGk = new ArrayList<>();
    private static Lock cachedGkLock = new ReentrantLock();

    public static void getGkFromLocal() {
        Log.d(TAG, "getGkFromLocal: called");
        QuestionDatabase.databaseWriteExecutor.execute(() -> {
            List<Gk> newCachedGk = QuestionDatabase.getDatabase(MyApp.getApContext())
                    .questionDAO()
                    .getNotShownQuestions();
            Log.d(TAG, "getGkFromLocal: "+newCachedGk.size());
            cachedGkLock.lock();
            try {
                cachedGk = newCachedGk;
            } finally {
                cachedGkLock.unlock();
            }
        });
    }

    public static void addToSavedGk(CurrentGk currentGk) {
        cachedGkLock.lock();
        try {
            currentGk.gk.isSaved = true;
            //cachedGk.add(currentGk.position, currentGk.gk);

            QuestionDatabase.databaseWriteExecutor.execute(() -> {
                QuestionDatabase.getDatabase(MyApp.getApContext())
                        .questionDAO()
                        .updateGk(currentGk.gk);
            });
        } finally {
            cachedGkLock.unlock();
        }
    }

    public static void setGkAsShown(CurrentGk currentGk) {
        cachedGkLock.lock();
        try {
            int positionToRemove = currentGk.position;
            if (positionToRemove >= 0 && positionToRemove < cachedGk.size()) {
                cachedGk.remove(positionToRemove);
                currentGk.gk.isShown = true;

                QuestionDatabase.databaseWriteExecutor.execute(() -> {
                    QuestionDatabase.getDatabase(MyApp.getApContext())
                            .questionDAO()
                            .updateGk(currentGk.gk);
                });
            }
        } finally {
            cachedGkLock.unlock();
        }
    }

    private final Random random;

    public GkEngine() {
        random = new Random();
    }

    public CurrentGk getGk() {
        cachedGkLock.lock();
        try {
            if (cachedGk.isEmpty()) {
                return null;
            }

            int gkPosition = random.nextInt(cachedGk.size());
            return new CurrentGk(cachedGk.get(gkPosition), gkPosition);
        } finally {
            cachedGkLock.unlock();
        }
    }
}