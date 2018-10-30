package com.segment.analytics.android.integrations.clevertap;

import android.app.Activity;
import android.app.Application;
import android.app.usage.UsageEvents;
import android.os.Bundle;
import com.clevertap.android.sdk.CleverTapAPI;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.android.integrations.clevertap.CleverTapIntegration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.ScreenPayloadBuilder;

import java.lang.Exception;

import static com.segment.analytics.Utils.createTraits;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*", "org.json.*" })
@PrepareForTest(CleverTapAPI.class)

public class CleverTapTest {
    @Mock CleverTapAPI clevertap;
    @Mock Application context;
    Logger logger;
    @Mock Analytics analytics;

    CleverTapIntegration integration;

    @Before
    public void setUp() {
        initMocks(this);
        mockStatic(CleverTapAPI.class);
        logger = Logger.with(Analytics.LogLevel.DEBUG);
        when(analytics.logger("CleverTap")).thenReturn(logger);
        when(analytics.getApplication()).thenReturn(context);

        try {
            when(CleverTapAPI.getInstance(context)).thenReturn(clevertap);
        } catch (Exception e) {
          // ignore
        }

        integration = new CleverTapIntegration(clevertap, logger);
    }


    @Test
    public void activityCreate() {
        Activity activity = mock(Activity.class);
        Bundle bundle = mock(Bundle.class);
        integration.onActivityCreated(activity, bundle);
        verifyStatic();
        CleverTapAPI.setAppForeground(true);
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void activityStart() {
        Activity activity = mock(Activity.class);
        integration.onActivityStarted(activity);
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void activityResume() {
        Activity activity = mock(Activity.class);
        integration.onActivityResumed(activity);
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void activityPause() {
        Activity activity = mock(Activity.class);
        integration.onActivityPaused(activity);
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void activityStop() {
        Activity activity = mock(Activity.class);
        integration.onActivityStopped(activity);
        verifyNoMoreCleverTapInteractions();
    }


    @Test
    public void activitySaveInstance() {
        Activity activity = mock(Activity.class);
        Bundle bundle = mock(Bundle.class);
        integration.onActivitySaveInstanceState(activity, bundle);
        verifyNoMoreCleverTapInteractions();
    }

    @Test
    public void activityDestroy() {
        Activity activity = mock(Activity.class);
        integration.onActivityDestroyed(activity);
        verifyNoMoreCleverTapInteractions();
    }

    private void verifyNoMoreCleverTapInteractions() {
        verifyNoMoreInteractions(CleverTapAPI.class);
        verifyNoMoreInteractions(clevertap);
    }
}