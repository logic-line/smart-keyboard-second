package com.sikderithub.keyboard;

import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.inputmethod.dictionarypack.EventHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AndroidJUnit4.class)
public class EventHandlerTest {
    private EventHandler eventHandler;
    private Context mockContext;
    private Intent mockIntent;

    @Before
    public void setUp() {
        eventHandler = new EventHandler();
        mockContext = Mockito.mock(Context.class);
        mockIntent = Mockito.mock(Intent.class);
    }

    @Test
    public void testOnReceiveIsCalled() {
        eventHandler.onReceive(mockContext, mockIntent);

        // Verify that the startService or startForegroundService methods were called
        // Depending on the Android version
        Mockito.verify(mockContext).startService(Mockito.any(Intent.class));
        Mockito.verify(mockContext, Mockito.never()).startForegroundService(Mockito.any(Intent.class));
    }

}