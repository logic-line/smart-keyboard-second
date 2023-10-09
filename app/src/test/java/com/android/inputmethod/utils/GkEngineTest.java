package com.android.inputmethod.utils;

import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(JUnit4.class)
public class GkEngineTest {
    private static final String TAG = "GkEngineTest";
    // Create an instance of GkEngine


    @Test
    public void testConcurrentGetGk() throws InterruptedException {
        GkEngine.getGkFromLocal();

        final int numThreads = 10;
        final int numGkEntries = 1000;

        GkEngine gkEngine = new GkEngine();


        // Create an ExecutorService to simulate concurrent calls
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Number of times to call getGk concurrently
        final int numIterations = 10000;

        for (int i = 0; i < numIterations; i++) {
            executor.submit(() -> {
                GkEngine.CurrentGk currentGk = gkEngine.getGk();
                if (currentGk != null) {
                    // Do something with the currentGk if needed
                    Log.d(TAG, "testConcurrentGetGk: "+currentGk.gk.question);
                }
            });
        }

        // Shutdown the executor and wait for all tasks to complete
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // Your assertions or verifications here
    }
}